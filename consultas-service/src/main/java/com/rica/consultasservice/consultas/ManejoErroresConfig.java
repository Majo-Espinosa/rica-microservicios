package com.rica.consultasservice.consultas;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import tools.jackson.core.JacksonException;

/**
 * Que hacer cuando procesar un mensaje falla:
 *  - errores que no se arreglan reintentando (JSON roto, campo faltante) van directo a la Dead Letter Topic;
 *  - el resto se reintenta con espera creciente (1 s, 2 s, 4 s) y, si sigue fallando, tambien va a la DLT.
 * Asi un mensaje venenoso no bloquea la particion ni se pierde en silencio.
 */
@Configuration
public class ManejoErroresConfig {

    @Bean
    public DefaultErrorHandler manejadorDeErrores(KafkaTemplate<String, String> kafkaTemplate) {
        // Publica el mensaje fallido en "<topico-original>.DLT", con cabeceras que explican el error.
        // El destino se indica explicitamente: desde Spring Kafka 4 el sufijo por defecto es "-dlt",
        // y sin esto el mensaje iria a "investigadores.registrados-dlt" en vez del topico declarado.
        DeadLetterPublishingRecoverer recuperador = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (registro, error) -> new TopicPartition(registro.topic() + ".DLT", registro.partition()));

        ExponentialBackOffWithMaxRetries espera = new ExponentialBackOffWithMaxRetries(3);
        espera.setInitialInterval(1_000);
        espera.setMultiplier(2.0);
        espera.setMaxInterval(8_000);

        DefaultErrorHandler manejador = new DefaultErrorHandler(recuperador, espera);
        manejador.addNotRetryableExceptions(JacksonException.class, NullPointerException.class);
        return manejador;
    }

}
