/**
 * LogConsole
 * 10.05.2012
 * @author Philipp Haussleiter
 *
 */
package com.innoq.liqid.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogConsole implements Log {

    private final static Logger LOG = Logger.getLogger(LogConsole.class.getName());

    public LogConsole(){
        LOG.info(">> starting...");
    }

    public void write(String message, Class caller) {
        LOG.log(Level.INFO, ">> {0} {1}", new Object[]{caller.getName(), message});
    }

    public void close() {
        LOG.info(">> closing...");
    }

    public String getSource() {
        return LOG.getName();
    }
}
