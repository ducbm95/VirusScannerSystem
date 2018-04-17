/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.servercheckvirus.remote;

import com.ducbm.commonutils.AppConfiguration;
import com.ducbm.commonutils.Constants;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

/**
 *
 * @author ducbm
 */
public class RPCClientImpl implements RPCClient {
    
    private static final Logger LOGGER =
            LogManager.getLogger(RPCClientImpl.class.getCanonicalName());
    
    private static final Integer SUCCESS = 1;
    private static final Integer FAILURE = 0;
    
    private final Connection connection;
    private final Channel channel;
    
    public RPCClientImpl() throws IOException, TimeoutException {
        String rpcHost = AppConfiguration.getConfigInstance().getString(Constants.CONFIG_ATTR_SERVER_CHECKVIRUS_HOST);
        int qos = AppConfiguration.getConfigInstance().getInt(Constants.CONFIG_ATTR_SERVER_CHECKVIRUS_QOS);
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rpcHost);
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(qos);
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
            
            channel.basicPublish("", Constants.VIRUS_SER_RPC_QUEUE_NAME, props,
                    fileLocation.getBytes("UTF-8"));
            
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
            return buildJSONResponse(SUCCESS, responseText);
        } catch (IOException | InterruptedException ex) {
            LOGGER.error(ex);
            return buildJSONResponse(FAILURE, "");
        } finally {
            try {
                channel.close();
                connection.close();
            } catch (IOException | TimeoutException ex) {
                LOGGER.error(ex);
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
