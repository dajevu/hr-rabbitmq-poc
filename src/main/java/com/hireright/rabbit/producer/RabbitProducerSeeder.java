package com.hireright.rabbit.producer;

import com.hireright.rabbit.Constants;
import com.rabbitmq.client.*;

import java.io.IOException;

public class RabbitProducerSeeder implements Runnable {

    static ConnectionFactory connectionFactory;

    /* Run this class to seed/send messages as a rabbitmq producer */

    public static void main(String[] args) throws IOException, InterruptedException {

        connectionFactory = new ConnectionFactory();

        // Publish msgCount messages and wait for confirms.
        (new Thread(new RabbitProducerSeeder())).start();
    }

    public void run() {
        try {
            long startTime = System.currentTimeMillis();

            // Setup
            Connection conn = connectionFactory.newConnection();

            Channel ch = conn.createChannel();

            // Notifies the channel that the Rabbit exchange will be sending acks.
            ch.confirmSelect();

            // Listens for acks issued by Rabbit upon successful processing of the message.
            ch.addConfirmListener(new ConfirmListener() {
                public void handleAck(long l, boolean b) throws IOException {
                    System.out.println("Ack received for:: " + l);
                }

                public void handleNack(long l, boolean b) throws IOException {
                    System.out.println("nAck received for:: " + l);
                }
            });

            ch.addReturnListener(new ReturnListener() {
                public void handleReturn(int i, String s, String s2, String s3, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
                    System.out.println("Messages returned for exchange:: " + s2);
                }
            });

            // Publish some example messasges to the exchange. Notice we use the replyTo
            // header to provide a correlation id (replyTo method).
            for (long i = 0; i < Constants.MSG_COUNT; ++i) {
                ch.basicPublish(Constants.EXCHANGE, Constants.REQUEST_QUEUE, true,  false,
                        new AMQP.BasicProperties.Builder()
                                .contentType("text/plain").deliveryMode(2)
                                .priority(1).correlationId(String.valueOf(i)).replyTo(Constants.REPLY_QUEUE)
                                .build(),
                        "nop".getBytes());
            }

            // Method means that the channel will wait until all messages have been consumed
            // by the exchange
            ch.waitForConfirms();

            // Cleanup
            ch.close();
            conn.close();

            long endTime = System.currentTimeMillis();
            System.out.printf("Test took %.3fs\n",
                    (float) (endTime - startTime) / 1000);
        } catch (Throwable e) {
            System.out.println("foobar :(");
            System.out.print(e);
        }
    }


}
