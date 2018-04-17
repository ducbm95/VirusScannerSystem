/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ducbm.data.api.hash;

/**
 *
 * @author ducbm
 */
public class Node {
    
    public String key;
    public String value;
    
    public Node pre;
    public Node next;
    
    public Node(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
}
