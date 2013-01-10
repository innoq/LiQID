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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TestGroup
 * 11.12.2011
 */
public class TestGroup {

    private static LdapHelper HELPER;
    private static final Logger LOG = Logger.getLogger(TestUser.class.getName());
    private static String CN = "Administratoren";
    private static LdapUser testUser;

    public TestGroup() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        HELPER = Utils.getHelper();
        CN = "G_" + System.currentTimeMillis();
        testUser = HELPER.getUserTemplate("U_" + System.currentTimeMillis());
        testUser.set("cn", testUser.getUid());
        HELPER.setUser(testUser);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        HELPER.rmUser(testUser);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGroupLoad() {
        LdapGroup group = (LdapGroup) HELPER.getGroup("Administratoren");
        group.debug();
        LOG.log(Level.INFO, "\nmembers: {0}\n", group.get("member"));
        assertFalse(group.isNew());
        assertFalse(group.isEmpty());
    }

    @Test
    public void testCreateGroup() throws Exception {
        Node g1 = HELPER.getGroup(CN);
        assertTrue(g1.isEmpty());
        LdapGroup t1 = Utils.getTestGroup("test");
        t1 = Utils.updatedGroup(t1, CN);
        if (HELPER.setGroup(t1)) {
            LOG.log(Level.INFO, "created Group {0}", CN);
        }
        g1 = HELPER.getGroup(CN);
        assertFalse(g1.isEmpty());

    }

    @Test
    public void testAddUserToGroup() throws Exception {
        LdapGroup g1 = (LdapGroup) HELPER.getGroup(CN);
        g1.addUser(testUser);
        if (HELPER.setGroup(g1)) {
            LOG.log(Level.INFO, "updated Group {0}", CN);
        }
        g1 = (LdapGroup) HELPER.getGroup(CN);
        g1.debug();
    }

    @Test
    public void testRemoveUserFromGroup() throws Exception {
        LdapGroup g1 = (LdapGroup) HELPER.getGroup(CN);
        String[] usernames = {"U1_" + System.currentTimeMillis(), "U2_" + System.currentTimeMillis()};
        if (HELPER.setGroup(g1)) {
            LOG.log(Level.INFO, "updated Group {0}", CN);
        }
        List<LdapUser> users = Utils.createTestUsers(usernames);
        for (LdapUser user : users) {
            g1.addUser(user);
        }
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup(CN);
        int count = g1.getUsers().size();
        LOG.log(Level.INFO, "user count Group {0} is {1}", new Object[]{CN, count});
        g1.debug();
        assertTrue(count > 1);
        g1.rmUser(users.get(0));
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup(CN);
        LOG.log(Level.INFO, "user count Group {0} is {1}", new Object[]{CN, g1.getUsers().size()});
        g1.debug();
        assertTrue(g1.getUsers().size() == (count - 1));
        Utils.removeTestUsers(users);
    }

    @Test
    public void testUpdateGroup() throws Exception {
        LdapGroup g1 = (LdapGroup) HELPER.getGroup(CN);
        assertNull(g1.get("description"));
        g1.set("description", "Group " + CN);
        HELPER.setGroup(g1);
        g1 = (LdapGroup) HELPER.getGroup(CN);
        assertEquals(g1.get("description"), "Group " + CN);
    }

    @Test
    public void testUpdateEmptyGroup() throws Exception {
        LdapGroup g1 = (LdapGroup) HELPER.getGroup(CN);
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
    }

    @Test
    public void testDeleteGroup() {
        LdapGroup g1 = (LdapGroup) HELPER.getGroup(CN);
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
