LiQID
=====
LDAP innoQ ID Manager

# Abstract

This is Library should help java developers to use LDAP-Directories in a simple way. Without the hazzle of JNDI (Java Naming and Directory Interface).

# Model
TBD

Diagram needs to be updated.

# Utils
TBD

# LDAP-Connector

Building:

You need a proper Configuration in a properties file.
There are 3 possibilities to define the location of this file:

1. via Code with setting Configuration.setPropertiesFile(....) before you instantiate the LDAPHelper.
2. store the file in the default location: user_dir/.liqid/liqid.properties
3. use the Enviroment Variabel LIQID_PROPERTIES to set the location of the properties file.

## Configuration
This Lib needs to have some proper Configuration to work. You can use a separate Properties File or you can add the necessary Entries to your existing Java Application Configuration. 

> $ java -jar ldap-connector-1.3-SNAPSHOT.jar -generate

will create an example liqid.properties:

    # example liqid.properties
    # created 10.07.13 14:40
    
	# LDAP Settings
	
	## LDAP Listing, divided by ","
	ldap.listing=ldap1
	## Default LDAP (with leading information)
	default.ldap=ldap1
	
	## Mandatory Configuration:
	ldap.user.objectClasses=person
	ldap.group.objectClasses=groupOfUniqueNames
	
	### OUs for this LDAP instances
	ldap1.ou_people=ou=users
	ldap1.ou_group=ou=roles
	
	### ldap|ldaps :// <host>:<port>
	ldap1.url=ldap://localhost:389
	ldap1.principal=dc=Manager,dc=example,dc=com
	ldap1.credentials=password
	ldap1.base_dn=dc=example,dc=com
	
	## Optional Configuration:
	### User ID Attribute - DEFAULT: uid
	# ldap1.user.id.attribute=cn
	
	### User ObjectClass - DEFAULT: person
	# ldap1.user.object.class=person
	
	### Group ID Attribute - DEFAULT: cn
	# ldap1.group.id.attribute=cn
	
	### Group ObjectClass - DEFAULT: groupOfNames
	# ldap1.group.object.class=groupOfUniqueNames
	
	### Group Member Attribute - DEFAULT: member
	# ldap1.group.member.attribute=uniqueMember


You can change the location of that file manually via

    Configuration.setPropertiesLocation("/var/apps/config/liqid.properties");
    
The Default location is __~/.liqid/liqid.properties__    

### Loading a User from the LDAP Directory

    public void testUserLoad() {
        // loads an user with uid "test" from LDAP
        LdapUser ldapUser = (LdapUser) HELPER.getUser("test");

        // loads a preset test-user
        LdapUser testUser = getTestUser();
        
        // compares both users
        LOG.log(Level.INFO, "testUser: {0}", testUser);
        LOG.log(Level.INFO, "ldapUser: {0}", ldapUser);
        assertTrue("should be test: " + testUser + " ldap: " + ldapUser, testUser.equals(ldapUser));
    }
	
### Creating a new User in the LDAP Directory

    public void testUserCreate() {
        LdapUser user = HELPER.getUserTemplate("foobar");
        try {
            if (HELPER.setUser(user)) {
                LOG.log(Level.INFO, "created User {0}", user.getName());
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "setUser fails", ex);
        }
        user = HELPER.getUser("foobar");
        assertFalse(user.isEmpty());
    }	

# Legal

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
