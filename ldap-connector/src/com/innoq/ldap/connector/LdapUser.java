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

import com.innoq.liqid.model.Node;
import com.innoq.liqid.utils.SHACrypt;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;

/**
 * LdapUser
 * 04.12.2011
 */
public class LdapUser extends LdapNode implements Comparable<LdapUser> {

    /** Users uid. */
    private String uid = "";
    private Set<LdapGroup> groups = null;
    private String password = null;
    private File avatar;

    public LdapUser() {
        super();
    }

    public LdapUser(String uid) {
        super();
        this.uid = uid;
        this.name = uid;
        set("uid", uid);
    }

    public Set<LdapGroup> getGroups() {
        if (this.groups == null) {
            this.groups = new TreeSet<LdapGroup>();
            for(Node n : LdapHelper.getInstance().getGroupsForUser(this)){
                this.groups.add((LdapGroup) n);
            }
        }
        return this.groups;
    }

    public void setPassword(String password) {
        this.password = SHACrypt.encrypt(password);
        this.attributes.put("userPassword", "{SHA}" + this.password);
    }

    public String getPassword() {
        return password;
    }

    public File getAvatar() {
        return avatar;
    }

    public void setAvatar(File avatar) {
        this.avatar = avatar;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return this.uid == null ? "" : this.uid;
    }

    @Override
    public boolean isEmpty(){
        return attributes.size() < 2;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.uid != null ? this.uid.hashCode() : 0);
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
        final LdapUser other = (LdapUser) obj;
        if ((this.uid == null) ? (other.uid != null) : !this.uid.equals(other.uid)) {
            return false;
        }
        for(String key : this.getKeys()){
            if(!this.get(key).equals(other.get(key))){
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(LdapUser t) {
        if(t == null){
            return 1;
        }
        return getUid().compareTo(t.getUid());
    }

    @Override
    public String toString() {
        return getUid() + " # " + attributes.size();
    }
}
