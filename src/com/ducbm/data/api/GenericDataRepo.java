/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.data.api;

import com.ducbm.data.api.kyoto.KyotoVirusDataRepo;
import com.ducbm.data.api.hash.HashmapVirusDataRepo;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ducbm
 */
public class GenericDataRepo implements VirusDataRepository {
    
    // list of cache, from higher level to lower level
    private final List<VirusDataRepository> listCache;
    
    public GenericDataRepo() {
        listCache = new ArrayList<>();
        listCache.add(new HashmapVirusDataRepo()); // cache level 0
        listCache.add(new KyotoVirusDataRepo()); // cache level 1
    }

    @Override
    public String selectOne(String sha256) {
        String result = null;
        for (int i = 0; i < listCache.size(); i++) {
            VirusDataRepository repo = listCache.get(i);
            result = repo.selectOne(sha256);
            if (result != null) {
                updateCacheForHigherLevel(i, sha256, result);
                break;
            }
        }
        return result;
    }

    @Override
    public void save(String sha256, String data) {
        listCache.forEach((repo) -> {
            repo.save(sha256, data);
        });
    }

    @Override
    public void delete(String sha256) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private void updateCacheForHigherLevel(int level, String sha256, String data) {
        for (int i = level - 1; i >= 0; i--) {
            listCache.get(i).save(sha256, data);
        }
    }
    
}
