/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.ducbm.data.api.hash;

import java.util.HashMap;

/**
 *
 * @author ducbm
 */
public class LRUHashCache {
    
    private int capacity;
    private HashMap<String, Node> map = new HashMap<>();
    private Node head = null;
    private Node end = null;
    
    public LRUHashCache(int capacity) {
        this.capacity = capacity;
    }
    
    public String get(String key) {
        if(map.containsKey(key)){
            Node n = map.get(key);
            remove(n);
            setHead(n);
            return n.value;
        }
        
        return null;
    }
    
    public void remove(Node n) {
        if (n.pre != null) {
            n.pre.next = n.next;
        } else {
            head = n.next;
        }
        
        if (n.next!=null) {
            n.next.pre = n.pre;
        } else {
            end = n.pre;
        }
        
    }
    
    public void setHead(Node n) {
        n.next = head;
        n.pre = null;
        
        if(head != null)
            head.pre = n;
        
        head = n;
        
        if(end == null)
            end = head;
    }
    
    public void set(String key, String value) {
        if (map.containsKey(key)) {
            Node old = map.get(key);
            old.value = value;
            remove(old);
            setHead(old);
        } else {
            Node created = new Node(key, value);
            if(map.size() >= capacity){
                map.remove(end.key);
                remove(end);
                setHead(created);
            } else {
                setHead(created);
            }
            map.put(key, created);
        }
    }
    
}
