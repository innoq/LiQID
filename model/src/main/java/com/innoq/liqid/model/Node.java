package com.innoq.liqid.model;

import java.util.Set;

/**
 * Node
 * 10.12.2011
 * @author Philipp Haussleiter
 *
 */
public interface Node {

    public String getName();
    
    public String get(String key);

    public void set(String key, String value);

    public Set<String> getKeys();

    public boolean isEmpty();
}
