/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.data.remote;

/**
 *
 * @author ducbm
 */
public interface DataRPCServer {
    
    int REQUEST_SELECT_ONE = 0;
    int REQUEST_SAVE = 1;
    int REQUEST_DELELE = 2;
    
    void serveForDataRPCServer();
    
}
