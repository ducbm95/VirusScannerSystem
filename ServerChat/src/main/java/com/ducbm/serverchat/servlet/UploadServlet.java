/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.serverchat.servlet;

import com.ducbm.serverchat.remote.RPCClient;
import com.ducbm.serverchat.remote.RPCClientImpl;
import hapax.Template;
import hapax.TemplateDictionary;
import hapax.TemplateException;
import hapax.TemplateLoader;
import hapax.TemplateResourceLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.io.FileUtils;

import org.eclipse.jetty.http.HttpStatus;

/**
 *
 * @author ducbm
 */
public class UploadServlet extends HttpServlet {
    
    public static final String UPLOAD_DIR = "/home/ducbm/Work/workspace/upload_dir/";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            TemplateLoader templateLoader = TemplateResourceLoader.create("views/");
            Template template = templateLoader.getTemplate("index.xtm");
            TemplateDictionary templeDictionary = new TemplateDictionary();
            String responseTxt = template.renderToString(templeDictionary);
            
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println(responseTxt);
        } catch (TemplateException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            e.printStackTrace(); // print logs here
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Part filePart = request.getPart("file");
            String fileName = Paths.get(filePart.getSubmittedFileName())
                    .getFileName().toString();
            InputStream fileContent = filePart.getInputStream();
            saveFile(UPLOAD_DIR + fileName, fileContent);
            String scanRes = scanFileForVirus(UPLOAD_DIR + fileName);
            
            response.setStatus(HttpStatus.OK_200);
            response.getWriter().println(scanRes);
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            e.printStackTrace(); // print logs here
        }
    }
    
    private void saveFile(String fileLocation, InputStream fileContent)
            throws Exception {
        File targetFile = new File(fileLocation);
        FileUtils.copyInputStreamToFile(fileContent, targetFile);
        System.out.println(sha256(fileLocation));
    }
    
    private String scanFileForVirus(String fileLocation) {
        RPCClient client = new RPCClientImpl();
        String scanResult = client.scanFileForVirus(fileLocation);
        return scanResult;
    }
    
    private static final String sha256(String fileLocation) {
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
            return "error";
        }
    }
}
