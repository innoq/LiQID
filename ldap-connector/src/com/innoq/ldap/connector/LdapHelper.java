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

import com.innoq.liqid.log.Log;
import com.innoq.liqid.log.LogZero;
import com.innoq.liqid.model.Helper;
import com.innoq.liqid.model.Node;
import com.innoq.liqid.model.QueryBuilder;
import com.innoq.liqid.utils.Configuration;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.ImageIcon;

/**
 * LdapHelper 14.04.2011
 */
public class LdapHelper implements Helper {

    private static final String MODIFY_TIMESTAMP = "modifyTimestamp";
    private static final String MODIFIERS_NAME = "modifiersName";
    private static final String ASTERISK = "*";
    private static final String MEMBER = "member";
    private static final String OBJECT_CLASS = "objectClass";
    private static final String JPEG_PHOTO = "jpegPhoto";
    private static final String USER_PASSWORD = "userPassword";
    private static final String UID = "uid";
    private static final String CN = "cn";
    private static final String PERSON = "person";
    private static final String GROUP_OF_NAMES = "groupOfNames";
    private static final String OWNER = "owner";
    private Log log = new LogZero();
    private Node principal = null;
    private DirContext ctx = null;
    private String baseDn;
    private String basePeopleDn;
    private String baseGroupDn;
    private Map<String, String> defaultValues = new HashMap<String, String>();
    private static Map<String, LdapHelper> helpers = new HashMap<String, LdapHelper>();
    private boolean online = false;
    private String instance;
    private long queryCount;
    private long modificationCount;
    private long validationCount;
    private long creationCount;
    private long deletionCount;
    private final static String GROUP_CN_FORMAT = "cn=%s,%s";
    private final static String USER_UID_FORMAT = "uid=%s,%s";
    private final static String ENTRY_CN_FORMAT = "cn=%s,%s";
    private final static String DEFAULT_JABBER_SERVER = "jabber.example.com";
    private final static String DEFAULT_SSH_KEY = "<!-- no key -->";

    public static LdapHelper getInstance() {
        String defaultLdap = Configuration.getProperty("default.ldap");
        return getInstance(defaultLdap);
    }

    /**
     * Returns a new Instance of LdapHelper.
     *
     * @return a new Instance.
     */
    public static LdapHelper getInstance(String instance) {
        if (!helpers.containsKey(instance)) {
            helpers.put(instance, new LdapHelper(instance));
        }
        LdapHelper helper = helpers.get(instance);
        if (!helper.online) {
            helper.loadProperties();
        }
        return helper;
    }

    /**
     * Returns a Listing of all LDAPs from the properties file (ldap.listing).
     *
     * @return the Listing as String Array.
     */
    public static String[] getLdaps() {
        String ldapListing = Configuration.getProperty("ldap.listing");
        String[] ldaps = ldapListing.split(",");
        return ldaps;
    }

    /**
     * Returns an Instance of the LdapHelper.
     *
     * @param instance
     */
    public LdapHelper(String instance) {
        this.instance = instance;
        checkDirs();
    }

    /**
     * Sets the Log of that LdapHelper.
     *
     * @param log the Log writer to use.
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Writes the modifications on an user object back to the LDAP.
     *
     * @param user the modfied user.
     * @return true if the user was set correct, false otherwise.
     */
    public boolean setUser(final Node node) throws Exception {
        LdapUser newLdapUser = (LdapUser) node;
        try {
            LdapUser oldLdapUser = (LdapUser) getUser(newLdapUser.getUid());
            if (oldLdapUser.isEmpty()) {
                newLdapUser.setPassword("!" + System.currentTimeMillis());
                log.write("bind: " + getOuForNode(newLdapUser) + "\n", LdapHelper.class);
                ctx.bind(getOuForNode(newLdapUser), null, newLdapUser.getAttributes());
                creationCount++;
            } else {
                ModificationItem[] mods = buildModificationsForUser(newLdapUser, oldLdapUser);
                if (mods.length > 0) {
                    log.write("modifyAttributes: " + getOuForNode(newLdapUser) + "\n", LdapHelper.class);
                    ctx.modifyAttributes(getOuForNode(newLdapUser), mods);
                    modificationCount++;
                }
            }
            return true;
        } catch (NamingException ex) {
            throw new LdapException(newLdapUser, ex);
        } finally {
            return false;
        }
    }

    /**
     * Deletes an LDAP-User.
     *
     * @param node of the LDAP-User to be deleted.
     * @return true if User was deleted, otherwise false.
     */
    public boolean rmUser(final Node node) {
        LdapUser user = (LdapUser) node;
        try {
            deletionCount++;
            ctx.unbind(getOuForNode(user));
        } catch (NamingException ex) {
            throw new LdapException(user, ex);
        }
        Node ldapUser = getUser(user.getUid());
        if (ldapUser.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Deletes a LDAP-Group.
     *
     * @param node of the LDAP-Group to be deleted.
     * @return true if Group was deleted, otherwise false.
     */
    public boolean rmGroup(Node node) {
        LdapGroup group = (LdapGroup) node;
        try {
            deletionCount++;
            ctx.unbind(getOuForNode(group));
        } catch (NamingException ex) {
            throw new LdapException(group, ex);
        }
        Node ldapGroup = getGroup(group.getCn());
        if (ldapGroup.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds or updates a LDAP-Group.
     *
     * @param node of the LDAP-Group to be set.
     * @return true if the Group was added/updated, otherwise false.
     * @throws Exception
     */
    public boolean setGroup(Node node) throws Exception {
        LdapGroup newLdapGroup = (LdapGroup) node;
        try {
            newLdapGroup = updateGroupMembers(newLdapGroup);
            LdapGroup oldLdapGroup = (LdapGroup) getGroup(newLdapGroup.getCn());
            if (oldLdapGroup.isEmpty()) {
                creationCount++;
                log.write("bind: " + getOuForNode(newLdapGroup) + "\n", LdapHelper.class);
                ctx.bind(getOuForNode(newLdapGroup), null, newLdapGroup.getAttributes());
            } else {
                ModificationItem[] mods = buildModificationsForGroup(newLdapGroup, oldLdapGroup);
                if (mods.length > 0) {
                    modificationCount++;
                    log.write("modifyAttributes: " + getOuForNode(newLdapGroup) + "\n", LdapHelper.class);
                    ctx.modifyAttributes(getOuForNode(newLdapGroup), mods);
                }
            }
            return true;
        } catch (NamingException ex) {
            throw new LdapException(newLdapGroup, ex);
        } finally {
            return false;
        }
    }

    public boolean setEntry(Node node) throws Exception {
        LdapEntry newLdapEntry = (LdapEntry) node;
        try {
            LdapEntry oldLdapEntry = (LdapEntry) getEntry(newLdapEntry.getCn(), newLdapEntry.getOwner());
            if (oldLdapEntry.isEmpty()) {
                creationCount++;
                log.write("bind: " + getOuForNode(newLdapEntry) + "\n", LdapHelper.class);
                ctx.bind(getOuForNode(newLdapEntry), null, newLdapEntry.getAttributes());
            } else {
                ModificationItem[] mods = buildModificationsForEntry(newLdapEntry, oldLdapEntry);
                if (mods.length > 0) {
                    modificationCount++;
                    log.write("modifyAttributes: " + getOuForNode(newLdapEntry) + "\n", LdapHelper.class);
                    ctx.modifyAttributes(getOuForNode(newLdapEntry), mods);
                }
            }
            return true;
        } catch (NamingException ex) {
            throw new LdapException(newLdapEntry, ex);
        } finally {
            return false;
        }
    }

    /**
     * Returns an Sub-Entry of an LDAP User/LDAP Group.
     * @param cn of that Entry.
     * @param owner DN of Parent Node.
     * @return a new Entry.
     */
    public Node getEntry(final String cn, final String owner){
        // TODO implement me!
        Node entry = new LdapEntry(cn, owner); 
        return entry;
    }
    
    /**
     * Returns an LDAP-User.
     *
     * @param uid the uid of the User.
     * @see com.innoq.liqid.model.Node#getName().
     * @return the Node of that User, either filled (if User was found), or
     * empty.
     */
    public Node getUser(final String uid) {
        Node user = new LdapUser(uid);
        try {
            String query = "(&(objectClass=person)(uid=" + uid + "))";
            SearchResult searchResult = null;
            Attributes attributes = null;
            SearchControls controls = new SearchControls();
            controls.setReturningAttributes(new String[]{ASTERISK, MODIFY_TIMESTAMP, MODIFIERS_NAME});
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search("", query, controls);
            queryCount++;
            if (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                user = fillAttributesInUser((LdapUser) user, attributes);
            }
        } catch (NamingException ex) {
            throw new LdapException(instance + ":" + uid, ex);
        }
        return user;
    }

    /**
     * Returns several LDAP-Users for a given search-String.
     *
     * @param uid the uid or part of uid of the Users.
     * @return Users as a Set of com.innoq.liqid.model.Node.
     */
    public Set<Node> findUsers(final String uid) {
        QueryBuilder qb = new LdapQueryBuilder();
        if (uid.equals("*")) {
            qb.append(OBJECT_CLASS, PERSON);
        } else {
            qb.append(OBJECT_CLASS, PERSON);
            qb.append(UID, uid + "*");
        }
        return findUsers(qb);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Node> findUsers(QueryBuilder qb) {
        Set<Node> users = new TreeSet<Node>();
        String query = qb.getQuery();
        try {
            SearchResult searchResult = null;
            Attributes attributes = null;
            SearchControls controls = new SearchControls();
            controls.setReturningAttributes(new String[]{ASTERISK, MODIFY_TIMESTAMP, MODIFIERS_NAME});
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search("", query, controls);
            queryCount++;
            while (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                //searchResult
                attributes = searchResult.getAttributes();
                LdapUser user = new LdapUser();
                user = fillAttributesInUser(user, attributes);
                users.add(user);
            }
        } catch (NamingException ex) {
            throw new LdapException(instance + ":" + qb.getQuery(), ex);
        }
        return users;
    }

    /**
     * Returns a LDAP-Group.
     *
     * @param cn the cn of that Group.
     * @see com.innoq.liqid.model.Node#getName().
     * @return the Node of that Group, either filled (if Group was found), or
     * empty.
     */
    public Node getGroup(final String cn) {
        Node group = new LdapGroup(cn);
        try {
            String query = "(&(objectClass=groupOfNames)(cn=" + cn + "))";
            SearchResult searchResult = null;
            Attributes attributes = null;
            SearchControls controls = new SearchControls();
            controls.setReturningAttributes(new String[]{ASTERISK, MODIFY_TIMESTAMP, MODIFIERS_NAME});
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search("", query, controls);
            queryCount++;
            if (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                group = fillAttributesInGroup((LdapGroup) group, attributes);
            }
        } catch (NamingException ex) {
            throw new LdapException(instance + ":" + cn, ex);
        }
        return group;
    }

    /**
     * Returns several LDAP-Groups for a given search-String.
     *
     * @param cn the cn (or part of cn) for the groups.
     * @return Groups as a Set of Nodes.
     */
    public Set<Node> findGroups(final String cn) {
        QueryBuilder qb = new LdapQueryBuilder();
        if (cn.equals("*")) {
            qb.append(OBJECT_CLASS, GROUP_OF_NAMES);
        } else {
            qb.append(OBJECT_CLASS, GROUP_OF_NAMES);
            qb.append(CN, cn + "*");
        }
        return findGroups(qb);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Node> findGroups(QueryBuilder qb) {
        Set<Node> groups = new TreeSet<Node>();
        String query = qb.getQuery();
        try {
            SearchResult searchResult = null;
            Attributes attributes = null;
            SearchControls controls = new SearchControls();
            controls.setReturningAttributes(new String[]{ASTERISK, MODIFY_TIMESTAMP, MODIFIERS_NAME});
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search("", query, controls);
            queryCount++;
            while (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                LdapGroup group = new LdapGroup();
                group = fillAttributesInGroup(group, attributes);
                groups.add(group);
            }
        } catch (NamingException ex) {
            throw new LdapException(instance + ":" + qb.getQuery(), ex);
        }
        return groups;
    }

    /**
     * Returns the LDAP-Principal as a LdapUser.
     *
     * @return the Principal of that instance as a Node
     * @see com.innoq.liqid.model.Node.
     */
    public Node getPrincipal() {
        if (principal == null) {
            principal = new LdapUser();
            LdapUser p = (LdapUser) principal;
            p.setDn(Configuration.getProperty(instance + ".principal").trim());
            p.setUid(getUidForDN(p.getDn()));
        }
        return principal;
    }

    /**
     * Returns a Value from the Default Collection.
     *
     * @param key the key of the default-collection.
     * @return the default value for that key if exists otherwise an empty
     * string.
     */
    public String getDefault(final String key) {
        if (defaultValues.containsKey(key)) {
            return defaultValues.get(key);
        }
        return "";
    }

    /**
     * Returns the uid for an User-DN.
     *
     * @param dn the dn of an user.
     * @return the uid for that DN.
     */
    public String getUidForDN(final String dn) {
        if (dn.startsWith("cn=")) {
            return dn.replace("," + baseDn, "").replace("cn=", "").trim();
        }
        return dn.replace("," + basePeopleDn, "").replace("uid=", "").trim();
    }

    /**
     * Returns the DN (Distinguished Name) for a Node.
     *
     * @param node the given Node.
     * @return the DN of that Node.
     */
    public String getDNForNode(final LdapNode node) {
        if (node instanceof LdapGroup) {
            return String.format(GROUP_CN_FORMAT, node.get(CN), baseGroupDn);
        } else {
            return String.format(USER_UID_FORMAT, node.get(UID), basePeopleDn);
        }
    }

    /**
     * Returns the OU (Organisation-Unit) an Node.
     *
     * @param node the given node.
     * @return the DN of the OU of that node.
     */
    public String getOuForNode(final LdapNode node) {
        if (node instanceof LdapGroup) {
            String ouGroup = Configuration.getProperty(instance + ".ou_group");
            return String.format(GROUP_CN_FORMAT, node.get(CN), ouGroup);
        }
        if (node instanceof LdapUser) {
            String ouPeople = Configuration.getProperty(instance + ".ou_people");
            return String.format(USER_UID_FORMAT, node.get(UID), ouPeople);
        }
        return String.format(ENTRY_CN_FORMAT, node.get(CN), node.get(OWNER));

    }

    /**
     * Load all Groups for a given User.
     *
     * @param user the given User.
     * @return Groups as a Set of Nodes for that User.
     */
    public Set<Node> getGroupsForUser(Node user) {
        Set<Node> groups = new TreeSet<Node>();
        try {
            String query = "(& (objectClass=groupOfNames) (member=" + ((LdapUser) user).getDn() + "))";
            SearchResult searchResult = null;
            Attributes attributes = null;
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search("", query, controls);
            queryCount++;
            while (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                groups.add(getGroup(getAttributeOrNa(attributes, CN)));
            }
        } catch (NamingException ex) {
            throw new LdapException(user, ex);
        }
        return groups;
    }

    /**
     * Load all Users for a given Group.
     *
     * @param group the given Group.
     * @return Users as a Set of Nodes for that Group.
     */
    public Set<Node> getUsersForGroup(Node group) {
        Set<Node> users = new TreeSet<Node>();
        try {
            String query = "(&(objectClass=person) (memberOf=" + ((LdapGroup) group).getDn() + "))";
            SearchResult searchResult = null;
            Attributes attributes = null;
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search("", query, controls);
            while (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                users.add(getUser(getAttributeOrNa(attributes, UID)));
            }
        } catch (NamingException ex) {
            throw new LdapException(group, ex);
        }
        return users;
    }

    /**
     * We use this method to check the given credentials to be valid. To prevent
     * the check every action call, the result (if true) will be cached for 60s.
     *
     * @param uid the given uid.
     * @param password the given password.
     * @return true if credentials are valid, otherwise false.
     */
    public boolean checkCredentials(final String uid, final String password) {
        StringBuilder sb = new StringBuilder("uid=").append(uid).append(",");
        sb.append(Configuration.getProperty(instance + ".ou_people")).append(",");
        sb.append(baseDn);
        if (uid == null || uid.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        try {
            Hashtable environment = (Hashtable) ctx.getEnvironment().clone();
            environment.put(Context.SECURITY_PRINCIPAL, sb.toString());
            environment.put(Context.SECURITY_CREDENTIALS, password);
            DirContext dirContext = new InitialDirContext(environment);
            dirContext.close();
            validationCount++;
            return true;
        } catch (NamingException ex) {
            log.write("NamingException " + ex.getLocalizedMessage() + "\n", LdapHelper.class);
        }
        return false;
    }

    /**
     * Returns a basic LDAP-User-Template for a new LDAP-User.
     *
     * @param uid of the new LDAP-User.
     * @return the (prefiled) User-Template.
     */
    public LdapUser getUserTemplate(String uid) {
        LdapUser user = new LdapUser(uid);
        user.setDn(user.getDn());
        String ocs[] = Configuration.getProperty("ldap.user.objectClasses").split(",");
        for (String oc : ocs) {
            user.addObjectClass(oc.trim());
        }
        user = (LdapUser) updateObjectClasses(user);

        // for inetOrgPerson
        user.set("sn", uid);

        // for JabberAccount
        if (user.getObjectClasses().contains("JabberAccount")) {
            user.set("jabberID", uid + "@" + defaultValues.get("jabberServer"));
            user.set("jabberAccess", "TRUE");
        }

        // for posixAccount
        user.set("uidNumber", "0");
        user.set("gidNumber", "0");
        user.set("homeDirectory", "/dev/null");

        // for ldapPublicKey
        if (user.getObjectClasses().contains("ldapPublicKey")) {
            user.set("sshPublicKey", defaultValues.get("sshKey"));
        }

        return user;
    }

    /**
     * Returns a basic LDAP-Group-Template for a new LDAP-Group. The Group
     * contains always the LDAP-Principal User (for Reading).
     *
     * @param cn of the new LDAP-Group.
     * @return the (pre-filled) Group-Template.
     */
    public LdapGroup getGroupTemplate(String cn) {
        LdapGroup group = new LdapGroup(cn);
        group.setDn(group.getDn());
        String ocs[] = Configuration.getProperty("ldap.group.objectClasses").split(",");
        for (String oc : ocs) {
            group.addObjectClass(oc.trim());
        }
        group = (LdapGroup) updateObjectClasses(group);
        // for groupOfNames
        // First User is always the Principal
        group.addUser((LdapUser) getPrincipal());
        return group;
    }

    /**
     * {@inheritDoc}
     */
    public long getCreationCount() {
        return creationCount;
    }

    /**
     * {@inheritDoc}
     */
    public long getDeletionCount() {
        return deletionCount;
    }

    /**
     * {@inheritDoc}
     */
    public long getModificationCount() {
        return modificationCount;
    }

    /**
     * {@inheritDoc}
     */
    public long getQueryCount() {
        return queryCount;
    }

    /**
     * {@inheritDoc}
     */
    public long getValidationCount() {
        return validationCount;
    }

    /**
     * {@inheritDoc}
     */
    public void reload() {
        loadProperties();
    }

    private LdapGroup updateGroupMembers(LdapGroup group) {
        LdapUser p = (LdapUser) principal;
        group.getAttributes().remove(MEMBER);
        if (!group.getUsers().contains(p)) {
            group.addUser(p);
        }
        for (LdapUser user : group.getUsers()) {
            if (user != null) {
                group.addAttribute(new BasicAttribute(MEMBER, user.getDn()));
            }
        }
        return group;
    }

    private LdapNode updateObjectClasses(LdapNode node) {
        BasicAttribute ocattrs = new BasicAttribute(OBJECT_CLASS);
        for (String oc : node.getObjectClasses()) {
            ocattrs.add(oc);
        }
        node.addAttribute(ocattrs);
        return node;
    }

    private ModificationItem[] buildModificationsForGroup(final LdapGroup newLdapGroup, final LdapGroup oldLdapGroup) {
        List<String> mods = new ArrayList<String>();
        List<String> adds = new ArrayList<String>();
        List<String> dels = new ArrayList<String>();
        Attributes attrs = new BasicAttributes();
        for (String key : newLdapGroup.getKeys()) {
            if (!MEMBER.equals(key) && !OBJECT_CLASS.equals(key)) {
                if (oldLdapGroup.get(key) != null && !newLdapGroup.get(key).equals(oldLdapGroup.get(key))) {
                    attrs.put(key, newLdapGroup.get(key));
                    mods.add(key);
                }
                if (oldLdapGroup.get(key) == null) {
                    attrs.put(key, newLdapGroup.get(key));
                    adds.add(key);
                }
            }
        }

        for (String key : oldLdapGroup.getKeys()) {
            if (!MEMBER.equals(key)
                    && !OBJECT_CLASS.equals(key)
                    && newLdapGroup.get(key) == null) {
                attrs.put(key, newLdapGroup.get(key));
                dels.add(key);
            }
        }

        attrs = filterForNullAttributes(attrs);

        List<ModificationItem> miList = new ArrayList<ModificationItem>();
        miList = buildObjectClassChangeSets(miList, oldLdapGroup, newLdapGroup);
        miList = buildMiListForGroup(miList, attrs, mods, adds, dels);
        miList = buildMemberChangeSets(miList, oldLdapGroup, newLdapGroup);

        int c = 0;
        ModificationItem[] miArray = new ModificationItem[miList.size()];
        for (ModificationItem m : miList) {
            miArray[c] = m;
            c++;
        }
        return miArray;
    }

    private List<ModificationItem> buildMiListForGroup(List<ModificationItem> miList, Attributes attrs, List<String> mods, List<String> adds, List<String> dels) {
        Enumeration<String> keys = attrs.getIDs();
        String k;
        while (keys.hasMoreElements()) {
            k = keys.nextElement();
            if (mods.contains(k)) {
                log.write("MiListForGroup MOD " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                miList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrs.get(k)));
            }
            if (adds.contains(k)) {
                log.write("MiListForGroup ADD " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, attrs.get(k)));
            }
            if (dels.contains(k)) {
                log.write("MiListForGroup REM " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                miList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attrs.get(k)));
            }
        }
        return miList;
    }

    private List<ModificationItem> buildMemberChangeSets(List<ModificationItem> miList, LdapGroup oldLdapGroup, LdapGroup newLdapGroup) {
        BasicAttribute a;
        for (LdapUser member : newLdapGroup.getUsers()) {
            if (!oldLdapGroup.getUsers().contains(member)) {
                a = new BasicAttribute(MEMBER, member.getDn());
                miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, a));
            }
        }
        for (LdapUser member : oldLdapGroup.getUsers()) {
            if (!newLdapGroup.getUsers().contains(member)) {
                a = new BasicAttribute(MEMBER, member.getDn());
                miList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, a));
            }
        }
        return miList;
    }

    private List<ModificationItem> buildObjectClassChangeSets(List<ModificationItem> miList, LdapNode oldNode, LdapNode newNode) {
        BasicAttribute a;
        for (String oc : newNode.getObjectClasses()) {
            if (!oldNode.getObjectClasses().contains(oc)) {
                a = new BasicAttribute(OBJECT_CLASS, oc);
                miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, a));
            }
        }
        for (String oc : oldNode.getObjectClasses()) {
            if (!newNode.getObjectClasses().contains(oc)) {
                a = new BasicAttribute(OBJECT_CLASS, oc);
                miList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, a));
            }
        }
        return miList;
    }

    private ModificationItem[] buildModificationsForEntry(final LdapEntry newLdapEntry, final LdapEntry oldLdapEntry) {
        List<String> mods = new ArrayList<String>();
        List<String> adds = new ArrayList<String>();
        List<String> dels = new ArrayList<String>();
        Attributes attrs = new BasicAttributes();
        for (String key : newLdapEntry.getKeys()) {
            if (!OBJECT_CLASS.equals(key)) {
                if (oldLdapEntry.get(key) != null && !newLdapEntry.get(key).equals(oldLdapEntry.get(key))) {
                    attrs.put(key, newLdapEntry.get(key));
                    mods.add(key);
                }
                if (oldLdapEntry.get(key) == null) {
                    attrs.put(key, newLdapEntry.get(key));
                    adds.add(key);
                }
            }
        }
        for (String key : oldLdapEntry.getKeys()) {
            if (!OBJECT_CLASS.equals(key) && newLdapEntry.get(key) == null) {
                attrs.put(key, newLdapEntry.get(key));
                dels.add(key);
            }
        }
        attrs = filterForNullAttributes(attrs);
        List<ModificationItem> miList = new ArrayList<ModificationItem>();
        miList = buildObjectClassChangeSets(miList, oldLdapEntry, newLdapEntry);
        miList = buildMiListForEntry(miList, attrs, mods, adds, dels);
        int c = 0;
        ModificationItem[] miArray = new ModificationItem[miList.size()];
        for (ModificationItem m : miList) {
            miArray[c] = m;
            c++;
        }
        return miArray;
    }

    private ModificationItem[] buildModificationsForUser(final LdapUser newLdapUser, final LdapUser oldLdapUser) {
        List<String> mods = new ArrayList<String>();
        List<String> adds = new ArrayList<String>();
        List<String> dels = new ArrayList<String>();
        Attributes attrs = new BasicAttributes();
        for (String key : newLdapUser.getKeys()) {
            if (!OBJECT_CLASS.equals(key)) {
                if (oldLdapUser.get(key) != null && !newLdapUser.get(key).equals(oldLdapUser.get(key))) {
                    attrs.put(key, newLdapUser.get(key));
                    mods.add(key);
                }
                if (oldLdapUser.get(key) == null) {
                    attrs.put(key, newLdapUser.get(key));
                    adds.add(key);
                }
            }
        }
        for (String key : oldLdapUser.getKeys()) {
            if (!OBJECT_CLASS.equals(key) && newLdapUser.get(key) == null) {
                attrs.put(key, newLdapUser.get(key));
                dels.add(key);
            }
        }

        attrs = filterForNullAttributes(attrs);
        List<ModificationItem> miList = new ArrayList<ModificationItem>();
        miList = buildObjectClassChangeSets(miList, oldLdapUser, newLdapUser);
        miList = buildMiListForUser(miList, attrs, mods, adds, dels);
        if (newLdapUser.getPassword() != null) {
            BasicAttribute pwAttr = new BasicAttribute(USER_PASSWORD, "{SHA}" + newLdapUser.getPassword());
            log.write("update Password\n", LdapHelper.class);
            miList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, pwAttr));
        }
        int c = 0;
        ModificationItem[] miArray = new ModificationItem[miList.size()];
        for (ModificationItem m : miList) {
            miArray[c] = m;
            c++;
        }
        return miArray;
    }

    private Attributes filterForNullAttributes(Attributes attrs) {
        String k;
        Enumeration<String> keys = attrs.getIDs();
        while (keys.hasMoreElements()) {
            k = keys.nextElement();
            if (getAttributeOrNa(attrs, k).equals("null")) {
                log.write("filterForNullAttributes removing " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                attrs.remove(k);
            }
        }
        return attrs;
    }

    private List<ModificationItem> buildMiListForEntry(List<ModificationItem> miList, Attributes attrs, List<String> mods, List<String> adds, List<String> dels) {
        Enumeration<String> keys = attrs.getIDs();
        String k;
        while (keys.hasMoreElements()) {
            k = keys.nextElement();
            if (mods.contains(k)) {
                log.write("buildMiListForEntry MOD " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                miList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrs.get(k)));
            }
            if (adds.contains(k)) {
                log.write("buildMiListForEntry ADD " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, attrs.get(k)));
            }
            if (dels.contains(k)) {
                log.write("buildMiListForEntry REM " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                miList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attrs.get(k)));
            }
        }
        return miList;
    }

    private List<ModificationItem> buildMiListForUser(List<ModificationItem> miList, Attributes attrs, List<String> mods, List<String> adds, List<String> dels) {
        Enumeration<String> keys = attrs.getIDs();
        String k;
        while (keys.hasMoreElements()) {
            k = keys.nextElement();
            if (!USER_PASSWORD.equals(k)) {
                if (mods.contains(k)) {
                    log.write("buildMiListForUser MOD " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                    miList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrs.get(k)));
                }
                if (adds.contains(k)) {
                    log.write("buildMiListForUser ADD " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                    miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, attrs.get(k)));
                }
                if (dels.contains(k)) {
                    log.write("buildMiListForUser REM " + k + " " + attrs.get(k) + "\n", LdapHelper.class);
                    miList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attrs.get(k)));
                }
            }
        }
        return miList;
    }

    private LdapUser fillAttributesInUser(LdapUser user, Attributes attributes) {
        String key;
        Enumeration<String> keys = attributes.getIDs();
        try {
            user = (LdapUser) fillObjectClasses(user, attributes);
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                if (JPEG_PHOTO.equals(key) && attributes.get(JPEG_PHOTO) != null) {
                    Object photo = attributes.get(JPEG_PHOTO).get();
                    byte[] buf = (byte[]) photo;
                    user.setAvatar(getUserAvatarAsFile(new ImageIcon(buf), user.getUid()));
                }
                if (UID.equals(key)) {
                    user.setUid(getAttributeOrNa(attributes, key));
                }
                if (MODIFY_TIMESTAMP.equals(key)) {
                    user.setModifyTimestamp(getAttributeOrNa(attributes, key));
                } else if (MODIFIERS_NAME.equals(key)) {
                    user.setModifiersName(getAttributeOrNa(attributes, key));
                } else if (USER_PASSWORD.equals(key)) {
                    user.addAttribute(new BasicAttribute(USER_PASSWORD, null));
                } else {
                    user.addAttribute((BasicAttribute) attributes.get(key));
                }
            }
        } catch (NamingException ex) {
            throw new LdapException(user, ex);
        }
        return user;
    }

    private LdapGroup fillAttributesInGroup(LdapGroup group, Attributes attributes) {
        String key;
        LdapUser member;
        Enumeration<String> keys = attributes.getIDs();
        try {
            group = (LdapGroup) fillObjectClasses(group, attributes);
            while (keys.hasMoreElements()) {
                key = keys.nextElement();
                if (MODIFY_TIMESTAMP.equals(key)) {
                    group.setModifyTimestamp(getAttributeOrNa(attributes, key));
                } else if (MODIFIERS_NAME.equals(key)) {
                    group.setModifiersName(getAttributeOrNa(attributes, key));
                } else {
                    group.addAttribute((BasicAttribute) attributes.get(key));
                }
                if (CN.equals(key)) {
                    group.setCn(getAttributeOrNa(attributes, key));
                }
            }
            NamingEnumeration members = attributes.get(MEMBER).getAll();
            while (members.hasMoreElements()) {
                String memberDN = (String) members.nextElement();
                member = new LdapUser(getUidForDN(memberDN));
                member.setDn(memberDN);
                group.addUser(member);
            }
        } catch (NamingException ex) {
            throw new LdapException(group, ex);
        }
        return group;
    }

    private LdapNode fillObjectClasses(LdapNode node, Attributes attributes) {
        try {

            NamingEnumeration objectClasses = attributes.get(OBJECT_CLASS).getAll();

            while (objectClasses.hasMoreElements()) {
                String oc = (String) objectClasses.nextElement();
                node.addObjectClass(oc.trim());
            }
        } catch (NamingException ex) {
            throw new LdapException(node, ex);
        }
        return node;
    }

    private File getUserAvatarAsFile(ImageIcon avatar, String uid) {
        String tmpDir = Configuration.getInstance().getTmpDir();
        File folder = new File(tmpDir + "/ldap/" + instance + "/avatars");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(tmpDir + "/ldap/" + instance + "/avatars" + "/" + uid + ".png");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                throw new LdapException(instance + ":" + uid + ":" + file.getAbsolutePath(), ex);
            }
        }
        BufferedImage bi = null;
        try {
            Image img = avatar.getImage();
            bi = new BufferedImage(img.getWidth(null), img.getHeight(null),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
        } catch (IllegalArgumentException ex) {
            throw new LdapException(instance + ":" + uid + ":" + file.getAbsolutePath(), ex);
        }
        try {
            if (bi != null) {
                ImageIO.write(bi, "png", file);
            }
        } catch (IOException ex) {
            throw new LdapException(instance + ":" + uid + ":" + file.getAbsolutePath(), ex);
        }
        return file;
    }

    private String getAttributeOrNa(final Attributes attributes, final String key) {
        return attributes.get(key).toString().replace(key + ":", "").trim();
    }

    private String getOuAttributeFromDN(String dn) {
        return dn.replace("," + baseDn, "").replace("ou=", "").trim();
    }

    private void loadProperties() {
        queryCount = 0L;
        modificationCount = 0L;
        validationCount = 0L;
        creationCount = 0L;
        deletionCount = 0L;
        baseDn = Configuration.getProperty(instance + ".base_dn");
        basePeopleDn = Configuration.getProperty(instance + ".ou_people") + "," + baseDn;
        baseGroupDn = Configuration.getProperty(instance + ".ou_group") + "," + baseDn;
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, Configuration.getProperty(instance + ".principal"));
        env.put(Context.SECURITY_CREDENTIALS, Configuration.getProperty(instance + ".credentials"));

        env.put(Context.PROVIDER_URL, Configuration.getProperty(instance + ".url") + "/" + baseDn);
        if (Configuration.getProperty(instance + ".url").startsWith("ldaps")) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }

        defaultValues.put("jabberServer", Configuration.getProperty("ldap.jabberServer", DEFAULT_JABBER_SERVER));
        defaultValues.put("sshKey", Configuration.getProperty("ldap.sshKey", DEFAULT_SSH_KEY));

        try {
            ctx = new InitialDirContext(env);
            online = true;
            log.write("connected to " + instance + "\n", LdapHelper.class);
            if ("true".equals(Configuration.getProperty(instance + ".ou.autocreate"))) {
                checkOus();
            }
        } catch (NamingException ex) {
            online = false;
            log.write("could not connect to " + instance + "\n", LdapHelper.class);
            throw new LdapException(instance + ": loadProperties", ex);
        }
    }

    private void checkOus() {
        log.write("checking Organisation Units\n", LdapHelper.class);
        String ous[] = {basePeopleDn, baseGroupDn};
        List<String> exists = new ArrayList<String>();
        try {
            String query = "(objectClass=organizationalUnit)";
            String ouAttribute;
            SearchResult searchResult = null;
            Attributes attributes = null;
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search("", query, controls);
            while (results.hasMore()) {
                searchResult = (SearchResult) results.next();
                attributes = searchResult.getAttributes();
                ouAttribute = getAttributeOrNa(attributes, "ou");
                for (String ou : ous) {
                    if (getOuAttributeFromDN(ou).equals(ouAttribute)) {
                        log.write("existing Organisation Unit " + ou + "\n", LdapHelper.class);
                        exists.add(ou.trim());
                    }
                }
            }
            Hashtable environment = (Hashtable) ctx.getEnvironment().clone();
            environment.put(Context.PROVIDER_URL, Configuration.getProperty(instance + ".url"));
            DirContext dirContext = new InitialDirContext(environment);
            for (String ou : ous) {
                log.write("checking ou: " + ou + "\n", LdapHelper.class);
                if (!exists.contains(ou)) {
                    log.write("creating Organisation Unit " + ou + "\n", LdapHelper.class);
                    dirContext.bind(ou, null, getOuAttributes(ou));
                    creationCount++;
                }
            }
            dirContext.close();
        } catch (NamingException ex) {
            throw new LdapException(instance + ": checkOus", ex);
        }
    }

    private BasicAttributes getOuAttributes(String dn) {
        BasicAttributes attrs = new BasicAttributes();
        BasicAttribute ocattrs = new BasicAttribute(OBJECT_CLASS);
        ocattrs.add("top");
        ocattrs.add("organizationalUnit");
        attrs.put(ocattrs);
        attrs.put("ou", getOuAttributeFromDN(dn));
        log.write("Attributes for " + dn + ":\n" + attrs.toString() + "\n", LdapHelper.class);
        return attrs;
    }

    private void checkDirs() {
        String tmpDir = Configuration.getInstance().getTmpDir();
        String ldapDir = tmpDir + "/ldap";
        String subFolders[] = {instance + "/avatars"};
        for (String subFolder : subFolders) {
            File aFolder = new File(ldapDir + "/" + subFolder);
            if (!aFolder.exists()) {
                aFolder.mkdirs();
            }
        }
    }
}
