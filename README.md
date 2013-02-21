hr-rabbitmq-poc
===============

Example of RabbitMQ usage for request/response pattern. Demonstrated with standard Java client and Spring AMQP.

There are two main examples included in the project. One uses the standard RabbitMQ Java client, whereas the
other uses Springs AMQP support.  Each are described below.

RabbitMQ Setup
==============
The examples require that you have access to a RabbitMQ instance. See the instructions on RabbitMQ's
site for installing in your environment. Examples assume you are using 2.8 or higher or Rabbit, and
use the standard installation ports etc.

Standard Java AMQP Client
=========================

This example uses the standard RabbitMQ Java client. In order to run it, open two command windows and
cd to the location of the project.

In command window #1, we'll compile the project and start the example RabbitMQ consumer. Issue the
following commands:

1. mvn compile
2. mvn exec:java -Dexec.mainClass="com.hireright.rabbit.consumer.RabbitConsumer"

In command window #2, we'll run the Rabbit publisher (publishes messages to the exchange). In that window:

1. mvn exec:java -Dexec.mainClass="com.hireright.rabbit.producer.RabbitProducerSeeder"

Spring Java Example
===================

The RabbitConsumerSpring class illustrates the use of Springs AMQP library. It abstracts out some of
the more complicated parts of working directly with the Java client, and is a good solution
for those of you using Spring. Unlike with the standard Java Rabbit client example, this example
will actually both publish the message to Rabbit and will listen simultaneously as a consumer.

If you have previously run the "Standard Java AMQP Client", use the RabbitMQ administrator to first
remove the "dhs.dead.letter" and "dhs.exchange" exchanges. Also remove any queues that start with
dhs.*. While not absolutely necessary, this will ensure things are properly instantiated for the
Spring sample.

To run it, be sure to exit out of the Java consumer (from the above Standard Java AMQP client example),
and then issue:

1. mvn exec:java -Dexec.mainClass="com.hireright.rabbit.consumer.RabbitConsumerSpring"


Importing into Eclipse
======================

If you prefer to use Eclipse to run the project, you can import it as a Maven project. To do so:

1. Select File->Import. Open the "Maven" folder.
2. Select "Existing Maven Projects". Click "Next >".
3. Select the root directory where you cloned this GIT repository locally.
4. A window should appear with the pom.xml selected. It will include the default project name. Select "Finish".

After a moment of processing, the project should now appear in Eclipse.


