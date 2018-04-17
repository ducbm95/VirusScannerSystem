/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.data.remote;

import com.ducbm.commonutils.AppConfiguration;
import com.ducbm.commonutils.Constants;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 *
 * @author ducbm
 */
public class DataRPCClientImpl implements DataRPCClient {
    
    private static final Logger LOGGER =
            LogManager.getLogger(DataRPCClient.class.getCanonicalName());
    
    private final Connection connection;
    private final Channel channel;
    
    public DataRPCClientImpl() throws IOException, TimeoutException {
        String rpcHost = AppConfiguration.getConfigInstance()
                .getString(Constants.CONFIG_ATTR_DATA_REMOTE_HOST);
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rpcHost);
        
        connection = factory.newConnection();
        channel = connection.createChannel();
    }
    
    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }
    
    @Override
    public String selectOne(String sha256) {
        try {
            String replyQueueName = channel.queueDeclare().getQueue();
            String corrId = UUID.randomUUID().toString();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();
            String requestData = buildRequestData(DataRPCServer.REQUEST_SELECT_ONE, sha256, null);
            channel.basicPublish("", Constants.DATA_RPC_QUEUE_NAME, props, requestData.getBytes("UTF-8"));
            
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
            return response.take();
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e);
        }
        return "";
    }
    
    @Override
    public void save(String sha256, String data) {
        String requestData = buildRequestData(DataRPCServer.REQUEST_SAVE, sha256, data);
        try {
            channel.basicPublish("", Constants.DATA_RPC_QUEUE_NAME,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    requestData.getBytes("UTF-8"));
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }
    
    @Override
    public void delete(String sha256) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private String buildRequestData(int requestType, String sha256, String data) {
        JSONObject requestData = new JSONObject();
        requestData.put("type", requestType);
        requestData.put("sha256", sha256);
        requestData.put("data", data);
        return requestData.toString();
    }
    
}
