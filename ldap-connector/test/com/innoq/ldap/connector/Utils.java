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

import com.innoq.liqid.log.LogConsole;
import com.innoq.liqid.model.Node;
import com.innoq.liqid.utils.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utils 27.05.2012
 */
public class Utils {

    private static LdapHelper HELPER;
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    private static String UID;

    public static LdapHelper getHelper() {
        if (HELPER == null) {
            Configuration.getInstance().setTmpDir("target");
            HELPER = LdapHelper.getInstance();
            HELPER.setLog(new LogConsole());
            showConfigInfo();
        }
        return HELPER;
    }

    public static LdapUser updatedUser(LdapUser user, String name) {
        List<String> ocs = Arrays.asList(HELPER.getUserObjectClasses());
        user.setUid(name);
        user.setPassword(name);
        user.set(HELPER.getUserIdentifyer(), name);
        if (ocs.contains("posixAccount")) {
            user.set("gecos", "User " + name);
            user.set("cn", name);
        }
        return user;
    }

    public static Map<String, String> getTestUserAttributes(String uid) {
        List<String> ocs = Arrays.asList(HELPER.getUserObjectClasses());
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("sn", uid.toUpperCase());
        attributes.put(HELPER.getUserIdentifyer(), uid);
        attributes.put("description", uid);

        if (ocs.contains("inetOrgPerson")) {
            attributes.put("displayName", "Test2 Test1");
            attributes.put("givenName", uid + "2");
        }
        if (ocs.contains("posixAccount")) {
            attributes.put("cn", uid);
            attributes.put("uidNumber", "3");
            attributes.put("gidNumber", "3");
            attributes.put("homeDirectory", "/home/" + uid);
            attributes.put("gecos", "User " + uid);
            attributes.put("loginShell", "/bin/bash");
        }

        if (ocs.contains("shadowAccount")) {
            attributes.put("shadowInactive", "0");
            attributes.put("shadowLastChange", "15055");
            attributes.put("shadowMax", "99999");
            attributes.put("shadowWarning", "7");
        }

        if (ocs.contains("inetOrgPerson")) {
            attributes.put("mail", "test@test.com");
            attributes.put("l", "Berlin");
            attributes.put("mobile", "+12345678910");
            attributes.put("postalAddress", "a Street 123 17$12345 a City$NRW$Germany");
            attributes.put("postalCode", "12345");
            attributes.put("street", "a Street 123");
        }

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
        group.set(HELPER.getGroupIdentifyer(), name);
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
        LOG.log(Level.INFO, "\n{0}", user.toLdif(HELPER));
        return user;
    }

    public static List<LdapUser> createTestUsers(String[] names) throws Exception {
        LdapUser user;
        List<LdapUser> users = new ArrayList<LdapUser>();
        for (String name : names) {
            user = HELPER.getUserTemplate(name);
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
        LOG.log(Level.INFO, "\n{0}", group.toLdif(HELPER));
        return group;
    }

    private static void showConfigInfo() {
        StringBuilder sb = new StringBuilder("\n");
        sb.append("------------------------------------------------------------------------\n");
        sb.append("CONFIGURATION:\n");
        sb.append("------------------------------------------------------------------------\n");
        sb.append(" Version:\t").append(Configuration.getInstance().getVersion()).append("\n");
        sb.append(" File:\t").append(Configuration.getInstance().getFilename()).append("\n");
        sb.append(" Tmp-Dir:\t").append(Configuration.getInstance().getTmpDir()).append("\n");
        sb.append(" Cache-Dir:\t").append(Configuration.getInstance().getCacheDir()).append("\n");
        List<String> ocs;
        ocs = Arrays.asList(HELPER.getUserObjectClasses());
        sb.append(" User oc List:\t").append(ocs.toString()).append("\n");
        ocs = Arrays.asList(HELPER.getGroupObjectClasses());
        sb.append(" Group oc List:\t").append(ocs.toString()).append("\n");
        sb.append("------------------------------------------------------------------------\n");
        LOG.log(Level.INFO, sb.toString());
    }

    public static void removeTestGroup(LdapGroup g1) {
        HELPER.rmGroup(g1);
    }

    public static LdapGroup createTestGroup(String cn) throws Exception {
        LdapGroup g1 = HELPER.getGroupTemplate(cn);
        HELPER.setGroup(g1);
        return g1;
    }
    
    public static LdapUser createTestUser(String uid) throws Exception{
        LdapUser u1 = HELPER.getUserTemplate(uid);
        HELPER.setUser(u1);
        return u1;
    }
    
    public static boolean removeTestUser(LdapUser u1) {
        return HELPER.rmUser(u1);
    }
}
