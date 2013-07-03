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
 * LdapNode 10.12.2011
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

    private static final String LINE = "------------------------------------------------------------------------\n";
    protected BasicAttributes attributes = new BasicAttributes(false);
    protected Set<String> keys = null;
    protected Set<String> objectClasses = null;
    protected String name;
    protected String dn = null;
    protected String modifyTimestamp = null;
    protected String modifiersName = null;
    protected String cn = null;

    /**
     * Returns an entry for a given key.
     *
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
     *
     * @return true it this Node is empty, false otherwise.
     */
    public boolean isEmpty() {
        return true;
    }

    /**
     * Sets the value of an entry.
     *
     * @param key the key of that entry.
     * @param value the new value for that entry.
     */
    public void set(String key, String value) {
        if (value != null
                && !value.isEmpty()
                && key != null
                && !key.isEmpty()) {
            addAttribute(new BasicAttribute(key, value, true));
        }
    }

    /**
     * Returns all BasicAttributes of that Node
     *
     * @see javax.naming.directory.BasicAttributes.
     * @return all BasicAttributes.
     */
    public BasicAttributes getAttributes() {
        return attributes;
    }

    /**
     * Sets (overwrites) all BasicAttributes of that Node.
     *
     * @param attributes the new Attributes of that node.
     */
    public void setAttributes(BasicAttributes attributes) {
        this.attributes = attributes;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getCn() {
        return this.cn == null ? "" : this.cn;
    }

    public boolean isNew() {
        return (this.modifiersName == null && this.modifyTimestamp == null);
    }

    public void debug() {
        StringBuilder sb = new StringBuilder("\n");
        sb.append(LINE);
        sb.append("\t").append("dn").append(" : ").append(getDn()).append("\n");
        if (!isNew()) {
            sb.append("\tupdated: ").append(this.modifyTimestamp).append(" by ").append(this.modifiersName).append("\n");
        }
        sb.append(LINE);
        for (String key : getKeys()) {
            sb.append("\t");
            sb.append(key);
            sb.append(" : ");
            sb.append(get(key));
            sb.append("\n");
        }
        sb.append(LINE);
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

    public void addObjectClass(final String objectClass) {
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

    public void setDn(final String dn) {
        this.dn = dn;
    }

    public void setModifyTimestamp(final String modifyTimestamp) {
        this.modifyTimestamp = modifyTimestamp.trim();
    }

    public void setModifiersName(final String modifiersName) {
        this.modifiersName = modifiersName.trim();
    }

    public String getModifiersName() {
        return this.modifiersName;
    }

    public String getModifyTimestamp() {
        return this.modifyTimestamp;
    }

    public String toLdif(LdapHelper instance) {
        StringBuilder sb = new StringBuilder("version: 1\n\n");
        sb.append("dn: ").append(getDn()).append("\n");
        for (String oc : getObjectClasses()) {
            sb.append("objectClass: ").append(oc).append("\n");
        }
        for (String key : getKeys()) {
            if (!"objectClass".equals(key)) {
                sb.append(key).append(": ").append(get(key)).append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
