package org.entit.rga.consumer;

import java.util.HashMap;

import org.entit.rga.service.notification.RGANotificationService;
import org.entit.rga.web.model.RGARequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RGAConsumer {

	@Autowired
	private RGANotificationService notificationService;
	
	@KafkaListener(topics = { "${persister.update.buildingplan.topic}", "${persister.save.buildingplan.topic}",
			"${persister.update.buildingplan.workflow.topic}" })
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		ObjectMapper mapper = new ObjectMapper();
		RGARequest rGARequest = new RGARequest();
		try {
			log.debug("Consuming record: " + record);
			rGARequest = mapper.convertValue(record, RGARequest.class);
		} catch (final Exception e) {
			log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
		}
//		log.debug("BPA Received: " + bpaRequest.getRegularisation().getApplicationNo());
		notificationService.process(rGARequest);
	}
}
