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
