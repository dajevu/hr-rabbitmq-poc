package com.hireright.rabbit.consumer;

import com.hireright.rabbit.Constants;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RabbitConsumer {

    static ConnectionFactory connectionFactory;

    public static void main(String[] argv) throws Exception {

        // This method will create the exchanges and queues if they don't already exist
        setupExchangeAndQueues();

        connectionFactory = new ConnectionFactory();

        connectionFactory.setHost("localhost");

        // This will connect to the Rabbit instance running on localhost
        Connection connection = connectionFactory.newConnection();

        Channel channel = connection.createChannel();

        // This method enables the publisher to send acknowledgements that the message
        // was received and processed into the binded queue.
        channel.confirmSelect();

        // This listener is listening for confirmation from the Exchange
        // that the message was received and sent to the appropriate queue.
        // See, this consumer is receiving a rabbit message, but then sending
        // a response. This is used for the response.
        channel.addConfirmListener(new ConfirmListener() {
            public void handleAck(long l, boolean b) throws IOException {
                System.out.println("Message :: " + l + " sent successfully");
            }

            public void handleNack(long l, boolean b) throws IOException {
                System.out.println("Message :: " + l + " NOT SENT successfully");
            }
        });

        channel.addReturnListener(new ReturnListener() {
            public void handleReturn(int i, String s, String s2, String s3, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
                System.out.println("Messages returned for exchange (NOT SENT):: " + s2);
            }
        });

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        QueueingConsumer consumer = new QueueingConsumer(channel);

        // Starts the consumer
        channel.basicConsume(Constants.REQUEST_QUEUE, true, consumer);

        // This while loop will ensure that messages are consumed as they are received, until
        // the Main is stopped with CTRL-C.
        while (true) {
            try {
                // Grabs the next message arriving in the queue
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                // Grabs the message body
                String message = new String(delivery.getBody());

                // Grabs the routing key used when the message was published.
                String routingKey = delivery.getEnvelope().getRoutingKey();

                System.out.println(" [x] Received '" + routingKey + "':'" + message + "'");

                // Now, let's create an acknowledgement message using the correlationId and replyTo as the destination routing key.
                // NOTE: In this example, I'm simply creating a GUID as the correlationId, but this would typically be
                // the HireRight subrequest #.
                channel.basicPublish(Constants.EXCHANGE, delivery.getProperties().getReplyTo(), true,  false,
                        new AMQP.BasicProperties.Builder()
                                .contentType("text/plain").deliveryMode(2)
                                .priority(1).correlationId(String.valueOf(java.util.UUID.randomUUID().toString()))
                                .build(),
                        "nop".getBytes());


            } catch (Exception e) {
                System.out.println("Error receiving message: " + e.toString());
            }
        }
    }

    /* Method will setup the exchanges and queues for the consumer */
    public static void setupExchangeAndQueues() throws Exception {
        connectionFactory = new ConnectionFactory();

        connectionFactory.setHost("localhost");

        Connection connection = connectionFactory.newConnection();

        Channel channel = connection.createChannel();

        // This will create a new exchange (similar to a post office)
        channel.exchangeDeclare(Constants.EXCHANGE, Constants.EXCHANGE_TYPE, true);

        // Create a dead-letter exchange - this is the exchange messages are forwarded when they expire
        // off the regular queue, i.e. those messages that don't get picked up within the time-to-live period.
        channel.exchangeDeclare(Constants.DEAD_LETTER_EXCHANGE, Constants.EXCHANGE_TYPE, true);

        // This is the queue that expiring messages will eventually go
        channel.queueDeclare(Constants.DEAD_LETTER_QUEUE, true, false, false, null);

        // This will create the two new queues, one for request, other for response.
        // Think of a queue like a specific PO box that the message will be forwarded to by the exchange.
        Map<String, Object> queueProps = new HashMap<String, Object>();

        // We want to expire messages off the queue if they don't get responded to within specified period of time - they
        // will be moved to the dhs.dead.letter queue
        queueProps.put("x-message-ttl", 50000);
        queueProps.put("x-dead-letter-exchange", Constants.DEAD_LETTER_EXCHANGE);
        queueProps.put("x-dead-letter-routing-key", Constants.DEAD_LETTER_QUEUE);

        // Create the request and response queues
        channel.queueDeclare(Constants.REQUEST_QUEUE, true, false, false, queueProps);
        channel.queueDeclare(Constants.REPLY_QUEUE, true, false, false, queueProps);

        // Now, setup the routing rules from the exchange to the queue. Note, we're using
        // the same name for the routing-key as the queue name, but they could be different.
        channel.queueBind(Constants.DEAD_LETTER_QUEUE, Constants.DEAD_LETTER_EXCHANGE, Constants.DEAD_LETTER_QUEUE);
        channel.queueBind(Constants.REQUEST_QUEUE, Constants.EXCHANGE, Constants.REQUEST_QUEUE);
        channel.queueBind(Constants.REPLY_QUEUE, Constants.EXCHANGE, Constants.REPLY_QUEUE);

    }
}
