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
package com.innoq.liqid.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Configuration
 * 14.04.2011
 * @author Philipp Haussleiter
 *
 */
public class Configuration {

    public final static String SEP = System.getProperty("file.separator");
    private final static String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static Configuration instance = new Configuration();
    private String tmpDir = null;
    private String cacheDir = null;
    private Properties properties = null;
    private String filename = System.getProperty("user.home") + SEP + ".liqid" + SEP + "liqid.properties";

    public static String getProperty(String key) {
        if (instance.properties == null) {
            instance.loadProperties();
        }
        return instance.properties.getProperty(key);
    }

    public static String getProperty(String key, String def) {
        if (instance.properties == null || instance.properties.getProperty(key) == null) {
            return def;
        }
        return instance.properties.getProperty(key, def);
    }

    public static void setProperty(String key, String value) {
        if (instance.properties != null) {
            if (value == null) {
                instance.properties.remove(key);
            } else {
                instance.properties.setProperty(key, value);
            }
        }
    }

    public String getTmpDir() {
        if (tmpDir == null) {
            tmpDir = getProperty("tmp", TMP_DIR);
        }
        return tmpDir;
    }

    public String getCacheDir() {
        return cacheDir != null ? cacheDir : getTmpDir();
    }

    public boolean checkProperties() {
        File propFile = new File(filename);
        return propFile.exists();
    }

    public String getFilename() {
        return filename;
    }

    public void setTmpDir(String tmpDir) {
        this.tmpDir = tmpDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public static Configuration getInstance() {
        return instance;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    private void loadProperties() {
        try {
            properties = new Properties();
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(filename));
            properties.load(stream);
            stream.close();
        } catch (Exception ex) {
            System.out.println(filename + " was not found!");
        }
    }

    public void setPropertiesFile(String filename) {
        this.filename = filename;
        loadProperties();
    }
}
