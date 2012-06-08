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
 * LogFile
 * 16.12.2011
 * @author Philipp Haussleiter
 *
 */
package com.innoq.liqid.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogFile implements Log {

    BufferedOutputStream bos;
    File logFile;

    public LogFile(String logFilePath) {
        try {
            logFile = new File(logFilePath);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            bos = new BufferedOutputStream(new FileOutputStream(logFilePath));
        } catch (IOException ex) {
            Logger.getLogger(LogFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void write(String message, Class caller) {
        try {
            bos.write(message.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(LogFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        try {
            bos.flush();
            bos.close();
        } catch (IOException ex) {
            Logger.getLogger(LogFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getSource() {
        return logFile.getName();
    }
}
