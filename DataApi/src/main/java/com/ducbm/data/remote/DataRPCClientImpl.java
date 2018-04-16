/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.data.remote;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
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
public class DataRPCClientImpl implements DataRPCClient {
    
    private static final String DATA_RPC_HOST = "localhost";
    private static final String DATA_RPC_QUEUE_NAME = "DATA_RPC_SERVICE";
    
    private static final int REQUEST_SELECT_ONE = 0;
    private static final int REQUEST_SAVE = 1;
    private static final int REQUEST_DELELE = 2;
    
    private Connection connection;
    private Channel channel;
    
    private String replyQueueName;
    
    public DataRPCClientImpl() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(DATA_RPC_HOST);
        
        connection = factory.newConnection();
        channel = connection.createChannel();
        replyQueueName = channel.queueDeclare().getQueue();
        
    }
    
    @Override
    public String selectOne(String sha256) {
        String corrId = UUID.randomUUID().toString();
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();
        String requestData = buildRequestData(REQUEST_SELECT_ONE, sha256, null);
        
        try {
            channel.basicPublish("", DATA_RPC_QUEUE_NAME, props, requestData.getBytes("UTF-8"));
            
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
            return response.take().toString();
        } catch (Exception e) {
            
        }
        return "";
    }
    
    @Override
    public void save(String sha256, String data) {
        String requestData = buildRequestData(REQUEST_SAVE, sha256, data);
        try {
            channel.basicPublish("", DATA_RPC_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    requestData.getBytes("UTF-8"));
        } catch (IOException ex) {
            Logger.getLogger(DataRPCClientImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void delete(String sha256) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private String buildRequestData(int requestType, String sha256, String data) {
        JSONObject requestData = new JSONObject();
        requestData.put("type", requestType);
        requestData.put("sha256", sha256);
        requestData.put("data", data);
        return requestData.toString();
    }
    
}
