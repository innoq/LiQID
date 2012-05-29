/**
 * Log
 * 16.12.2011
 * @author Philipp Haussleiter
 *
 */
package com.innoq.liqid.log;

/**
 * Interface for a basic Log-Representation.
 * @author Philipp Haußleiter
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
