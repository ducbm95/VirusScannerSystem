/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.data.api.hash;

import com.ducbm.data.api.VirusDataRepository;

/**
 *
 * @author ducbm
 */
public class HashmapVirusDataRepo implements VirusDataRepository {
    
    private LRUHashCache lruHashCache;
    
    public HashmapVirusDataRepo() {
        lruHashCache = new LRUHashCache(100);
    }

    @Override
    public String selectOne(String sha256) {
        System.out.println(" [*] Select from HashMap repo for sha256: " + sha256);
        return lruHashCache.get(sha256);
    }

    @Override
    public void save(String sha256, String data) {
        System.out.println(" [*] Save to HashMap repo for sha256: " + sha256);
        lruHashCache.set(sha256, data);
    }

    @Override
    public void delete(String sha256) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
