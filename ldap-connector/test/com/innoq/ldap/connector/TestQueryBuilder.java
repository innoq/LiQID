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
import java.util.Set;
import com.innoq.liqid.model.QueryBuilder;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * TestQueryBuilder
 * 27.05.2012
 */
public class TestQueryBuilder {

    private static LdapHelper HELPER;
    private static final Logger LOG = Logger.getLogger(TestQueryBuilder.class.getName());
    private static String UID1;
    private static String UID2;
    private static String CN1;
    private static String CN2;

    @BeforeClass
    public static void setUpClass() throws Exception {
        HELPER = Utils.getHelper();
        UID1 = "U1_" + System.currentTimeMillis();
        UID2 = "U2_" + System.currentTimeMillis();
        CN1 = "G1_" + System.currentTimeMillis();
        CN1 = "G2_" + System.currentTimeMillis();
        LdapUser u1 = Utils.getTestUser(UID1);
        HELPER.setUser(u1);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Node u1 = HELPER.getUser(UID1);
        HELPER.rmUser(u1);
    }

    @Test
    public void testForSingleValue() {
        String expectedValues = "(objectClass=person)";
        QueryBuilder qb = new LdapQueryBuilder();
        qb.append("objectClass", "person");
        assertEquals(expectedValues, qb.getQuery());
    }

    @Test
    public void testForMultiValues() {
        String expectedValues = "(&(l=Berlin)(objectClass=person)(uid=U1_*))";
        QueryBuilder qb = new LdapQueryBuilder();
        qb.append("objectClass", "person");
        qb.append("l", "Berlin");
        qb.append("uid", "U1_*");
        assertEquals(expectedValues, qb.getQuery());
    }

    @Test
    public void testReplaceParameter() {
        String expectedValues = "(objectClass=*)";
        QueryBuilder qb = new LdapQueryBuilder();
        qb.append("objectClass", "person");
        qb.append("objectClass", "*");
        assertEquals(expectedValues, qb.getQuery());
    }

    @Test
    public void testSearchUsersByAttributes() {
        QueryBuilder qb = new LdapQueryBuilder();
        qb.append(HELPER.getUserIdentifyer(), UID1);
        Set<Node> users = HELPER.findUsers(qb);
        assertTrue(users.size() > 0);
    }

    // TODO finish stuff here....
    private static void preparesUsers() {
        LdapUser u1 = Utils.getTestUser(UID1);
        LdapUser u2 = Utils.getTestUser(UID2);
        String keys1[] = {"o", "sn", "l"};
    }
}
