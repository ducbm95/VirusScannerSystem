/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.serverchat.remote;

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
import org.json.JSONObject;

/**
 *
 * @author ducbm
 */
public class RPCClientImpl implements RPCClient {
    
    private static final String RPC_HOST = "localhost";
    private static final int RPC_PORT = 8100;
    private static final String RPC_QUEUE_NAME = "SCAN_VIRUS_SERVICE";
    
    private static final Integer SUCCESS = 1;
    private static final Integer FAILURE = 0;
    
    private Connection connection;
    private Channel channel;
    
    public RPCClientImpl() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RPC_HOST);
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(10);
    }
    
    @Override
    public String scanFileForVirus(String fileLocation) {
        try {
            String replyQueueName = channel.queueDeclare().getQueue();
            String corrId = UUID.randomUUID().toString();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();
            
            channel.basicPublish("", RPC_QUEUE_NAME, props, fileLocation.getBytes("UTF-8"));
            
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
            String responseText = response.take().toString();
            return buildJSONResponse(SUCCESS, responseText);
        } catch (IOException e) {
            return buildJSONResponse(FAILURE, "");
        } catch (InterruptedException e) {
            return buildJSONResponse(FAILURE, "");
        } finally {
            try {
                channel.close();
                connection.close();
            } catch (IOException e) {
                e.printStackTrace(); // show log here
            } catch (TimeoutException ex) {
                Logger.getLogger(RPCClientImpl.class.getName()).log(Level.SEVERE, "", ex);
            }
        }
    }
    
    private String buildJSONResponse(Integer status, String response) {
        JSONObject resObj = new JSONObject();
        resObj.put("status", status);
        resObj.put("response", new JSONObject(response));
        return resObj.toString();
    }
}
