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
 * Log
 * 16.12.2011
 *
 */
package com.innoq.liqid.log;

/**
 * Interface for a basic Log-Representation.
 */
public interface Log {
    /**
     * Write something to the Log.
     * @param message
     */
    public abstract void write(String message, Class caller);
    /**
     * Close the Log.
     */
    public void close();
    /**
     * Returns the Log (e.g. /tmp/logs/123324234.log).
     * @return the Log.
     */
    public String getSource();
}
