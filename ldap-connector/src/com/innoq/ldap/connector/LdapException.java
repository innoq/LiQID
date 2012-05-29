/**
 * LdapException
 * 22.05.2012
 * @author Philipp Haussleiter
 *
 */
package com.innoq.ldap.connector;

import com.innoq.liqid.model.Node;

public class LdapException extends RuntimeException {

    public LdapException(String msg, Throwable thrwbl){
        super(msg, thrwbl);
    }

    public LdapException(Node node, Throwable thrwbl) {
        super(thrwbl);
        if (node instanceof LdapGroup) {
            ((LdapGroup) node).debug();
        }
        if (node instanceof LdapUser) {
            ((LdapUser) node).debug();
        }
    }
}
