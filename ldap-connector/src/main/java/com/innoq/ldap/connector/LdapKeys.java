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

public class LdapKeys {

	
    public static final String ADMIN_GROUP_CN = "Administratoren";
    public static final String ATTR_OU_PEOPLE = ".ou_people";
    public static final String ASTERISK = "*";
    public static final String DEFAULT_JABBER_SERVER = "jabber.example.com";
    public static final String DEFAULT_SSH_KEY = "<!-- no key -->";
    public static final String ENTRY_CN_FORMAT = "%s=%s,%s";
    public static final String ENTRY_UUID = "entryUUID";
    public static final String GROUP_CN_FORMAT = "%s=%s,%s";
    public static final String GROUP_ID_ATTRIBTUE = "cn";
    public static final String GROUP_MEMBER_ATTRIBUTE = "member";
    public static final String GROUP_OBJECTCLASS = "groupOfNames";
    public static final String JPEG_PHOTO = "jpegPhoto";
    public static final String LDAP_ATTRIBUTES_BINARY = "java.naming.ldap.attributes.binary";
    public static final String MODIFIERS_NAME = "modifiersName";
    public static final String MODIFY_TIMESTAMP = "modifyTimestamp";
    public static final String OBJECT_CLASS = "objectClass";
    public static final String OWNER = "owner";
    public static final String USER_ID_ATTRIBUTE = "uid";
    public static final String USER_OBJECTCLASS = "person";
    public static final String USER_PASSWORD = "userPassword";
    public static final String USER_UID_FORMAT = "%s=%s,%s";
       
    private LdapKeys() {}
    
}
