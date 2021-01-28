package com.singhb.examples.springboot;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.transaction.KafkaTransactionManager;

@Configuration
@EnableKafka
public class Config {

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.getContainerProperties().setAckMode(AckMode.MANUAL);
		//factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
		factory.setConsumerFactory(consumerFactory());
		//factory.getContainerProperties().setTransactionManager(kafkaTransactionManager());
	
		return factory;
	}

	@Bean
	public KafkaTransactionManager< Integer, String> kafkaTransactionManager(){
		return new KafkaTransactionManager<Integer, String>(producerFactoryMock());
	}
	
  
	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs());
	}

	@Bean
	public Map<String, Object> consumerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "tstgropid");
		
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "6000");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		
		props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		//props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);		
		
		return props;

	}

	@Bean
	public Listener listener() {
		return new Listener();
	}

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}
	
	@Bean
	public ProducerFactory<Integer, String> producerFactoryMock() {
		return new DefaultKafkaProducerFactory<>(producerConfigsMock());
	}
//predinine config properties
	@Bean
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		//props.put(ProducerConfig.RETRIES_CONFIG, 0);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.RETRIES_CONFIG, 10);
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
		//props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "PRODI1");
		
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return props;
	}
	
	@Bean
	public Map<String, Object> producerConfigsMock() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
		props.put(ProducerConfig.RETRIES_CONFIG, 1);
		props.put(ProducerConfig.ACKS_CONFIG, "all");
		props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
		props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "PRODI2");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		return props;
	}


	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<String, String>(producerFactory());
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplateBoot() {
		return new KafkaTemplate<String, String>(producerFactoryBoot());
	}

	@Bean
	public ProducerFactory<String, String> producerFactoryBoot() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}
}
