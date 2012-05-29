/**
 * Node
 * 10.12.2011
 * @author Philipp Haussleiter
 *
 */
package com.innoq.ldap.connector;

import com.innoq.liqid.model.Node;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.naming.NamingEnumeration;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

public class LdapNode implements Node {

    protected BasicAttributes attributes = new BasicAttributes(false);
    protected Set<String> keys = null;
    protected Set<String> objectClasses = null;
    protected String name;
    protected String dn = null;

    /**
     * Returns an entry for a given key.
     * @param key the key for the entry.
     * @return the value of the entry, null if the entry does not exists.
     */
    public String get(String key) {
        if (getKeys().contains(key)) {
            return attributes.get(key).toString().replace(key + ":", "").trim();
        }
        return null;
    }

    /**
     * Basic Constructor.
     */
    public LdapNode() {
        objectClasses = new HashSet<String>();
    }

    /**
     * There might be always a root entry (uid or cn).
     * @return true it this Node is empty, false otherwise.
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * Sets the value of an entry.
     * @param key the key of that entry.
     * @param value the new value for that entry.
     */
    public void set(String key, String value) {
        if (value != null
                && !value.isEmpty()
                && key != null
                && !key.isEmpty()) {
            attributes.put(new BasicAttribute(key, value, true));
        }
    }

    /**
     * Returns all BasicAttributes of that Node @see javax.naming.directory.BasicAttributes.
     * @return all BasicAttributes.
     */
    public BasicAttributes getAttributes() {
        return attributes;
    }

    /**
     * Sets (overwrites) all BasicAttributes of that Node.
     * @param attributes the new Attributes of that node.
     */
    public void setAttributes(BasicAttributes attributes) {
        this.attributes = attributes;
    }

    public void debug() {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("\t").append("dn").append(" : ").append(getDn()).append("\n");
        sb.append("============================================================\n");
        for (String key : getKeys()) {
            sb.append("\t");
            sb.append(key);
            sb.append(" : ");
            sb.append(get(key));
            sb.append("\n");
        }
        sb.append("============================================================\n");
        Logger.getLogger(LdapNode.class.getName()).info(sb.toString());
    }

    public void addAttribute(BasicAttribute attribute) {
        this.attributes.put(attribute);
        this.keys = null;
    }

    public BasicAttribute getAttribute(String key) {
        if (getKeys().contains(key)) {
            return (BasicAttribute) attributes.get(key);
        }
        return null;
    }

    public Set<String> getKeys() {
        if (keys == null) {
            keys = new HashSet<String>();
            NamingEnumeration<String> attrkeys = attributes.getIDs();
            while (attrkeys.hasMoreElements()) {
                keys.add((String) attrkeys.nextElement());
            }
        }
        return keys;
    }

    public Set<String> getObjectClasses() {
        return objectClasses;
    }

    public void addObjectClass(String objectClass) {
        this.objectClasses.add(objectClass);
    }

    public String getName() {
        return name;
    }

    public String getDn() {
        if (dn == null) {
            dn = LdapHelper.getInstance().getDNForNode(this);
        }
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }
}
