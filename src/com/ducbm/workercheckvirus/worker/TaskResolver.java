/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.workercheckvirus.worker;

import com.ducbm.commonutils.Checksum;
import com.ducbm.data.remote.DataRPCClient;
import com.ducbm.data.remote.DataRPCClientImpl;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import org.json.JSONObject;

/**
 *
 * @author ducbm
 */
public class TaskResolver {
    
    public static String doTask(String fileLocation) throws IOException {
        
        String sha256 = Checksum.sha256(fileLocation);
        String result;
        DataRPCClient client = null;
        
        // check file on db
        String lastRequestResult = "";
        try {
            client = new DataRPCClientImpl();
            lastRequestResult =  client.selectOne(sha256);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        
        if ("".equals(lastRequestResult)) {
            StringBuilder commandBuider = new StringBuilder();
            commandBuider.append("clamdscan ");
            commandBuider.append(fileLocation);
            System.out.println(" [*] Run command: " + commandBuider.toString());
            
            String scanResult = doCommandLineTask(commandBuider.toString());
            String[] lines = scanResult.split("\\n");
            
            boolean infected = false;
            for (String line: lines) {
                if (line.contains("Infected files:")) {
                    infected = Integer.valueOf(line.substring(16)) > 0;
                    break;
                }
            }
            
            JSONObject json = new JSONObject();
            json.put("sha256", sha256);
            json.put("file", fileLocation);
            json.append("virus_status", new JSONObject().put("clamav", infected));
            json.put("updated", new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
            System.out.println(" [*] Virus scan result: " + json.toString());
            
            result = json.toString();
            client.save(sha256, result);
        } else {
            JSONObject json = new JSONObject(lastRequestResult);
            result = json.toString();
        }
        return result;
    }
    
    private static String doCommandLineTask(String command) throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        Scanner s = new Scanner(p.getInputStream()).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }
    
}
