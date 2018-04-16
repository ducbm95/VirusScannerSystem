/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.workercheckvirus;

import com.ducbm.workercheckvirus.worker.Worker;
import com.ducbm.workercheckvirus.worker.WorkerImpl;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author ducbm
 */
public class Application {
    
    public static void main(String[] args) throws IOException, TimeoutException {
        Worker worker = new WorkerImpl();
        worker.waitForTaskScanVirus();
    }
    
}
