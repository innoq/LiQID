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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.innoq.liqid.utils.Configuration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * TestUser
 * 11.12.2011
 */
public class TestUser {

    private static LdapHelper HELPER;
    private static final Logger LOG = Logger.getLogger(TestUser.class.getName());
    private static String UID;

    public TestUser() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        HELPER = Utils.getHelper();
        UID = "U_" + System.currentTimeMillis();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateUser() {
        //String uid = "newTestUser";
        Node u1 = HELPER.getUser(UID);
        assertTrue(u1.isEmpty());
        LdapUser t1 = Utils.getTestUser(UID);
        t1 = Utils.updatedUser(t1, UID);
        try {
            if (HELPER.setUser(t1)) {
                LOG.log(Level.INFO, "created User {0}", UID);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "setUser fails", ex);
        }
        u1 = HELPER.getUser(UID);
        assertFalse(u1.isEmpty());
    }

    @Test
    public void testValidLogin() {
        boolean login = HELPER.checkCredentials(UID, UID);
        assertTrue("testValidLogin: should be true", login);
    }

    @Test
    public void testInvalidLogin() {
        boolean login = HELPER.checkCredentials(UID, "test");
        assertFalse("testInvalidLogin: should be false", login);
    }

    @Test
    public void testUserLoad() {
        LdapUser ldapUser = (LdapUser) HELPER.getUser(UID);
        LdapUser testUser = Utils.getTestUser(UID);
        LOG.log(Level.INFO, "testUser: {0}", testUser);
        LOG.log(Level.INFO, "ldapUser: {0}", ldapUser);
        assertTrue("Differences: " + Utils.compare(testUser, ldapUser), testUser.equals(ldapUser));
    }

    @Test
    public void testAlterUser() {
        LdapUser user1 = Utils.getTestUser(UID);
        user1.set("givenName", "Testname");
        user1.set("mail", "test@example.com");
        try {
            HELPER.setUser(user1);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "setUser fails", ex);
        }
        LdapUser user2 = (LdapUser) HELPER.getUser(UID);
        assertTrue(user1.equals(user2));
        Utils.resetTestUser(UID);
    }

    @Test
    public void testAlterPassword() {
        LdapUser user1 = Utils.getTestUser(UID);
        user1.setPassword("test2");
        try {
            HELPER.setUser(user1);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "setUser fails", ex);
        }
        boolean login = HELPER.checkCredentials(UID, "test2");
        assertTrue("should be true", login);
        Utils.resetTestUser(UID);
    }
    @Test
    public void testUpdateUser() throws Exception {
        Node u1 = HELPER.getUser(UID);
        assertNull(u1.get("o"));
        u1.set("description", "Company for  " + UID);
        HELPER.setUser(u1);
        u1 = HELPER.getUser(UID);
        assertEquals(u1.get("description"), "Company for  " + UID);
    }
    
    @Test
    public void testDeleteUser() {
        Node u1 = HELPER.getUser(UID);
        assertFalse(u1.isEmpty());
        try {
            if (HELPER.rmUser(u1)) {
                LOG.log(Level.INFO, "deleted User {0}", UID);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "setUser fails", ex);
        }
        u1 = HELPER.getUser(UID);
        assertTrue(u1.isEmpty());
    }

    @Test
    public void testDefaultConfig() {
        String jabberServer = Configuration.getProperty("ldap.jabberServer");
        String sshKey = Configuration.getProperty("sshKey");
        Configuration.setProperty("ldap.jabberServer", null);
        Configuration.setProperty("ldap.sshKey", null);
        HELPER.reload();
        LdapUser u1 = HELPER.getUserTemplate(UID);
        assertEquals(UID + "@" + LdapHelper.DEFAULT_JABBER_SERVER, u1.get("jabberID"));
        assertEquals(LdapHelper.DEFAULT_SSH_KEY, u1.get("sshPublicKey"));
        Configuration.setProperty("ldap.jabberServer", jabberServer);
        Configuration.setProperty("ldap.sshKey", sshKey);
        HELPER.reload();
    }
}
