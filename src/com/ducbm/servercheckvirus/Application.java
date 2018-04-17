/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.servercheckvirus;

import com.ducbm.servercheckvirus.remote.RPCServer;
import com.ducbm.servercheckvirus.remote.RPCServerImpl;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author ducbm
 */
public class Application {
    
    public static void main(String[] args) throws IOException, TimeoutException {
        RPCServer server = new RPCServerImpl();
        server.serveScanFileForVirus();
    }
    
}
