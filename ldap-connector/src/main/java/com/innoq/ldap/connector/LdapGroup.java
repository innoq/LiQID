/*
 Copyright (C) 2017 innoQ Deutschland GmbH

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
package com.innoq.ldap.connector;

import java.util.Set;
import java.util.TreeSet;

/**
 * LdapGroup 14.04.2011
 */
public class LdapGroup extends LdapNode implements Comparable<LdapGroup> {

    private Set<LdapUser> users = new TreeSet<>();

    public LdapGroup() {
        super();
    }

    /**
     * @param cn common name
     * @deprecated Don't use this method any more. This Constructor will use the
     * default LdapHelper instance.
     * see LdapGroup(String uid, LdapHelper Instance)
     */
    @Deprecated
    public LdapGroup(String cn) {
        this(cn, LdapHelper.getInstance());
    }

    /**
     * Creates a Group Object Instance within a specific LdapHelper instance.
     *
     * @param cn the common name
     * @param instance the LdapHelper instance
     */
    public LdapGroup(String cn, LdapHelper instance) {
        super();
        setCn(cn);
        this.name = cn;
        set(instance.getGroupIdentifyer(), cn);
    }

    /**
     * Get all users from this group.
     *
     * @return Set if Users.
     */
    public Set<LdapUser> getUsers() {
        return this.users;
    }

    /**
     * Adds an user to the group.
     *
     * @param user the user to be added
     */
    public void addUser(LdapUser user) {
        this.users.add(user);
    }

    /**
     * Removes an user from the group.
     *
     * @param user the user to be removed
     */
    public void rmUser(LdapUser user) {
        this.users.remove(user);
    }

    /**
     * Removes every user from the group.
     */
    public void clearUsers() {
        this.users.clear();
    }

    @Override
    public boolean isEmpty() {
        return attributes.size() < 3;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (get("cn") != null ? get("cn").hashCode() : 0);
        hash = 79 * hash + (this.attributes != null ? this.attributes.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LdapGroup other = (LdapGroup) obj;
        if ((get("cn") == null) ? (other.get("cn") != null) : !get("cn").equals(get("cn"))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(LdapGroup t) {
        if (t == null) {
            return 1;
        }
        if (getCn() == null && t.getCn() != null) {
        	return -1;
        }
        return getCn().compareTo(t.getCn());
    }

    @Override
    public String toString() {
        return get("cn") + " # " + attributes.size();
    }

    @Override
    public String toLdif(LdapHelper instance) {
        StringBuilder sb = new StringBuilder("version: 1\n\n");
        sb.append("dn: ").append(getDn()).append("\n");
        for (String oc : getObjectClasses()) {
            sb.append("objectClass: ").append(oc).append("\n");
        }

        for (LdapUser user : getUsers()) {
            sb.append(instance.getGroupMemberAttribut()).append(": ").append(user.getDn()).append("\n");
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
