/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.data.api.kyoto;

import com.ducbm.commonutils.AppConfiguration;
import com.ducbm.commonutils.Constants;
import com.ducbm.data.api.VirusDataRepository;
import kyotocabinet.DB;

/**
 *
 * @author ducbm
 */
public class KyotoVirusDataRepo implements VirusDataRepository {
    
    private final DB kyotoDB;
    
    public KyotoVirusDataRepo() {
        String dbName = AppConfiguration.getConfigInstance()
                .getString(Constants.CONFIG_ATTR_DATA_API_KYOTO_NAME);
        
        kyotoDB = new DB();
        kyotoDB.open(dbName, DB.OWRITER | DB.OCREATE); //create if not existed
    }
    
    public void close() {
        kyotoDB.close();
    }

    @Override
    public String selectOne(String sha256) {
        System.out.println(" [*] Select from Kyoto repo for sha256: " + sha256);
        return kyotoDB.get(sha256);
    }

    @Override
    public void save(String sha256, String data) {
        System.out.println(" [*] Save to Kyoto repo for sha256: " + sha256);
        kyotoDB.set(sha256, data);
    }

    @Override
    public void delete(String sha256) {
        kyotoDB.remove(sha256);
    }
    
}
