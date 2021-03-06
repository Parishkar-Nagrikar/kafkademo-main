package com.singhb.examples.springboot;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
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
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableKafka
public class Config {

	@Bean
	ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		
		factory.setConsumerFactory(consumerFactory());
		//factory.getContainerProperties().setTransactionManager(kafkaTransactionManager());
		factory.setRetryTemplate(kafkaRetry());
        //***********/
		//factory.getContainerProperties().setAckMode(AckMode.MANUAL);
//		factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
//		factory.setErrorHandler(((exception, data) -> {           
//			/* here you can do you custom handling, I am just logging it same as default Error handler doesIf you just want to log. 
//			 * you need not configure the error handler here.
//			 *  The default handler does it for you.Generally, you will persist the failed records to DB for tracking the failed records.  */
//				System.out.println("Error in process with Exception {} and the record is {}"+ exception+ data);      
//			}));
		 factory.setRetryTemplate(kafkaRetry());
	        factory.setRecoveryCallback(retryContext -> {
	            ConsumerRecord consumerRecord = (ConsumerRecord) retryContext.getAttribute("record");
	            System.out.println("Recovery is called for message {} "+ consumerRecord.value());
	            return Optional.empty();
	        });
		return factory;
	}

    public RetryTemplate kafkaRetry() {
        RetryTemplate retryTemplate = new RetryTemplate();
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

	@Bean
	public Listener listener() {
		return new Listener();
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
	
	private RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
      /* here retry policy is used to set the number of attempts to retry and what exceptions you wanted to try and what you don't want to retry.*/
         retryTemplate.setRetryPolicy(getSimpleRetryPolicy());
        return retryTemplate;
    }
	//***********//
    private SimpleRetryPolicy getSimpleRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();// the boolean value in the map determines whether exception should be retried exception.
        exceptionMap.put(Exception.class, true);
        return new SimpleRetryPolicy(3,exceptionMap,true);
    }

/*-------------------------producer---------------------------------*/
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
