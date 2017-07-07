package com.dsinpractice.spikes.kafka.multipleconsumers;

public final class MultipleConsumersMain {

  public static void main(String[] args) {

    String brokers = "localhost:9027";
    String groupId = "atlas";
    String topic = "ATLAS_HOOK";
    int numberOfConsumer = 1;


    if (args != null && args.length > 4) {
      brokers = args[0];
      groupId = args[1];
      topic = args[2];
      numberOfConsumer = Integer.parseInt(args[3]);
    }

    // Start Notification Producer Thread
    NotificationProducerThread producerThread = new NotificationProducerThread(brokers, topic);
    Thread t1 = new Thread(producerThread);
    t1.start();


    // Start group of Notification Consumers
    NotificationConsumerGroup consumerGroup =
        new NotificationConsumerGroup(brokers, groupId, topic, numberOfConsumer);

    consumerGroup.execute();

    try {
      Thread.sleep(100000);
    } catch (InterruptedException ie) {

    }
  }
}
