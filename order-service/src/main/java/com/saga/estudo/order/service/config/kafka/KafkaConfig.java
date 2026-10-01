package com.saga.estudo.order.service.config.kafka;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.KafkaTelemetry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.internal.OpenTelemetryConsumerInterceptor;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.internal.OpenTelemetryProducerInterceptor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {

    private static final int REPLICA_COUNT = 1;
    private static final int PARTITION_COUNT = 1;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${kafka.topic.notify-ending}")
    private String notifyEndingTopic;
    @Value("${kafka.topic.start-saga}")
    private String startSagaTopic;


    @Bean
    public ConsumerFactory<String, String>consumerFactory(KafkaTelemetry kafkaTelemetry){
        return new DefaultKafkaConsumerFactory<>(consumerProps(kafkaTelemetry));
    }

    private Map<String, Object> consumerProps(KafkaTelemetry kafkaTelemetry) {
        Map<String, Object>  map = new HashMap<>();
        map.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        map.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupId);
        map.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        map.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        map.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, this.autoOffsetReset);

        map.putAll(kafkaTelemetry.consumerInterceptorConfigProperties());
        return map;
    }

    @Bean
    public ProducerFactory<String, String>producerFactory(KafkaTelemetry kafkaTelemetry){
        return new DefaultKafkaProducerFactory<>(producerProps(kafkaTelemetry));
    }

    @Bean
    public KafkaTelemetry kafkaTelemetry(OpenTelemetry openTelemetry){
        return KafkaTelemetry.create(openTelemetry);
    }

    private Map<String, Object> producerProps(KafkaTelemetry kafkaTelemetry) {
        Map<String, Object>  map = new HashMap<>();
        map.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        map.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        map.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        map.putAll(kafkaTelemetry.producerInterceptorConfigProperties());
        return map;
    }

    @Bean
    public KafkaTemplate<String,String> kafkaTemplate(ProducerFactory<String, String> producerFactory){
        return  new KafkaTemplate<>(producerFactory);
    }

    private NewTopic buildTopic(String name){
        return TopicBuilder
                .name(name)
                .replicas(REPLICA_COUNT)
                .partitions(PARTITION_COUNT)
                .build();
    }

    @Bean
    public NewTopic notifyEndingTopic(){
        return buildTopic(this.notifyEndingTopic);
    }

    @Bean
    public NewTopic startSagaTopic(){
        return buildTopic(this.startSagaTopic);
    }
}
