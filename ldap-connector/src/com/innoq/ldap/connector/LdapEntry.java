/**
 * LdapEntry 02.03.2013 consolving.de
 *
 * @author Philipp Haussleiter<philipp@consolving.de>
 *
 */
package com.innoq.ldap.connector;

public class LdapEntry extends LdapNode implements Comparable<LdapEntry> {

    public String owner;

    public LdapEntry(String cn, String owner) {
        super();
        this.cn = cn;
        this.owner = owner;
    }

    public String getOwner() {
        return this.owner;
    }

    public int compareTo(LdapEntry t) {
        if (t == null) {
            return 1;
        }
        if(t.owner == null){
            return 1;
        }
        return getCn().compareTo(t.getCn()) * owner.compareTo(t.getOwner());
    }
}
