package com.saga.estudo.order.service.config.observability;

import com.mongodb.MongoClientSettings;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.mongo.v3_1.MongoTelemetry;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

@Configuration(proxyBeanMethods = false)
public class ContextPropagationConfiguration {

    @Bean
    ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer(OpenTelemetry openTelemetry) {
        // Inicializa a telemetria passando a instância global injetada pelo Spring Boot 4
        MongoTelemetry mongoTelemetry = MongoTelemetry.builder(openTelemetry)
                .build();

        // O método correto da interface funcional é 'customize(MongoClientSettings.Builder clientSettingsBuilder)'
        return new MongoClientSettingsBuilderCustomizer() {
            @Override
            public void customize(MongoClientSettings.Builder clientSettingsBuilder) {
                // Adiciona o CommandListener do OTel utilizando o método correto da biblioteca: createCommandListener()
                clientSettingsBuilder.addCommandListener(mongoTelemetry.createCommandListener());
            }
        };
    }

}
