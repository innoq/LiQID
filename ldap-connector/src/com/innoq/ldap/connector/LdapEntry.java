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
