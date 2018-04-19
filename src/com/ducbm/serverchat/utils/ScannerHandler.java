/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.serverchat.utils;

import com.ducbm.commonutils.Checksum;
import com.ducbm.servercheckvirus.remote.RPCClient;
import com.ducbm.servercheckvirus.remote.RPCClientImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author ducbm
 */
public class ScannerHandler {
    
    private static final Logger LOGGER =
            LogManager.getLogger(ScannerHandler.class.getCanonicalName());
    
    private static final ScannerHandler instance = new ScannerHandler();
    
    private final HashMap<String, ScannerResult> resultMap;
    private final ExecutorService executor;
    private final HashMap<String, Thread> threads;
    
    private ScannerHandler() {
        resultMap = new HashMap<>();
        executor = Executors.newFixedThreadPool(10);
        threads = new HashMap<>();
    }
    
    public static ScannerHandler getInstance() {
        return instance;
    }
    
    public String startScanFileForVirus(String fileLocation) {
        String sha256 = Checksum.sha256(fileLocation);
        String uuid = UUID.randomUUID().toString();
        ScannerResult result = new ScannerResult(sha256, fileLocation, null);
        resultMap.put(uuid, result);
        
        Thread scanner = new ScanVirusThread(uuid);
        executor.execute(scanner);
        threads.put(uuid, scanner);
        
        return uuid;
    }
    
    public ScannerResult getScanResult(String uuid, boolean waitForRes) {
        if (resultMap.get(uuid) == null) {
            return null;
        }
        ScannerResult res;
        if (!waitForRes || resultMap.get(uuid).getResult() != null) {
            res = resultMap.get(uuid);
        } else {
            Thread scanner = threads.get(uuid);
            // wait for thread complete
            
            synchronized (scanner) {
                try {
                    scanner.wait();
                    res = resultMap.get(uuid);
                } catch (InterruptedException ex) {
                    LOGGER.error(ex);
                    res = resultMap.get(uuid);
                    if (res.getResult() == null) {
                        res.setResult("{status: 0}");
                    }
                }
            }
        }
        return res;
    }
    
    private class ScanVirusThread extends Thread {
        
        private final String uuid;
        
        public ScanVirusThread(String uuid) {
            this.uuid = uuid;
        }
        
        @Override
        public void run() {
            try {
                RPCClient client = new RPCClientImpl();
                String scanResult = client.scanFileForVirus(resultMap.get(uuid).getFile());
                resultMap.get(uuid).setResult(scanResult);
            } catch (IOException | TimeoutException e) {
                LOGGER.error(e);
                // show error flag
                resultMap.get(uuid).setResult("{status: 0}");
            }
            threads.remove(this.uuid);
            synchronized (ScanVirusThread.this) {
                ScanVirusThread.this.notifyAll();
            }
        }
    }
}
