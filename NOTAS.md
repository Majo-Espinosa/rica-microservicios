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
