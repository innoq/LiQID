/*
Copyright (C) 2012 innoQ Deutschland GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.innoq.liqid.model;

import java.util.Set;

/**
 * Model of a Node.
 * 10.12.2011
 */
public interface Node {

    /**
     * Returns the Name of a Node (e.g. uid for User, cn for Group).
     *
     * @return the name.
     */
    String getName();

    /**
     * Returns the distinguished name of the Node.
     *
     * @return the DN.
     */
    String getDn();

    /**
     * Sets the distinguished name of the Node.
     * @param dn the DN.
     */
    void setDn(String dn);

    /**
     * Returns a value for a given Key of that node.
     *
     * @param key the key.
     * @return the value.
     */
    String get(String key);

    /**
     * Sets a value of that node.
     *
     * @param key the key.
     * @param value the value.
     */
    void set(String key, String value);

    /**
     * Returns all keys that the Node has.
     *
     * @return a List of keys.
     */
    Set<String> getKeys();

    /**
     * Is the Node empty?
     *
     * @return true if the Node is empty, false otherwise.
     */
    boolean isEmpty();
}
