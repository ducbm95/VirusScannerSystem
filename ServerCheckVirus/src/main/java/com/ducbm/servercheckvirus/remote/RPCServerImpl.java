/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.servercheckvirus.remote;

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
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ducbm
 */
public class RPCServerImpl implements RPCServer {
    
    private static final String RPC_HOST = "localhost";
    private static final int RPC_PORT = 8100;
    private static final String RPC_QUEUE_NAME = "SCAN_VIRUS_SERVICE";
    
    private final Connection connection;
    private final Channel channel;
    
    public RPCServerImpl() throws TimeoutException, IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RPC_HOST);
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channel.basicQos(10);
        System.out.println(" [x] Awaiting RPC requests");
    }
    
    @Override
    public void serveScanFileForVirus() {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                    AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                
                System.out.println(" [*] Received a request.");
                MyThread thread = new MyThread(envelope, properties, body);
                thread.start();
            }
        };
        try {
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        } catch (IOException ex) {
            Logger.getLogger(RPCServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
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
    }
    
    public void close() {
        if (connection != null)
            try {
                connection.close();
            } catch (IOException _ignore) {}
    }
    
    public class MyThread extends Thread {
        
        Envelope envelope;
        AMQP.BasicProperties properties;
        byte[] body;
        
        public MyThread(Envelope envelope,
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
                TaskMaster task = new TaskMasterImpl();
                response = task.scanFileForVirus(fileLocation);
            } catch (RuntimeException e){
                System.out.println(" [.] " + e.toString());
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(RPCServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(RPCServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(RPCServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                                    // RabbitMq consumer worker thread notifies the RPC server owner thread
//                synchronized(this) {
//                    this.notify();
//                }
            }
        }
        
    }
}
