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
 * @author Philipp Haussleiter
 *
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

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
        String expectedValues = "(&(l=Monheim am Rhein)(objectClass=person)(uid=ph*))";
        QueryBuilder qb = new LdapQueryBuilder();
        qb.append("objectClass", "person");
        qb.append("l", "Monheim am Rhein");
        qb.append("uid", "ph*");
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
        String l = "Monheim am Rhein";
        QueryBuilder qb = new LdapQueryBuilder();
        qb.append("l", l);
        Set<Node> users = HELPER.findUsers(qb);
        assertTrue(users.size() > 0);
    }

    private static void preparesUsers() {
        LdapUser u1 = Utils.getTestUser(UID1);
        LdapUser u2 = Utils.getTestUser(UID2);
        String keys1[] = {"o", "sn", "l"};
    }
}
