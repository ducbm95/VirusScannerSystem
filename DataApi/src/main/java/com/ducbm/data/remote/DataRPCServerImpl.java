/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.data.remote;

import com.ducbm.data.api.KyotoVirusDataRepo;
import com.ducbm.data.api.VirusDataRepository;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author ducbm
 */
public class DataRPCServerImpl implements DataRPCServer {
    
    private static final String DATA_RPC_HOST = "localhost";
    private static final String DATA_RPC_QUEUE_NAME = "DATA_RPC_SERVICE";
    
    private static final int REQUEST_SELECT_ONE = 0;
    private static final int REQUEST_SAVE = 1;
    private static final int REQUEST_DELELE = 2;
    
    private final Connection connection;
    private final Channel channel;
    private final VirusDataRepository repository;
    
    public DataRPCServerImpl() throws IOException, TimeoutException {
        repository = new KyotoVirusDataRepo();
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(DATA_RPC_HOST);
        
        connection = factory.newConnection();
        channel = connection.createChannel();
        
        channel.queueDeclare(DATA_RPC_QUEUE_NAME, false, false, false, null);
        channel.basicQos(10);
        System.out.println(" [x] Awaiting Data RPC requests");
    }
    
    public void close() {
        if (connection != null)
            try {
                connection.close();
            } catch (IOException _ignore) {}
    }
    
    @Override
    public void serveForDataRPCServer() {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                    AMQP.BasicProperties properties, byte[] body) throws IOException {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();
                String requestMsg = new String(body,"UTF-8");
                JSONObject jsonObj = new JSONObject(requestMsg);
                Integer requestType = Integer.valueOf(jsonObj.get("type").toString());
                String sha256 = jsonObj.get("sha256").toString();
                
                switch (requestType) {
                    case REQUEST_SELECT_ONE:
                        String response = repository.selectOne(sha256);
                        channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                        break;
                    case REQUEST_SAVE:
                        String data = jsonObj.get("data").toString();
                        repository.save(sha256, data);
                        break;
                    case REQUEST_DELELE:
                        repository.delete(sha256);
                        break;
                }
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        try {
            channel.basicConsume(DATA_RPC_QUEUE_NAME, false, consumer);
        } catch (IOException ex) {
            Logger.getLogger(DataRPCServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
