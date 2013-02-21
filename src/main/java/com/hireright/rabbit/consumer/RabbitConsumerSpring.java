package com.hireright.rabbit.consumer;


import com.hireright.rabbit.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component (value = "rabbitSpring")
public class RabbitConsumerSpring implements MessageListener {

    @Autowired
    private volatile RabbitTemplate amqpRequestTemplate;

    @Autowired
    private volatile RabbitTemplate amqpResponseTemplate;

    private static final Logger log = LoggerFactory.getLogger(RabbitConsumerSpring.class);

    public static void main(String[] args) throws Exception {

        ApplicationContext context = new ClassPathXmlApplicationContext("spring-configuration.xml");

        RabbitConsumerSpring consumer = (RabbitConsumerSpring) context.getBean("rabbitSpring");

        consumer.start(args);
    }

    private void start(String[] args) throws Exception {

        // Send a test message
        MessageProperties messageProperties = new MessageProperties();

        messageProperties.setCorrelationId(java.util.UUID.randomUUID().toString().getBytes());

        Message rabbitMessage = new Message("Test request message".getBytes(), messageProperties);

        Thread.sleep(10000);

        log.debug("Sending test message...");

        amqpRequestTemplate.send(rabbitMessage);

    }

    public void onMessage(Message message) {

        log.debug("Received message " + new String(message.getBody()));

        /* Once the message is received, we'll send back to the response queue a response message
           that contains a hypothetical HireRight subrequest #.
         */

        MessageProperties messageProperties = new MessageProperties();

        messageProperties.setCorrelationId(message.getMessageProperties().getCorrelationId());

        messageProperties.setHeader(Constants.HIRERIGHT_SUBREQUEST_HEADER, "HR-3232-33222");

        Message outboundMsg = new Message("Response message".getBytes(), messageProperties);

        amqpResponseTemplate.send(outboundMsg);
    }
}
