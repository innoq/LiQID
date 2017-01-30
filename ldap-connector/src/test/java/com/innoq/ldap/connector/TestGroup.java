/*
 Copyright (C) 2016 innoQ Deutschland GmbH

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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TestGroup 11.12.2011
 */
public class TestGroup {

    private static LdapHelper HELPER;
    private static final Logger LOG = Logger.getLogger(TestUser.class.getName());
    private static String CN;
    private static LdapUser testUser1, testUser2;

    public TestGroup() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        HELPER = Utils.getHelper();
        CN = "G1_" + System.currentTimeMillis();
        testUser1 = HELPER.getUserTemplate("U1_" + System.currentTimeMillis());
        testUser1.set("cn", testUser1.getUid());
        HELPER.setUser(testUser1);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        HELPER.rmUser(testUser1);
        HELPER.rmUser(testUser2);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGroupLoad() {
        LdapGroup group = (LdapGroup) HELPER.getGroup(HELPER.getAdminGroupIdentifiyer());
        group.debug();
        LOG.log(Level.INFO, "\nmembers: {0}\n", group.get(HELPER.getGroupMemberAttribut()));
        assertFalse(group.isNew());
        assertFalse(group.isEmpty());
    }

    @Test
    public void testCreateGroup() throws Exception {
        Node n1 = HELPER.getGroup(CN);
        assertTrue(n1.isEmpty());
        LdapGroup g1 = Utils.getTestGroup("test");
        g1 = Utils.updatedGroup(g1, CN);
        if (HELPER.setGroup(g1)) {
            LOG.log(Level.INFO, "created Group {0}", CN);
        }
        n1 = HELPER.getGroup(CN);
        assertFalse(n1.isEmpty());
        Utils.removeTestGroup(g1);
    }

    @Test
    public void testCreateGroupWithMetadata() throws Exception {
    	LdapGroup g1 = Utils.createTestGroup(CN);
        String[] parts;
        g1 = (LdapGroup) HELPER.getGroup(CN);
        assertNotNull(g1.getModifiersName());
        parts = g1.getModifiersName().split("=");
        assertTrue(parts.length > 0);
        assertNotNull(g1.getEntryUUID());
        parts = g1.getEntryUUID().split("-");
        assertTrue(parts.length > 0);
        assertNotNull(g1.getModifyTimestamp());
        assertTrue(g1.getModifyTimestamp().contains("Z"));
        Utils.removeTestGroup(g1);   	
    }
    
    @Test
    public void testAddUserToGroup() throws Exception {
        LdapGroup g1 = Utils.createTestGroup(CN);
        g1.addUser(testUser1);
        if (HELPER.setGroup(g1)) {
            LOG.log(Level.INFO, "updated Group {0}", CN);
        }
        g1 = (LdapGroup) HELPER.getGroup(CN);
        g1.debug();
        Utils.removeTestGroup(g1);
    }

    @Test
    public void testRemoveUserFromGroup() throws Exception {
        LdapGroup g1 = Utils.createTestGroup(CN);
        String[] usernames = {"U2_" + System.currentTimeMillis(), "U3_" + System.currentTimeMillis()};
        if (HELPER.setGroup(g1)) {
            LOG.log(Level.INFO, "updated Group {0}", CN);
        }
        List<LdapUser> users = Utils.createTestUsers(usernames);
        for (LdapUser user : users) {
            g1.addUser(user);
        }
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup(CN);
        int count;
        count = g1.getUsers().size();
        LOG.log(Level.INFO, "getUsers() count Group {0} is {1}", new Object[]{CN, count});
        g1.debug();
        assertTrue(count > 1);
        
        g1.rmUser(users.get(0));
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup(CN);
        LOG.log(Level.INFO, "user count Group {0} is {1}", new Object[]{CN, g1.getUsers().size()});
        g1.debug();
        assertTrue(g1.getUsers().size() == (count - 1));
        Utils.removeTestGroup(g1);
        Utils.removeTestUsers(users);
    }

    @Test
    public void testAddUsersToGroup() throws Exception {
		testUser2 = HELPER.getUserTemplate("U4_" + System.currentTimeMillis());
        testUser2.set("cn", testUser2.getUid());
        HELPER.setUser(testUser2);
        LdapGroup g1 = Utils.createTestGroup(CN);
        g1.addUser(testUser1);
        g1.addUser(testUser2);
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup(CN);
        LOG.log(Level.INFO, "user count Group {0} is {1}", new Object[]{CN, g1.getUsers().size()});
        assertTrue(g1.getUsers().size() > 2);
        
        Set<Node> members = HELPER.getUsersForGroup(g1);
        LOG.log(Level.INFO, "getUsersForGroup() count Group {0} is {1}", new Object[]{CN, members.size()});
        g1.debug();
        assertTrue(members.size() > 2);
        
        Utils.removeTestGroup(g1);
    }

    @Test
    public void testUpdateGroup() throws Exception {
        LdapGroup g1 =Utils.createTestGroup(CN);
        assertNull(g1.get("description"));
        g1.set("description", "Group " + CN);
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup(CN);
        assertEquals(g1.get("description"), "Group " + CN);
        Utils.removeTestGroup(g1);
    }

    @Test
    public void testUpdateExistingGroup() throws Exception {
        LdapGroup g1 = (LdapGroup) HELPER.getGroup("users");
        LOG.log(Level.INFO, "Group users description: {0}", g1.get("description"));
        String description = g1.get("description");
        g1.set("description", description + CN);
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup("users");
        assertEquals(g1.get("description"), description + CN);
        g1.set("description", description);
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup("users");
        assertEquals(g1.get("description"), description);
    }

    @Test
    public void testUpdateEmptyGroup() throws Exception {
        LdapGroup g1 = Utils.createTestGroup(CN);
        int count = g1.getUsers().size();
        LOG.log(Level.INFO, "user count Group {0} is {1}", new Object[]{CN, count});
        g1.debug();
        List<LdapUser> users = new ArrayList<LdapUser>();
        for (LdapUser user : g1.getUsers()) {
            users.add(user);
        }
        for (LdapUser user : users) {
            g1.rmUser(user);
        }
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup(CN);
        count = g1.getUsers().size();
        LOG.log(Level.INFO, "user count Group {0} is {1}", new Object[]{CN, count});
        g1.debug();
        assertTrue(g1.getUsers().size() == 1);
        Utils.removeTestGroup(g1);
    }

    @Test
    public void testDeleteGroup() throws Exception {
        LdapGroup g1 = Utils.createTestGroup(CN);
        try {
            if (HELPER.rmGroup(g1)) {
                LOG.log(Level.INFO, "deleted Group {0}", CN);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "setGroup fails", ex);
        }
        g1 = (LdapGroup) HELPER.getGroup(CN);
        assertTrue(g1.isEmpty());
    }
}
