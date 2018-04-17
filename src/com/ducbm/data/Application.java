/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.data;

import com.ducbm.data.remote.DataRPCServer;
import com.ducbm.data.remote.DataRPCServerImpl;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author ducbm
 */
public class Application {
    
    public static void main(String[] args) throws IOException, TimeoutException {
        DataRPCServer rPCServer = new DataRPCServerImpl();
        rPCServer.serveForDataRPCServer();
    }
    
}
