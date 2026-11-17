# NOTAS — rica-microservicios

## Taller Lección 5 — Descomposición de rica-api

### Paso 1. El corte: qué va en cada servicio

| Paquete / clase actual | Destino |
|---|---|
| `investigadores.*` (dominio, aplicación, infraestructura) | `investigadores-service` |
| `publicaciones.*` (incluye `LimitePublicacionesAnualesService`) | `publicaciones-service` |
| `compartido.GlobalExceptionHandler`, `RecursoNoEncontradoException` | Se duplica en ambos: cada servicio necesita su propio manejo de errores; ya no hay un único `@ControllerAdvice` compartido |
| `plataforma.StatusController`, `CorsConfig` | Se duplica en ambos, adaptado (cada servicio expone su propio `/api/status`) |
| `plataforma.ArranqueInformativo`, `SaludoInstitucionalService` | Se descarta: era una demostración del Tutorial 2 (IoC/DI) sin valor de negocio real |

**Sobre la duplicación de `compartido` y `plataforma`:** es la primera consecuencia real de
*Database per Service* y del despliegue independiente. Compartir un `@ControllerAdvice` implicaría
una librería o módulo común versionado por separado (un *shared kernel*). Queda fuera del alcance
de este taller, pero es la primera decisión de arquitectura real que el corte obliga a tomar:
duplicar poco código a cambio de no acoplar el ciclo de despliegue de los dos servicios.

### La costura que se cortó

En el monolito, `PublicacionService` llamaba a `investigadorRepository.existsByCorreoInstitucional_Valor(...)`:
una llamada de método en memoria que cruzaba en silencio el límite entre dos dominios.
Ahora `publicaciones-service` depende del puerto `VerificadorInvestigador`, y el adaptador temporal
`VerificadorInvestigadorPendiente` acepta cualquier correo. La dependencia no desapareció:
se hizo visible. Se resuelve en el Taller de la Lección 6 (gRPC + Circuit Breaker).

### Puertos y bases de datos

| Servicio | Puerto HTTP | Base de datos | Contenedor |
|---|---|---|---|
| investigadores-service | 8080 | PostgreSQL `rica` (5432) | `investigadores-db` |
| publicaciones-service | 8081 | MongoDB `rica` (27017) | `publicaciones-mongo` |

### Nota técnica: propiedad de MongoDB en Spring Boot 4

La guía usa `spring.data.mongodb.uri`. En Spring Boot 4 esa propiedad **se ignora en silencio** y
el cliente se conecta a `localhost:27017/test`. Se verificó corriendo el servicio contra un MongoDB
en otro puerto: con `spring.data.mongodb.uri` la petición falla tras 30 s de espera (sigue buscando
`localhost:27017`); con `spring.mongodb.uri` funciona. Por eso los servicios usan `spring.mongodb.uri`,
igual que el `application.yaml` original de rica-api.

### Cómo correr ambos servicios a la vez

```powershell
# terminal 1
cd investigadores-service
docker compose up -d
.\mvnw.cmd spring-boot:run

# terminal 2
cd publicaciones-service
docker compose up -d
.\mvnw.cmd spring-boot:run
```

Antes de empezar, detén el stack Docker de rica-api (`docker compose down` en rica-api): usa los
mismos puertos 5432 y 27017.

## Taller Lección 6 — Comunicación entre servicios

### Paso 1. Plan de comunicación

| Necesidad | Patrón | Dónde |
|---|---|---|
| Confirmar que un investigador existe antes de registrar una publicación | RPI síncrono — gRPC | Paso 2 |
| No caer en cascada si investigadores-service está lento o caído | Circuit Breaker (Resilience4j) | Paso 3 |
| Que ningún servicio dependa de una dirección de red fija | Service Registry (Eureka) | Paso 4 |
| Un único punto de entrada para el frontend | API Gateway | Paso 5 |
| Notificar que se registró un investigador, sin acoplar publicador y oyentes | Domain Event + Transactional Outbox (Kafka) | Paso 6 |
| Eliminar un investigador y marcar sus publicaciones, sin transacción distribuida | Saga (coreografía) | Paso 7 |

### Puertos

| Componente | Puerto |
|---|---|
| eureka-server | 8761 |
| gateway | 8000 |
| investigadores-service | 8080 (REST), 9090 (gRPC) |
| publicaciones-service | 8081 |
| Kafka (`rica-kafka`) | 9092 |

### Limitación documentada: la dirección gRPC sigue fija

Eureka resuelve el descubrimiento para las llamadas HTTP balanceadas (`lb://` en el Gateway), pero el
canal gRPC de publicaciones-service sigue apuntando a `static://localhost:9090`. Integrar Eureka con el
balanceo de canales gRPC es posible, pero añade complejidad fuera del alcance del taller.

### Dónde estaría la compensación de la Saga

Si publicaciones-service está caído, el evento `investigadores.en-eliminacion` espera en Kafka y la Saga
continúa sola cuando el servicio vuelve. Pero ante un error permanente, el investigador queda
indefinidamente `EN_ELIMINACION`: nunca desaparece a medias, pero tampoco se recupera. La compensación
sería un temporizador que, tras un plazo sin confirmación, regrese el estado a `ACTIVO`.

### Verificación de punta a punta (Paso 8)

- `POST /api/investigadores` por el Gateway → 201; Eureka lista GATEWAY, INVESTIGADORES-SERVICE y PUBLICACIONES-SERVICE.
- En el log de publicaciones-service: `Evento recibido [investigadores.registrados]: {...}`.
- `POST /api/publicaciones` con correo existente → 201 (verificado por gRPC); con correo INVENTADO → 404.
- `DELETE /api/investigadores/{id}` → 202, y en los logs:
  `Evento N publicado en investigadores.en-eliminacion` → `Saga: 1 publicaciones de ... marcadas como huérfanas`
  → `Saga: publicaciones confirmó para ... Eliminando investigador.`
- Con Kafka apagado el registro responde 201 igual; el evento queda con `publicado = f` y se publica al volver Kafka.
- Con investigadores-service apagado: 503 `UNAVAILABLE` y, tras 5 llamadas, `CLOSED to OPEN` y 503 inmediato (circuito abierto).

## Taller Lección 8 — CQRS: consultas-service

### Plan

| Servicio | Lado | Qué hace |
|---|---|---|
| investigadores-service | Escritura | Publica `investigadores.registrados`, `investigadores.en-eliminacion` y ahora `investigadores.eliminados` |
| publicaciones-service | Escritura | Publica `publicaciones.registradas` |
| consultas-service (8083) | Lectura | Escucha los cuatro eventos y mantiene una ficha por investigador en su propia base `rica_consultas`. Solo responde GET |

### Qué se observó al probarlo

- Una sola consulta (`GET /api/consultas/investigadores/{correo}`) devuelve el investigador con sus
  publicaciones sin llamar a ningún otro servicio.
- **Lectura caída, escritura intacta:** con consultas-service detenido, registrar una publicación sigue
  respondiendo 201; al volver, el consumidor procesa el evento pendiente y el total sube solo.
  Si el servicio se mata a la fuerza, Kafka espera a que venza la sesión del consumidor anterior
  (unos 45 s) antes de reasignar la partición: la ficha se actualiza con ese retraso.
- **La Saga limpia la ficha:** tras el DELETE, la ficha pasa a `EN_ELIMINACION` y luego desaparece (404).
- **Reconstrucción:** al borrar la colección y arrancar con un `group-id` nuevo, las fichas reaparecen.
  En la prueba, la ficha de un investigador eliminado **resucitó** en estado `EN_ELIMINACION`: al reproducir,
  `investigadores.eliminados` se procesó antes que el último `publicaciones.registradas`. Kafka no
  garantiza orden entre tópicos distintos; en producción se evita publicando todos los eventos de un
  agregado en un solo tópico, con número de versión.

### Qué se vería distinto si lectura y escritura fueran el mismo modelo

1. Ficha con dos publicaciones: haría falta un JOIN entre servicios (o una llamada a cada uno) en cada consulta.
2. Ranking: se calcularía en cada petición contando publicaciones, en vez de leer un campo ya guardado.
3. Log de proyección: no existiría; no habría consistencia eventual, pero tampoco aislamiento entre cargas.
4. Ficha eliminada → 404: se borraría en la misma transacción, sin estado intermedio observable.
5. Reconstrucción: imposible; el modelo sería la fuente de verdad y no podría tirarse y rehacerse.
