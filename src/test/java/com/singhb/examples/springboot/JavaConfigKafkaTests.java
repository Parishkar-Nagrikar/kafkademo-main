package com.singhb.examples.springboot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
public class JavaConfigKafkaTests {

	@Autowired
	private Listener listener;

	@Autowired
	private KafkaTemplate<Integer, String> template;

	@Test
	public void testSimple() throws Exception {
		template.send("annotated1", 0, "foo");
		template.flush();
		assertTrue(this.listener.latch1.await(10, TimeUnit.SECONDS));
	}

	
	
}
