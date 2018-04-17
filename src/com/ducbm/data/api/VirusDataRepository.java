/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.data.api;

/**
 *
 * @author ducbm
 */
public interface VirusDataRepository {
    
    // select data for a file, return data as JSON string
    String selectOne(String sha256);
    
    // save a new JSON string
    void save(String sha256, String data);
    
    // delete data for a file
    void delete(String sha256);
    
}
