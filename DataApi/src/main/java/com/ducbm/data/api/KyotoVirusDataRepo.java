/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.data.api;

import kyotocabinet.DB;

/**
 *
 * @author ducbm
 */
public class KyotoVirusDataRepo implements VirusDataRepository {
    
    private DB kyotoDB;
    
    public KyotoVirusDataRepo() {
        kyotoDB = new DB();
        kyotoDB.open("virus_db.kch", DB.OWRITER | DB.OCREATE); //create if not existed
    }
    
    public void close() {
        kyotoDB.close();
    }

    @Override
    public String selectOne(String sha256) {
        return kyotoDB.get(sha256);
    }

    @Override
    public void save(String sha256, String data) {
        kyotoDB.set(sha256, data);
    }

    @Override
    public void delete(String sha256) {
        kyotoDB.remove(sha256);
    }
    
}
