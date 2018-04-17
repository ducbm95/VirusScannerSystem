/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.servercheckvirus.remote;

import com.ducbm.commonutils.AppConfiguration;
import com.ducbm.commonutils.Constants;
import com.ducbm.servercheckvirus.deliverer.TaskMaster;
import com.ducbm.servercheckvirus.deliverer.TaskMasterImpl;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author ducbm
 */
public class RPCServerImpl implements RPCServer {
    
    private static final Logger LOGGER =
            LogManager.getLogger(RPCServerImpl.class.getCanonicalName());
    
    private final Connection connection;
    private final Channel channel;
    
    private final ExecutorService executor; // thread pool
    private final TaskMaster taskMaster;
    
    public RPCServerImpl() throws TimeoutException, IOException {
        String rpcHost = AppConfiguration.getConfigInstance()
                .getString(Constants.CONFIG_ATTR_SERVER_CHECKVIRUS_HOST);
        int qos = AppConfiguration.getConfigInstance()
                .getInt(Constants.CONFIG_ATTR_SERVER_CHECKVIRUS_QOS);
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rpcHost);
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(Constants.VIRUS_SER_RPC_QUEUE_NAME, false, false, false, null);
        channel.basicQos(qos);
        System.out.println(" [x] Awaiting RPC requests");
        
        executor = Executors.newFixedThreadPool(qos); //creating a pool of 10 threads
        taskMaster = new TaskMasterImpl();
    }
    
    @Override
    public void serveScanFileForVirus() {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                    AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                
                System.out.println(" [*] Received a request.");
                WorkerConnection thread = new WorkerConnection(envelope, properties, body);
                executor.execute(thread);
            }
        };
        try {
            channel.basicConsume(Constants.VIRUS_SER_RPC_QUEUE_NAME, false, consumer);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        
        // Wait and be prepared to consume the message from RPC client.
        while (true) {
            synchronized(consumer) {
                try {
                    consumer.wait();
                } catch (InterruptedException ex) {
                    LOGGER.error(ex);
                }
            }
        }
    }
    
    public void close() {
        if (connection != null)
            try {
                connection.close();
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
    }
    
    public class WorkerConnection extends Thread {
        
        private final Envelope envelope;
        private final AMQP.BasicProperties properties;
        private final byte[] body;
        
        public WorkerConnection(Envelope envelope,
                    AMQP.BasicProperties properties, byte[] body) {
            this.envelope = envelope;
            this.properties = properties;
            this.body = body;
        }
        
        @Override
        public void run() {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .build();
            
            String response = "";
            try {
                String fileLocation = new String(body,"UTF-8");
                response = taskMaster.scanFileForVirus(fileLocation);
            } catch (RuntimeException | UnsupportedEncodingException ex){
                LOGGER.error(ex);
            } finally {
                try {
                    channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (UnsupportedEncodingException ex) {
                    LOGGER.error(ex);
                } catch (IOException ex) {
                    LOGGER.error(ex);
                }
            }
        }
        
    }
}
