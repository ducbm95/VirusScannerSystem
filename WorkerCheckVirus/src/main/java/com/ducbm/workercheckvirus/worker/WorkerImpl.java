/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.workercheckvirus.worker;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;

/**
 *
 * @author ducbm
 */
public class WorkerImpl implements Worker {
    
    private static final String TASK_QUEUE_NAME = "TASK_SCAN_VIRUS";
    private static final String MASTER_HOST = "localhost";
    
    @Override
    public void waitForTaskScanVirus() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(MASTER_HOST);
            final Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();
            
            channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            channel.basicQos(1);
            
            final Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                        AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    
                    String fileLocation = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + fileLocation + "'");
                    
                    String response = "";
                    try {
                        response = TaskResolver.doTask(fileLocation);
                    } finally {
                        channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                        channel.basicAck(envelope.getDeliveryTag(), false);
                        
                        // RabbitMq consumer worker thread notifies the RPC server owner thread
                        synchronized(this) {
                            this.notify();
                        }
                    }
                }
            };
            channel.basicConsume(TASK_QUEUE_NAME, false, consumer);
            
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized(consumer) {
                    try {
                        consumer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
