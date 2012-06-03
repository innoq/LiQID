package com.innoq.ldap.connector;

import com.innoq.liqid.log.LogConsole;
import com.innoq.liqid.model.Node;
import com.innoq.liqid.utils.Configuration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TestUtils
 * 27.05.2012
 * @author Philipp Haussleiter
 *
 */
public class Utils {

    private static LdapHelper HELPER = null;
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    private static String UID;

    public static LdapHelper getHelper() {
        if (HELPER == null) {
            Configuration.getInstance().setPropertiesFile("test/test.ldap.properties");
            Configuration.getInstance().setTmpDir("target");
            HELPER = LdapHelper.getInstance();
            HELPER.setLog(new LogConsole());
        }
        return HELPER;
    }

    public static LdapUser updatedUser(LdapUser user, String name) {
        user.setUid(name);
        user.setPassword(name);
        user.set("cn", name);
        user.set("uid", name);
        user.set("gecos", "User " + name);
        return user;
    }

    public static Map<String, String> getTestUserAttributes(String uid) {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("cn", uid);
        attributes.put("gidNumber", "3");
        attributes.put("homeDirectory", "/home/" + uid);
        attributes.put("sn", uid.toUpperCase());
        attributes.put("uid", uid);
        attributes.put("uidNumber", "3");
        attributes.put("description", uid);
        attributes.put("displayName", "Test2 Test1");
        attributes.put("gecos", "User " + uid);
        attributes.put("givenName", uid + "2");
        attributes.put("l", "Ratingen");
        attributes.put("loginShell", "/bin/bash");
        attributes.put("mail", "test@test.com");
        attributes.put("mobile", "+12345678910");
        attributes.put("postalAddress", "a Street 123 17$12345 a City$NRW$Germany");
        attributes.put("postalCode", "12345");
        attributes.put("shadowInactive", "0");
        attributes.put("shadowLastChange", "15055");
        attributes.put("shadowMax", "99999");
        attributes.put("shadowWarning", "7");
        attributes.put("street", "a Street 123");
        return attributes;
    }

    public static String compare(Node n1, Node n2) {
        StringBuilder sb = new StringBuilder("\n");
        sb.append(n1.getName());
        sb.append(":");
        sb.append(n2.getName());
        sb.append("\n");
        for (String key : n1.getKeys()) {
            sb.append(key);
            sb.append(": ");
            sb.append(n1.get(key));
            sb.append(": ");
            sb.append(n2.get(key));
            sb.append("\n");
        }
        return sb.toString();
    }

    public static LdapUser addAttributes(LdapUser user, String uid) {
        Map<String, String> attributes = getTestUserAttributes(uid);
        for (String key : attributes.keySet()) {
            user.set(key, attributes.get(key));
        }
        return user;
    }

    public static LdapGroup updatedGroup(LdapGroup group, String name) {
        group.setCn(name);
        group.set("cn", name);
        group.set("uid", name);
        return group;
    }

    public static void resetTestUser(String uid) {
        try {
            LdapUser testUser = getTestUser(uid);
            testUser.setPassword(uid);
            HELPER.setUser(testUser);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "setUser fails", ex);
        }
    }

    public static LdapUser getTestUser(String uid) {
        LdapUser user = HELPER.getUserTemplate(uid);
        user = Utils.addAttributes(user, uid);
        return user;
    }

    public static List<LdapUser> createTestUsers(String[] names) throws Exception {
        LdapUser user;
        List<LdapUser> users = new ArrayList<LdapUser>();
        for (String name : names) {
            user = HELPER.getUserTemplate(name);
            user.set("cn", user.getUid());
            HELPER.setUser(user);
            users.add(user);
        }
        return users;
    }

    public static void removeTestUsers(List<LdapUser> users) {
        for (LdapUser user : users) {
            HELPER.rmUser(user);
        }
    }

    public static LdapGroup getTestGroup(String cn) {
        LdapGroup group = HELPER.getGroupTemplate(cn);
        group.debug();
        return group;
    }
}
