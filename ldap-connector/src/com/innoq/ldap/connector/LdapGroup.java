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
package com.innoq.ldap.connector;

import java.util.Set;
import java.util.TreeSet;

/**
 * Group
 * 14.04.2011
 * @author Philipp Haussleiter
 *
 */
public class LdapGroup extends LdapNode implements Comparable<LdapGroup> {

    private String cn;
    private Set<LdapUser> users = new TreeSet<LdapUser>();
    public LdapGroup() {
        super();
    }

    public LdapGroup(String cn) {
        super();
        this.cn = cn;
        this.name = cn;
        set("cn", cn);
        set("uid", cn);
    }

    /**
     * Get all users from this group.
     * @return Set if Users.
     */
    public Set<LdapUser> getUsers() {
        return this.users;
    }

    /**
     * Adds an user to the group.
     * @param user
     */
    public void addUser(LdapUser user) {
        this.users.add(user);
    }

    /**
     * Removes an user from the group.
     * @param user
     */
    public void rmUser(LdapUser user){
        this.users.remove(user);
    }
    
    /**
     * Removes every user from the group.
     */
    public void clearUsers(){
        this.users.clear();
    }

    public void setCn(String cn){
        this.cn = cn;
    }
    
    public String getCn() {
        return this.cn == null ? "" : this.cn;
    }

    @Override
    public boolean isEmpty(){
        return attributes.size() < 3;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.cn != null ? this.cn.hashCode() : 0);
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
        if ((this.cn == null) ? (other.cn != null) : !this.cn.equals(other.cn)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(LdapGroup t) {
        if(t == null){
            return 1;
        }
        return getCn().compareTo(t.getCn());
    }

    @Override
    public String toString() {
        return this.cn + " # " + attributes.size();
    }
}
