/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.serverchat.utils;

/**
 *
 * @author ducbm
 */
public class ScannerResult {
    
    private String sha256;
    private String file;
    private String result;
    private boolean ack = false;

    public ScannerResult() {
    }

    public ScannerResult(String sha256, String file, String result) {
        this.sha256 = sha256;
        this.file = file;
        this.result = result;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }
    
}
