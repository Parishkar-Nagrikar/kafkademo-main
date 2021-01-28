package com.singhb.examples.springboot;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
public class DemoApplication implements CommandLineRunner {

	public static Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Autowired
	@Qualifier("kafkaTemplateBoot")
	private KafkaTemplate<String, String> template;

	private final CountDownLatch latch = new CountDownLatch(3);

	@Override

	public void run(String... args) throws Exception {
		//send();
	}

	private void send() throws InterruptedException {
		this.template.executeInTransaction(kafkaTemplate -> {
			 kafkaTemplate.send("myTopic", System.currentTimeMillis() + ">1", "foo0");
			 kafkaTemplate.send("myTopic", System.currentTimeMillis() + ">3", "foo3");
			return null;
		});
		// this.template.send("myTopic", System.currentTimeMillis() + ">1", "foo1");
		// this.template.send("myTopic", System.currentTimeMillis() + ">2", "foo2");
		// this.template.send("myTopic", System.currentTimeMillis() + ">3", "foo3");
		latch.await(60, TimeUnit.SECONDS);
		logger.info("All received");
	}

	// , Acknowledgment ack
	//@KafkaListener(topics = "myTopic", containerFactory = "kafkaListenerContainerFactory")
	public void listen(String data) throws Exception {

		logger.info("Received -->" + data.toString());
		latch.countDown();
		if (data.equals("foo3")) {
			//throw new Exception();
		}
		// ack.acknowledge();
	}
}