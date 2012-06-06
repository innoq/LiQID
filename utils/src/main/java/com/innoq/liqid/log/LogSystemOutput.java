/**
 * LogSystemOutput
 * 06.06.2012
 * @author Philipp Haussleiter
 *
 */
package com.innoq.liqid.log;

public class LogSystemOutput implements Log {

    public void write(String message, Class caller) {
        System.out.println("==> " + caller.getName() + ": " + message+"\n");
    }

    public void close() {
        System.out.println("==> closing");
    }

    public String getSource() {
        return "System.out";
    }
}
