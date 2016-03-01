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
/**
 * KeyValueStore 14.02.2012
 *
 */
package com.innoq.liqid.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyValueStore implements Serializable {

    private final static Logger LOG = Logger.getLogger(KeyValueStore.class.getName());
    private Map<String, String> storage;

    public KeyValueStore() {
        storage = new HashMap<>();
    }

    public void set(String key, String value) {
        storage.put(key, value);
    }

    public String get(String key) {
        if (storage.containsKey(key)) {
            return storage.get(key);
        }
        return "";
    }

    public void remove(String key) {
        storage.remove(key);
    }

    /**
     * Loads a KeyValueStore from a given File
     *
     * @param filename the serialized KV-Store
     * @return the deserialized KV-Store, a new KV-Store, if file is invalid.
     */
    public static KeyValueStore loadKeyValueStore(String filename) {
        File cacheFile = new File(Configuration.getVersionedFilename(filename));
        if (cacheFile.exists()) {
            FileInputStream fileIn = null;
            try {
                fileIn = new FileInputStream(Configuration.getVersionedFilename(filename));
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                Object obj = objectIn.readObject();
                if (obj instanceof KeyValueStore) {
                    return (KeyValueStore) obj;
                }
            } catch (ClassNotFoundException|IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fileIn.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
        return new KeyValueStore();
    }

    /**
     * Saves a KeyValueStore.
     *
     * @param filename to serialize KV-Store.
     * @param store the KV-Store Object.
     * @return true if successful saved, false otherwise.
     */
    public static boolean saveKeyValueStore(String filename, KeyValueStore store) {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(Configuration.getVersionedFilename(filename));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(store);
            return true;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileOut.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
}
