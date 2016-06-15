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
 * ObjectCache
 * 23.02.2012
 *
 */
package com.innoq.liqid.utils;

import com.innoq.liqid.model.Node;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjectCache {
	
	private ObjectCache() {}

    private final static Logger LOG = Logger.getLogger(ObjectCache.class.getName());

    /**
     * Loads a Node from Cache File.
     * @param filename
     * @return Node that was loaded.
     */
    public static Node loadNodeCache(String filename) {
        Node node = null;
        File cacheFile = new File(Configuration.getVersionedFilename(filename));
        if (cacheFile.exists()) {
            FileInputStream fileIn = null;
            try {
                fileIn = new FileInputStream(Configuration.getVersionedFilename(filename));
                ObjectInputStream obj_in = new ObjectInputStream(fileIn);
                Object obj = obj_in.readObject();
                if (obj instanceof Node) {
                    node = (Node) obj;
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
        return node;
    }

    /**
     * Loads a Set of Nodes from Cache File.
     * @param filename
     * @return Set of Nodes that were loaded.
     */
    public static Set<Node> loadNodesCache(String filename) {
        Set<Node> nodes = null;
        File cacheFile = new File(Configuration.getVersionedFilename(filename));
        if (cacheFile.exists()) {
            FileInputStream fileIn = null;
            try {
                fileIn = new FileInputStream(Configuration.getVersionedFilename(filename));
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                Object obj = objectIn.readObject();
                if (obj instanceof Set) {
                    nodes = (Set<Node>) obj;
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
        return nodes;
    }

    /**
     * Saves a Set of Nodes to a Cache File.
     * @param filename
     */
    public static void saveNodesCache(Set<Node> nodes, String filename) {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(Configuration.getVersionedFilename(filename));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(nodes);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileOut.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Saves a Node to a Cache File.
     * @param filename
     */
    public static void saveNodeCache(Node node, String filename) {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(Configuration.getVersionedFilename(filename));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(node);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileOut.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
}
