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
import com.innoq.liqid.utils.Configuration;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * TestUser 11.12.2011
 */
public class TestUser {

    private static LdapHelper HELPER;
    private static final Logger LOGGER = Logger.getLogger(TestUser.class.getName());
    private static String UID;
    private static String PW;

    public TestUser() {
        HELPER = Utils.getHelper();
        UID = "UX_" + System.currentTimeMillis();
        PW = UID.substring(0, 5) + UID.substring(5);
        LOGGER.log(Level.INFO, "UID: {0}", UID);
    }

    @Test
    public void testCreateUser() throws Exception {
        //String uid = "newTestUser";
        Node u1 = HELPER.getUser(UID);
        assertTrue(u1.isEmpty());
        LdapUser t1 = Utils.getTestUser(UID);
        t1.setPassword(UID);
        t1 = Utils.updatedUser(t1, UID);
        assertTrue("testCreateUser " + UID + " for login failed", HELPER.setUser(t1));
        u1 = HELPER.getUser(UID);
        assertFalse(u1.isEmpty());
        Utils.removeTestUser(t1);
    }

    @Test
    public void testLoginWithNull() throws Exception {
        boolean login;
        LdapUser t1 = Utils.createTestUser(UID);
        login = HELPER.checkCredentials(UID, null);
        assertFalse("testValidLogin: should be false for " + UID + ":null", login);
        login = HELPER.checkCredentials(null, UID);
        assertFalse("testValidLogin: should be false for null:" + UID, login);
        login = HELPER.checkCredentials(null, null);
        assertFalse("testValidLogin: should be false for null:null", login);
        Utils.removeTestUser(t1);
    }

    @Test
    public void testValidLogin() throws Exception {
        LdapUser t1 = Utils.createTestUser(UID);
        t1.setPassword(UID);
        assertTrue("testValidLogin " + UID + " for login failed", HELPER.setUser(t1));
        boolean login = HELPER.checkCredentials(UID, UID);
        assertTrue("testValidLogin: should be true for " + UID, login);
        assertTrue(Utils.removeTestUser(t1));
    }

    @Test
    public void testInvalidLogin() {
        boolean login = HELPER.checkCredentials(UID, "test");
        assertFalse("testInvalidLogin: should be false " + UID, login);
    }

    @Test
    public void testUserLoad() throws Exception {
        LdapUser testUser = Utils.createTestUser(UID);
        LdapUser ldapUser = (LdapUser) HELPER.getUser(UID);
        LOGGER.log(Level.INFO, "testUser: {0}", testUser);
        LOGGER.log(Level.INFO, "ldapUser: {0}", ldapUser);
        assertFalse("User should be not new!", ldapUser.isNew());
        assertTrue("Differences: " + Utils.compare(testUser, ldapUser), testUser.equals(ldapUser));
        assertTrue(Utils.removeTestUser(testUser));
    }

    @Test
    public void testAlterUser() throws Exception {
        LdapUser user1 = Utils.createTestUser(UID);
        user1.set("description", "altered Test User");
        user1.set("sn", "Test User");
        assertTrue("testAlterUser " + UID + " fails", HELPER.setUser(user1));
        LdapUser user2 = (LdapUser) HELPER.getUser(UID);

        assertTrue("should be \n 'altered Test User' but was \n " + user2.get("description"), "altered Test User".equals(user2.get("description")));
        assertTrue("should be \n 'Test User' but was \n " + user2.get("sn"), "Test User".equals(user2.get("sn")));
        assertTrue(Utils.removeTestUser(user1));
    }

    @Test
    public void testAlterPassword() throws Exception {
        LdapUser user1 = Utils.createTestUser(UID);
        user1.setPassword("test2");
        assertTrue("testAlterPassword " + UID + " fails", HELPER.setUser(user1));
        boolean login = HELPER.checkCredentials(UID, "test2");
        assertTrue("should be true", login);
        assertTrue(Utils.removeTestUser(user1));
    }

    @Test
    public void testAlterPasswordAsUser() throws Exception {
        boolean login;
        String pass1, pass2;
        pass1 = "test2";
        pass2 = "test3";
        LdapUser user1 = Utils.createTestUser(UID);
        user1.setPassword(pass1);
        assertTrue("testAlterPasswordAsUser=>setUser " + UID + " fails", HELPER.setUser(user1));
        login = HELPER.checkCredentials(UID, pass1);
        assertTrue("should be true", login);
        user1.setPassword(pass2);
        assertTrue("testAlterPasswordAsUser=>setUserAsUser " + UID + " fails", HELPER.setUserAsUser(user1, UID, pass1));
        login = HELPER.checkCredentials(UID, pass2);
        assertTrue("should be true", login);
        assertTrue(Utils.removeTestUser(user1));
    }

    @Test
    public void testAvatar() throws Exception {
    	LdapUser user1 = Utils.createTestUser(UID);
    	user1 = (LdapUser) HELPER.getUser(UID);
    	assertNull(user1.getAvatar());
    	String dummyPath = Utils.generatePath("src", "test", "resources")+"dummy.png";
    	File avatar1 = Utils.getFile(dummyPath);
    	user1.setAvatar(avatar1);
    	HELPER.setUser(user1);
    	user1 = (LdapUser) HELPER.getUser(UID);
    	File avatar2 = user1.getAvatar();
    	assertEquals(avatar1.length(), avatar2.length());
    	assertTrue(Utils.compareFiles(avatar1, avatar2));
    	assertTrue(Utils.removeTestUser(user1));
    }
    
    @Test
    public void testUpdateUser() throws Exception {
        LdapUser u1 = Utils.createTestUser(UID);
        assertNull(u1.get("o"));
        u1.set("description", "Company for  " + UID);
        HELPER.setUser(u1);
        u1 = (LdapUser) HELPER.getUser(UID);
        assertEquals(u1.get("description"), "Company for  " + UID);
        assertTrue(Utils.removeTestUser(u1));
    }

    @Test
    public void testDeleteUser() throws Exception {
        LdapUser u1 = Utils.createTestUser(UID);
        assertFalse(u1.isEmpty());
        assertTrue("deleted User " + UID + " failed!", HELPER.rmUser(u1));
        u1 = (LdapUser) HELPER.getUser(UID);
        assertTrue(u1.isEmpty());
        Utils.removeTestUser(u1);
    }
    
    @Test
    public void testPrincipalDN() {
    	String principalDN = Configuration.getProperty(HELPER.getInstanceName() + ".principal").trim();
    	Node principal = HELPER.getPrincipal();
    	assertEquals(principalDN, principal.get("dn"));
    	LOGGER.log(Level.INFO, "Node: principal matches: {0} == {1}", new String[]{principalDN, principal.get("dn")});
    	LdapUser pUser = (LdapUser) principal;
    	assertEquals(principalDN, pUser.getDn());
    	LOGGER.log(Level.INFO, "User: principal matches: {0} == {1}", new String[]{principalDN, pUser.getDn()});
    }
    
    @Test
    public void testUserDN() throws Exception {
        LdapUser testUser = Utils.createTestUser(UID);
        LdapUser ldapUser = (LdapUser) HELPER.getUser(UID);
        assertEquals(testUser.getDn(), ldapUser.getDn());
    	LOGGER.log(Level.INFO, "User getDN(): User matches: {0} == {1}", new String[]{testUser.getDn(), ldapUser.getDn()});
        assertEquals(testUser.get("dn"), ldapUser.get("dn"));
    	LOGGER.log(Level.INFO, "User get(\"dn\"): User matches: {0} == {1}", new String[]{testUser.get("dn"), ldapUser.get("dn")});
    	Utils.removeTestUser(testUser);
    }    
}
