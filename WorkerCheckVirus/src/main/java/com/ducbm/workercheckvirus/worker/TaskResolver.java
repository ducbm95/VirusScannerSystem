/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.workercheckvirus.worker;

import com.ducbm.commonutils.Checksum;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import org.json.JSONObject;

/**
 *
 * @author ducbm
 */
public class TaskResolver {
    
    public static String doTask(String fileLocation) throws IOException {
        StringBuilder commandBuider = new StringBuilder();
        commandBuider.append("clamscan ");
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
        String sha256 = Checksum.sha256(fileLocation);
        
        JSONObject json = new JSONObject();
        json.put("sha256", sha256);
        json.put("file", fileLocation);
        json.append("virus_status", new JSONObject().put("clamav", infected));
        json.put("updated", new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime()));
        System.out.println(" [*] Virus scan result: " + json.toString());
        
        return json.toString();
    }
    
    private static String doCommandLineTask(String command) throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        Scanner s = new Scanner(p.getInputStream()).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }
    
}
