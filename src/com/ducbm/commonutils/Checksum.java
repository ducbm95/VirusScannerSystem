/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.commonutils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 *
 * @author ducbm
 */
public class Checksum {
    
    public static final String sha256(String fileLocation) {
        try {
            InputStream stream = new FileInputStream(fileLocation);
            
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dataBytes = new byte[1024];
            
            int nread = 0;
            while ((nread = stream.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            byte[] mdbytes = md.digest();
            
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < mdbytes.length; i++) {
                hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
            }
            return hexString.toString();
        } catch(FileNotFoundException e) {
            return "File not found";
        }catch (Exception e) {
            return "Error";
        }
    }
    
}
