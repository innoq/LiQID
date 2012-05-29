/**
 * KeyValueStore
 * 14.02.2012
 * @author Philipp Haussleiter
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
        storage = new HashMap<String, String>();
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
     * @param filename the serialized KV-Store
     * @return the deserialized KV-Store, a new KV-Store, if file is invalid.
     */
    public static KeyValueStore loadKeyValueStore(String filename) {
        File cacheFile = new File(filename);
        if (cacheFile.exists()) {
            FileInputStream f_in = null;
            try {
                f_in = new FileInputStream(filename);
                ObjectInputStream obj_in = new ObjectInputStream(f_in);
                Object obj = obj_in.readObject();
                if (obj instanceof KeyValueStore) {
                    return (KeyValueStore) obj;
                }
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                try {
                    f_in.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
        return new KeyValueStore();
    }

    /**
     * Saves a KeyValueStore.
     * @param filename to serialize KV-Store.
     * @param store the KV-Store Object.
     * @return true if successful saved, false otherwise.
     */
    public static boolean saveKeyValueStore(String filename, KeyValueStore store) {
        FileOutputStream f_out = null;
        try {
            f_out = new FileOutputStream(filename);
            ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
            obj_out.writeObject(store);
            return true;
        } catch (FileNotFoundException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                f_out.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
}
