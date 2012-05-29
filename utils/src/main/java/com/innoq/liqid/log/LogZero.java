/**
 * LogZero
 * 10.05.2012
 * @author Philipp Haussleiter
 *
 */
package com.innoq.liqid.log;

/**
 * This Log is a Zero Logger. Just a Placeholder, if no Log is defined.
 * @author Philipp Haußleiter
 */
public class LogZero implements Log {
    public void write(String message, Class caller) {}
    public void close() {}
    public String getSource() {
        return "Zero";
    }
}
