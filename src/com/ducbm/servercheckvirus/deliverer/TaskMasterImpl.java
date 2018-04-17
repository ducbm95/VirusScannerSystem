/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.servercheckvirus.deliverer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ducbm
 */
public class TaskMasterImpl implements TaskMaster {
    
    private static final String TASK_QUEUE_NAME = "TASK_SCAN_VIRUS";
    private static final String MASTER_HOST = "localhost";
    
    private Connection connection;
    private Channel channel;
    
    public TaskMasterImpl() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(MASTER_HOST);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }
    
    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }
     
    @Override
    public String scanFileForVirus(String fileLocation) {
        try {
            String replyQueueName = channel
                    .queueDeclare()
                    .getQueue();
            
            String corrId = UUID.randomUUID().toString();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();
            
            channel.basicPublish("", TASK_QUEUE_NAME, props,
                    fileLocation.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + fileLocation + "'");
            
            final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
            channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                        AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrId)) {
                        response.offer(new String(body, "UTF-8"));
                    }
                }
            });
            String responseText = response.take();
            System.out.println(" [x] Received '" + responseText + "'");
            return responseText;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ex) {
            Logger.getLogger(TaskMasterImpl.class.getName()).log(Level.SEVERE, "", ex);
        }
        return "ERROR";
    }
    
}
