package com.singhb.examples.springboot.web.controller;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoRestController {
	public static Logger logger = LoggerFactory.getLogger(DemoRestController.class);
	@Autowired
	@Qualifier("kafkaTemplateBoot")
	private KafkaTemplate<String, String> template;

	@GetMapping("/producer/{message}")
	public String sendMessage(@PathVariable(name = "message") String message) {

		try {
			send(message);
			System.out.println(" msg sent : " + message);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "Done Sending-->" + message;
	}
	
	
//producer
	@Transactional
	public void send(String message) throws InterruptedException {
		this.template.send("myTopic", message);
		System.out.println(" producer msg sent pass!!!" + message);
	}

//read it from last committed offset
//consumer
	// , Acknowledgment ack
	@KafkaListener(topics = "myTopic", containerFactory = "kafkaListenerContainerFactory")
	public void listen(KafkaConsumer<String,String> consumer,ConsumerRecord<String,String> cr, String data , 
			@Header(KafkaHeaders.OFFSET) int offset,Acknowledgment ack) 
			throws Exception {

			System.out.println(" consumer called!!! message : " + data);
			System.out.println(" consumer called!!! consumer: " + consumer);
			System.out.println(" consumer called!!! ConsumerRecord: " + cr);	
			System.out.println(" consumer called!!! offset: " + offset);	//12
		
		if (data.equals("foo31")) {
			logger.info("There was an exception -->" + data.toString());
			throw new Exception();//12
		} else {
			logger.info("Received -->" + data.toString());		
			consumer.commitAsync();
			ack.acknowledge();
		}
	}
}