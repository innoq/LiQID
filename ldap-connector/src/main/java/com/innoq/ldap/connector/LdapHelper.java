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

import com.innoq.liqid.model.Helper;
import com.innoq.liqid.model.Node;
import com.innoq.liqid.model.QueryBuilder;
import com.innoq.liqid.utils.Configuration;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LdapHelper 14.04.2011
 */
public class LdapHelper implements Helper {

    private static final String TAG_AVATARS = "/avatars";
    private static final String LOG_MODIFY_ATTRIBUTES = "modifyAttributes: ";
    private static final String LOG_BIND = "bind: ";
    private final Logger Logger = LoggerFactory.getLogger(LdapHelper.class);
    private Node principal;
    private DirContext ctx;
    private String baseDn;
    private String basePeopleDn;
    private String baseGroupDn;
    private String adminGroupIdentifiyer;
    private String groupMemberAttribut;
    private String userIdentifyer;
    private String groupIdentifyer;
    private String groupObjectClass;
    private String userObjectClass;
    private String[] userObjectClasses;
    private String[] groupObjectClasses;
    private Map<String, String> defaultValues = new HashMap<>();
    private static Map<String, LdapHelper> helpers = new HashMap<>();
    private boolean online;
    private final String instanceName;
    private long queryCount;
    private long modificationCount;
    private long validationCount;
    private long creationCount;
    private long deletionCount;
    
    /**
     * Returns an Instance of the LdapHelper.
     *
     * @param instance the identifyer of the LDAP Instance (e.g. ldap1)
     */
    public LdapHelper(String instance) {
        this.instanceName = instance;
        checkDirs();
    }

    public static LdapHelper getInstance() {
        String defaultLdap = Configuration.getProperty("default.ldap");
        return getInstance(defaultLdap);
    }

    /**
     * Returns a new Instance of LdapHelper.
     *
     * @param instance the identifyer of the LDAP Instance (e.g. ldap1)
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
     * Writes the modifications on an user object back to the LDAP.
     *
     * @return true if the user was set correct, false otherwise.
     * @throws java.lang.Exception if adding Node fails.
     */
    @Override
    public boolean setUser(final Node node) throws Exception {
        return setUserInContext(ctx, node);
    }

    /**
     * Writes the modifications on an user object back to the LDAP using a
     * specific context.
     *
     * @param node the LDAP Node with User Content.
     * @param uid the uid of the User that adds the Node.
     * @param password the password of the User that adds the Node.
     * @return true if the user was set correct, false otherwise.
     * @throws Exception if adding a User fails.
     */
	@Override
    public boolean setUserAsUser(Node node, String uid, String password) throws Exception {
		boolean status = false;
		StringBuilder sb = new StringBuilder(userIdentifyer + "=").append(uid).append(",");
		sb.append(Configuration.getProperty(instanceName + LdapKeys.ATTR_OU_PEOPLE)).append(",");
		sb.append(baseDn);
		if (uid == null || uid.isEmpty() || password == null || password.isEmpty()) {
			return false;
		}
		try {
			Hashtable<String, String> environment = (Hashtable<String, String>) ctx.getEnvironment().clone();
			environment.put(Context.SECURITY_PRINCIPAL, sb.toString());
			environment.put(Context.SECURITY_CREDENTIALS, password);
			DirContext dirContext = new InitialDirContext(environment);
			status = setUserInContext(dirContext, node);
			dirContext.close();
		} catch (NamingException ex) {
			handleNamingException("NamingException " + ex.getLocalizedMessage(), ex);
		}
		return status;
	}

    /**
     * Deletes an LDAP-User.
     *
     * @param node of the LDAP-User to be deleted.
     * @return true if User was deleted, otherwise false.
     */
    @Override
    public boolean rmUser(final Node node) {
        LdapUser user = (LdapUser) node;
        if(node == null) {
        	return false;
        }
        try {
            deletionCount++;
            ctx.unbind(getOuForNode(user));
        } catch (NamingException ex) {
            handleNamingException(user, ex);
        }
        Node ldapUser = getUser(user.getUid());
        return ldapUser.isEmpty();
    }

    /**
     * Deletes a LDAP-Group.
     *
     * @param node of the LDAP-Group to be deleted.
     * @return true if Group was deleted, otherwise false.
     */
    @Override
    public boolean rmGroup(Node node) {
        LdapGroup group = (LdapGroup) node;
        try {
            deletionCount++;
            ctx.unbind(getOuForNode(group));
        } catch (NamingException ex) {
            handleNamingException(group, ex);
        }
        Node ldapGroup = getGroup(group.getCn());
        return ldapGroup.isEmpty();
    }

    /**
     * Adds or updates a LDAP-Group.
     *
     * @param node of the LDAP-Group to be set.
     * @return true if the Group was added/updated, otherwise false.
     * @throws Exception if adding a Group fails.
     */
    @Override
    public boolean setGroup(Node node) throws Exception {
        LdapGroup newLdapGroup = (LdapGroup) node;
        newLdapGroup = updateGroupMembers(newLdapGroup);
        LdapGroup oldLdapGroup = (LdapGroup) getGroup(newLdapGroup.getCn());
        if (oldLdapGroup.isEmpty()) {
            creationCount++;
            Logger.info(LOG_BIND + getOuForNode(newLdapGroup));
            ctx.bind(getOuForNode(newLdapGroup), null, newLdapGroup.getAttributes());
            return true;
        } else {
            ModificationItem[] mods = buildModificationsForGroup(newLdapGroup, oldLdapGroup);
            if (mods.length > 0) {
                modificationCount++;
                Logger.info(LOG_MODIFY_ATTRIBUTES + getOuForNode(newLdapGroup));
                ctx.modifyAttributes(getOuForNode(newLdapGroup), mods);
                return true;
            }
        }
        return false;
    }

    public boolean setEntry(Node node) throws Exception {
        LdapEntry newLdapEntry = (LdapEntry) node;
        try {
            LdapEntry oldLdapEntry = (LdapEntry) getEntry(newLdapEntry.getCn(), newLdapEntry.getOwner());
            if (oldLdapEntry.isEmpty()) {
                creationCount++;
                Logger.info(LOG_BIND + getOuForNode(newLdapEntry));
                ctx.bind(getOuForNode(newLdapEntry), null, newLdapEntry.getAttributes());
            } else {
                ModificationItem[] mods = buildModificationsForEntry(newLdapEntry, oldLdapEntry);
                if (mods.length > 0) {
                    modificationCount++;
                    Logger.info(LOG_MODIFY_ATTRIBUTES + getOuForNode(newLdapEntry));
                    ctx.modifyAttributes(getOuForNode(newLdapEntry), mods);
                }
            }
            return true;
        } catch (NamingException ex) {
            handleNamingException(newLdapEntry, ex);
            return false;
        }
    }

    /**
     * Returns an Sub-Entry of an LDAP User/LDAP Group.
     *
     * @param cn of that Entry.
     * @param owner DN of Parent Node.
     * @return a new Entry.
     */
    public Node getEntry(final String cn, final String owner) {
        // TODO implement me!
        Node entry = new LdapEntry(cn, owner);
        return entry;
    }

    /**
     * Returns an LDAP-User.
     *
     * @param uid the uid of the User.
     * @see com.innoq.liqid.model.Node#getName
     * @return the Node of that User, either filled (if User was found), or empty.
     */
    @Override
    public Node getUser(final String uid) {
        
        try {
            String query = "(&(objectClass=" + userObjectClass + ")(" + userIdentifyer + "=" + uid + "))";
            SearchResult searchResult;
            Attributes attributes;
            SearchControls controls = new SearchControls();
            controls.setReturningAttributes(new String[]{LdapKeys.ASTERISK, LdapKeys.MODIFY_TIMESTAMP, LdapKeys.MODIFIERS_NAME, LdapKeys.ENTRY_UUID});
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = ctx.search("", query, controls);
            queryCount++;
            if (results.hasMore()) {
                searchResult = results.next();
                attributes = searchResult.getAttributes();
                LdapUser user = new LdapUser(uid, this);
                user.setDn(searchResult.getNameInNamespace());
                user = fillAttributesInUser((LdapUser) user, attributes);
                return user;
            }
        } catch (NamingException ex) {
            handleNamingException(instanceName + ":" + uid, ex);
        }
        return new LdapUser();
    }

    /**
     * Returns several LDAP-Users for a given search-String.
     *
     * @param uid the uid or part of uid of the Users.
     * @return Users as a Set of com.innoq.liqid.model.Node.
     */
    @Override
    public Set<Node> findUsers(final String uid) {
        QueryBuilder qb = new LdapQueryBuilder();
        if ("*".equals(uid)) {
            qb.append(LdapKeys.OBJECT_CLASS, userObjectClass);
        } else {
            qb.append(LdapKeys.OBJECT_CLASS, userObjectClass);
            qb.append(userIdentifyer, uid + "*");
        }
        return findUsers(qb);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Node> findUsers(QueryBuilder qb) {
        Set<Node> users = new TreeSet<>();
        String query = qb.getQuery();
        try {
            SearchResult searchResult;
            Attributes attributes;
            SearchControls controls = new SearchControls();
            controls.setReturningAttributes(new String[]{LdapKeys.ASTERISK, LdapKeys.MODIFY_TIMESTAMP, LdapKeys.MODIFIERS_NAME});
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = ctx.search("", query, controls);
            queryCount++;
            while (results.hasMore()) {
                searchResult = results.next();
                attributes = searchResult.getAttributes();
                LdapUser user = new LdapUser();
                user.setDn(searchResult.getNameInNamespace());
                user = fillAttributesInUser(user, attributes);
                users.add(user);
            }
        } catch (NamingException ex) {
            handleNamingException(instanceName + ":" + qb.getQuery(), ex);
        }
        return users;
    }

    /**
     * Returns a LDAP-Group.
     *
     * @param cn the cn of that Group.
     * @see com.innoq.liqid.model.Node#getName()
     * @return the Node of that Group, either filled (if Group was found), or empty.
     */
    @Override
    public Node getGroup(final String cn) {
        try {
            String query = "(&(objectClass=" + groupObjectClass + ")(" + groupIdentifyer + "=" + cn + "))";
            SearchResult searchResult;
            Attributes attributes;
            SearchControls controls = new SearchControls();
            controls.setReturningAttributes(new String[]{LdapKeys.ASTERISK, LdapKeys.MODIFY_TIMESTAMP, LdapKeys.MODIFIERS_NAME, LdapKeys.ENTRY_UUID});
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = ctx.search("", query, controls);
            queryCount++;
            if (results.hasMore()) {
                LdapGroup group = new LdapGroup(cn, this);
            	searchResult = results.next();
                group.setDn(searchResult.getNameInNamespace());
                attributes = searchResult.getAttributes();
                group = fillAttributesInGroup((LdapGroup) group, attributes);
                return group;
            }
        } catch (NamingException ex) {
            handleNamingException(instanceName + ":" + cn, ex);
        }
       return new LdapGroup();
    }

    /**
     * Returns several LDAP-Groups for a given search-String.
     *
     * @param cn the cn (or part of cn) for the groups.
     * @return Groups as a Set of Nodes.
     */
    @Override
    public Set<Node> findGroups(final String cn) {
        QueryBuilder qb = new LdapQueryBuilder();
        if ("*".equals(cn)) {
            qb.append(LdapKeys.OBJECT_CLASS, groupObjectClass);
        } else {
            qb.append(LdapKeys.OBJECT_CLASS, groupObjectClass);
            qb.append(groupIdentifyer, cn + "*");
        }
        return findGroups(qb);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Node> findGroups(QueryBuilder qb) {
        Set<Node> groups = new TreeSet<>();
        String query = qb.getQuery();
        try {
            SearchResult searchResult;
            Attributes attributes;
            SearchControls controls = new SearchControls();
            controls.setReturningAttributes(new String[]{LdapKeys.ASTERISK, LdapKeys.MODIFY_TIMESTAMP, LdapKeys.MODIFIERS_NAME});
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = ctx.search("", query, controls);
            queryCount++;
            while (results.hasMore()) {
                searchResult = results.next();
                attributes = searchResult.getAttributes();
                LdapGroup group = new LdapGroup();
                group.setDn(searchResult.getNameInNamespace());
                group = fillAttributesInGroup(group, attributes);
                groups.add(group);
            }
        } catch (NamingException ex) {
            handleNamingException(instanceName + ":" + qb.getQuery(), ex);
        }
        return groups;
    }

    /**
     * Returns the LDAP-Principal as a LdapUser.
     *
     * @return the Principal of that instanceName as a Node
     */
    @Override
    public Node getPrincipal() {
        if (principal == null) {
            principal = new LdapUser();
            LdapUser p = (LdapUser) principal;
            p.setDn(Configuration.getProperty(instanceName + ".principal").trim());
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
    	return getIdentifyerValueFromDN(dn);
    }

    /**
     * Returns the DN (Distinguished Name) for a Node.
     *
     * @param node the given Node.
     * @return the DN of that Node.
     */
    public String getDNForNode(final LdapNode node) {
        if (node instanceof LdapGroup) {
            return String.format(LdapKeys.GROUP_CN_FORMAT, groupIdentifyer, node.get(groupIdentifyer), baseGroupDn);
        } else {
            return String.format(LdapKeys.USER_UID_FORMAT, userIdentifyer, node.get(userIdentifyer), basePeopleDn);
        }
    }

    /**
     * Returns the OU (Organisation-Unit) an Node.
     *
     * @param node the given node.
     * @return the DN of the OU of that node.
     */
    public String getOuForNode(final LdapNode node) {
    	if(node == null) {
    		return null;
    	}
        if (node instanceof LdapGroup) {
            String ouGroup = Configuration.getProperty(instanceName + ".ou_group");
            return String.format(LdapKeys.GROUP_CN_FORMAT, groupIdentifyer, node.get(groupIdentifyer), ouGroup);
        }
        if (node instanceof LdapUser) {
            String ouPeople = Configuration.getProperty(instanceName + LdapKeys.ATTR_OU_PEOPLE);
            return String.format(LdapKeys.USER_UID_FORMAT, userIdentifyer, node.get(userIdentifyer), ouPeople);
        }
        return String.format(LdapKeys.ENTRY_CN_FORMAT, groupIdentifyer, node.get(groupIdentifyer), node.get(LdapKeys.OWNER));

    }

    /**
     * Load all Groups for a given User.
     *
     * @param user the given User.
     * @return Groups as a Set of Nodes for that User.
     */
    @Override
    public Set<Node> getGroupsForUser(Node user) {
        Set<Node> groups = new TreeSet<>();
        try {
            String query = "(& (objectClass=" + groupObjectClass + ") (" + groupMemberAttribut + "=" + ((LdapUser) user).getDn() + "))";
            SearchResult searchResult;
            Attributes attributes;
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = ctx.search("", query, controls);
            queryCount++;
            Node group;
            while (results.hasMore()) {
                searchResult = results.next();
                attributes = searchResult.getAttributes();
                group = getGroup(getAttributeOrNa(attributes, groupIdentifyer));
                group.setDn(searchResult.getNameInNamespace());
                groups.add(group);
            }
        } catch (NamingException ex) {
            handleNamingException(user, ex);
        }
        return groups;
    }

    /**
     * Load all Users for a given Group.
     *
     * @param group the given Group.
     * @return Users as a Set of Nodes for that Group.
     */
	@Override
    public Set<Node> getUsersForGroup(Node group) {
		Set<Node> users = new TreeSet<>();
		try {
			String query = "(" + groupIdentifyer + "=" + group.getName() + ")";
			SearchResult searchResult;
			Attributes attributes;
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			NamingEnumeration<SearchResult> results = ctx.search("", query, controls);
			Node user;
			while (results.hasMore()) {
				searchResult = results.next();
				attributes = searchResult.getAttributes();
				NamingEnumeration<?> members = attributes.get(groupMemberAttribut).getAll();
				while (members.hasMoreElements()) {
					String memberDN = (String) members.nextElement();
					String uid = getIdentifyerValueFromDN(memberDN);
					user = getUser(uid);
					user.setDn(memberDN);
					users.add(user);
				}
			}
		} catch (NamingException ex) {
			handleNamingException(group, ex);
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
    @Override
    public boolean checkCredentials(final String uid, final String password) {
        StringBuilder sb = new StringBuilder(userIdentifyer + "=").append(uid).append(",");
        sb.append(Configuration.getProperty(instanceName + LdapKeys.ATTR_OU_PEOPLE)).append(",");
        sb.append(baseDn);
        if (uid == null || uid.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        try {
            Hashtable<String,String> environment = (Hashtable<String,String>) ctx.getEnvironment().clone();
            environment.put(Context.SECURITY_PRINCIPAL, sb.toString());
            environment.put(Context.SECURITY_CREDENTIALS, password);
            DirContext dirContext = new InitialDirContext(environment);
            dirContext.close();
            validationCount++;
            return true;
        } catch (NamingException ex) {
            handleNamingException("NamingException " + ex.getLocalizedMessage() + "\n", ex);
        }
        return false;
    }

    // TODO externalize User Template (e.g. as LDIF)
    /**
     * Returns a basic LDAP-User-Template for a new LDAP-User.
     *
     * @param uid of the new LDAP-User.
     * @return the (prefiled) User-Template.
     */
    public LdapUser getUserTemplate(String uid) {
        LdapUser user = new LdapUser(uid, this);
        for (String oc : userObjectClasses) {
            user.addObjectClass(oc.trim());
        }
        user = (LdapUser) updateObjectClasses(user);

        // TODO this needs to be cleaner :-/.
        // for inetOrgPerson
        if (user.getObjectClasses().contains("inetOrgPerson")
                || user.getObjectClasses().contains("person")) {
            user.set("sn", uid);
        }

        // for JabberAccount
        if (user.getObjectClasses().contains("JabberAccount")) {
            user.set("jabberID", uid + "@" + defaultValues.get("jabberServer"));
            user.set("jabberAccess", "TRUE");
        }

        // for posixAccount
        if (user.getObjectClasses().contains("posixAccount")) {
            user.set("uidNumber", "99999");
            user.set("gidNumber", "99999");
            user.set("cn", uid);
            user.set("homeDirectory", "/dev/null");
        }

        // for ldapPublicKey
        if (user.getObjectClasses().contains("ldapPublicKey")) {
            user.set("sshPublicKey", defaultValues.get("sshKey"));
        }

        return user;
    }

    // TODO externalize Group Template (e.g. as LDIF)
    /**
     * Returns a basic LDAP-Group-Template for a new LDAP-Group. The Group
     * contains always the LDAP-Principal User (for Reading).
     *
     * @param cn of the new LDAP-Group.
     * @return the (pre-filled) Group-Template.
     */
    public LdapGroup getGroupTemplate(String cn) {
        LdapGroup group = new LdapGroup(cn, this);
        for (String oc : groupObjectClasses) {
            group.addObjectClass(oc.trim());
        }
        group = (LdapGroup) updateObjectClasses(group);
        // TODO this needs to be cleaner :-/.

        // for inetOrgPerson
        if (group.getObjectClasses().contains("shadowAccount")) {
            group.set("uid", cn);
        }

        // for groupOfNames
        // First User is always the Principal
        group.addUser((LdapUser) getPrincipal());
        return group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCreationCount() {
        return creationCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDeletionCount() {
        return deletionCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getModificationCount() {
        return modificationCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getQueryCount() {
        return queryCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getValidationCount() {
        return validationCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reload() {
        loadProperties();
    }

    public String getAdminGroupIdentifiyer() {
        return adminGroupIdentifiyer;
    }

    public String getGroupMemberAttribut() {
        return groupMemberAttribut;
    }

    public String getUserIdentifyer() {
        return userIdentifyer;
    }

    public String getGroupIdentifyer() {
        return groupIdentifyer;
    }

    public String getGroupObjectClass() {
        return groupObjectClass;
    }

    public String getUserObjectClass() {
        return userObjectClass;
    }

    public String[] getUserObjectClasses() {
        return userObjectClasses;
    }

    public String[] getGroupObjectClasses() {
        return groupObjectClasses;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    /**
     * Return the Value of the given Identifyer from a given dn.
     * @param dn of the entry.
     * @return the identifyer.
     */
	public static String getIdentifyerFromDN(String dn) {
		if (dn != null) {
			String[] parts = dn.trim().split(",");
			String[] kvs;
			if(parts.length > 0 && parts[0].contains("="))  {
				kvs = parts[0].trim().split("=");
				return kvs.length > 0 ? kvs[0] : null;
			}
		}
		return null;
	}
	
    /**
     * Return the Value of the given Identifyer from a given dn.
     * @param dn of the entry.
     * @return the value of the identifyer.
     */
	public static String getIdentifyerValueFromDN(String dn) {
		if (dn != null) {
			String[] parts = dn.trim().split(",");
			String[] kvs;
			if(parts.length > 0 && parts[0].contains("="))  {
				kvs = parts[0].trim().split("=");
				return kvs.length > 0 ? kvs[1] : null;
			}
		}
		return null;
	}
	
    private boolean setUserInContext(DirContext ldapCtx, Node node) throws NamingException {
        LdapUser newLdapUser = (LdapUser) node;
        LdapUser oldLdapUser = (LdapUser) getUser(newLdapUser.getUid());
        oldLdapUser.debug();
        newLdapUser.debug();
        if (oldLdapUser.isEmpty()) {
            if (newLdapUser.getPassword() == null) {
                newLdapUser.setPassword("!" + System.currentTimeMillis());
            }
            Logger.info(LOG_BIND + getOuForNode(newLdapUser));
            ldapCtx.bind(getOuForNode(newLdapUser), null, newLdapUser.getAttributes());
            creationCount++;
        } else {
            ModificationItem[] mods = buildModificationsForUser(newLdapUser, oldLdapUser);
            if (mods.length > 0) {
            	Logger.info(LOG_MODIFY_ATTRIBUTES + getOuForNode(newLdapUser));
                ldapCtx.modifyAttributes(getOuForNode(newLdapUser), mods);
                modificationCount++;
            }
        }
        return true;
    }

    private LdapGroup updateGroupMembers(LdapGroup group) {
        LdapUser p = (LdapUser) getPrincipal();
        group.getAttributes().remove(groupMemberAttribut);
        if (!group.getUsers().contains(p)) {
            group.addUser(p);
        }
        for (LdapUser user : group.getUsers()) {
            if (user != null) {
                group.addAttribute(new BasicAttribute(groupMemberAttribut, user.getDn()));
            }
        }
        return group;
    }

    private LdapNode updateObjectClasses(LdapNode node) {
        BasicAttribute ocattrs = new BasicAttribute(LdapKeys.OBJECT_CLASS);
        for (String oc : node.getObjectClasses()) {
            ocattrs.add(oc);
        }
        node.addAttribute(ocattrs);
        return node;
    }

    private ModificationItem[] buildModificationsForGroup(final LdapGroup newLdapGroup, final LdapGroup oldLdapGroup) {
        List<String> mods = new ArrayList<>();
        List<String> adds = new ArrayList<>();
        List<String> dels = new ArrayList<>();
        Attributes attrs = new BasicAttributes();
        for (String key : newLdapGroup.getKeys()) {
            if (!groupMemberAttribut.equals(key) && !LdapKeys.OBJECT_CLASS.equals(key)) {
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
            if (!groupMemberAttribut.equals(key)
                    && !LdapKeys.OBJECT_CLASS.equals(key)
                    && newLdapGroup.get(key) == null) {
                attrs.put(key, newLdapGroup.get(key));
                dels.add(key);
            }
        }

        attrs = filterForNullAttributes(attrs);

        List<ModificationItem> miList = new ArrayList<>();
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
            	Logger.info("MiListForGroup MOD " + k + " " + attrs.get(k));
                miList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrs.get(k)));
            }
            if (adds.contains(k)) {
            	Logger.info("MiListForGroup ADD " + k + " " + attrs.get(k));
                miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, attrs.get(k)));
            }
            if (dels.contains(k)) {
            	Logger.info("MiListForGroup REM " + k + " " + attrs.get(k));
                miList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, attrs.get(k)));
            }
        }
        return miList;
    }

    private List<ModificationItem> buildMemberChangeSets(List<ModificationItem> miList, LdapGroup oldLdapGroup, LdapGroup newLdapGroup) {
        BasicAttribute a;
        for (LdapUser member : newLdapGroup.getUsers()) {
            if (!oldLdapGroup.getUsers().contains(member)) {
                a = new BasicAttribute(groupMemberAttribut, member.getDn());
                miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, a));
            }
        }
        for (LdapUser member : oldLdapGroup.getUsers()) {
            if (!newLdapGroup.getUsers().contains(member)) {
                a = new BasicAttribute(groupMemberAttribut, member.getDn());
                miList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, a));
            }
        }
        return miList;
    }

    private List<ModificationItem> buildObjectClassChangeSets(List<ModificationItem> miList, LdapNode oldNode, LdapNode newNode) {
        BasicAttribute a;
        for (String oc : newNode.getObjectClasses()) {
            if (!oldNode.getObjectClasses().contains(oc)) {
                a = new BasicAttribute(LdapKeys.OBJECT_CLASS, oc);
                miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, a));
            }
        }
        for (String oc : oldNode.getObjectClasses()) {
            if (!newNode.getObjectClasses().contains(oc)) {
                a = new BasicAttribute(LdapKeys.OBJECT_CLASS, oc);
                miList.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, a));
            }
        }
        return miList;
    }

    private ModificationItem[] buildModificationsForEntry(final LdapEntry newLdapEntry, final LdapEntry oldLdapEntry) {
        List<String> mods = new ArrayList<>();
        List<String> adds = new ArrayList<>();
        List<String> dels = new ArrayList<>();
        Attributes attrs = new BasicAttributes();
        for (String key : newLdapEntry.getKeys()) {
            if (!LdapKeys.OBJECT_CLASS.equals(key)) {
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
            if (!LdapKeys.OBJECT_CLASS.equals(key) && newLdapEntry.get(key) == null) {
                attrs.put(key, newLdapEntry.get(key));
                dels.add(key);
            }
        }
        attrs = filterForNullAttributes(attrs);
        List<ModificationItem> miList = new ArrayList<>();
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
        List<String> mods = new ArrayList<>();
        List<String> adds = new ArrayList<>();
        List<String> dels = new ArrayList<>();
        Attributes attrs = new BasicAttributes();
        for (String key : newLdapUser.getKeys()) {
            if (!LdapKeys.OBJECT_CLASS.equals(key)) {
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
            if (!LdapKeys.OBJECT_CLASS.equals(key) && newLdapUser.get(key) == null) {
                attrs.put(key, newLdapUser.get(key));
                dels.add(key);
            }
        }

        attrs = filterForNullAttributes(attrs);
        List<ModificationItem> miList = new ArrayList<>();
        miList = buildObjectClassChangeSets(miList, oldLdapUser, newLdapUser);
        miList = buildMiListForUser(miList, attrs, mods, adds, dels);
        if (newLdapUser.getPassword() != null) {
            BasicAttribute pwAttr = new BasicAttribute(LdapKeys.USER_PASSWORD, "{SHA}" + newLdapUser.getPassword());
            Logger.info("updating Password for "+newLdapUser.getName());
            miList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, pwAttr));
        }
        if(newLdapUser.getAvatar() != null) {
        	BasicAttribute avatarAttr = new BasicAttribute(LdapKeys.JPEG_PHOTO, convertImageToBinary(newLdapUser.getAvatar()));
        	Logger.info("updating Avatar for "+newLdapUser.getName());
        	if(oldLdapUser.getAvatar() != null) {
            	miList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, avatarAttr));        		
        	} else {
               	miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, avatarAttr));  
        	}
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
            if ("null".equals(getAttributeOrNa(attrs, k))) {
            	Logger.info("filterForNullAttributes removing " + k + " " + attrs.get(k));
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
            	Logger.info("buildMiListForEntry MOD " + k + " " + attrs.get(k));
                miList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrs.get(k)));
            }
            if (adds.contains(k)) {
            	Logger.info("buildMiListForEntry ADD " + k + " " + attrs.get(k));
                miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, attrs.get(k)));
            }
            if (dels.contains(k)) {
            	Logger.info("buildMiListForEntry REM " + k + " " + attrs.get(k));
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
            if (!LdapKeys.USER_PASSWORD.equals(k)) {
                if (mods.contains(k)) {
                	Logger.info("buildMiListForUser MOD " + k + " " + attrs.get(k));
                    miList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrs.get(k)));
                }
                if (adds.contains(k)) {
                	Logger.info("buildMiListForUser ADD " + k + " " + attrs.get(k));
                    miList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, attrs.get(k)));
                }
                if (dels.contains(k)) {
                	Logger.info("buildMiListForUser REM " + k + " " + attrs.get(k));
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
                if (LdapKeys.JPEG_PHOTO.equals(key) && attributes.get(LdapKeys.JPEG_PHOTO) != null) {
                    Object photo = attributes.get(LdapKeys.JPEG_PHOTO).get();
                    byte[] buf = (byte[]) photo;
                    user.setAvatar(getUserAvatarAsFile(new ImageIcon(buf), user.getUid()));
                }
                if (userIdentifyer.equals(key)) {
                    user.setUid(getAttributeOrNa(attributes, key));
                }
                if (LdapKeys.MODIFY_TIMESTAMP.equals(key)) {
                    user.setModifyTimestamp(getAttributeOrNa(attributes, key));
                } else if (LdapKeys.MODIFIERS_NAME.equals(key)) {
                    user.setModifiersName(getAttributeOrNa(attributes, key));
                } else if (LdapKeys.ENTRY_UUID.equals(key)) {
                	user.setEntryUUID(getAttributeOrNa(attributes, key));
                } else if (LdapKeys.USER_PASSWORD.equals(key)) {
                    user.addAttribute(new BasicAttribute(LdapKeys.USER_PASSWORD, null));
                } else {
                    user.addAttribute((BasicAttribute) attributes.get(key));
                }
            }
        } catch (NamingException ex) {
            handleNamingException(user, ex);
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
                if (LdapKeys.MODIFY_TIMESTAMP.equals(key)) {
                    group.setModifyTimestamp(getAttributeOrNa(attributes, key));
                } else if (LdapKeys.MODIFIERS_NAME.equals(key)) {
                    group.setModifiersName(getAttributeOrNa(attributes, key));
                } else if (LdapKeys.ENTRY_UUID.equals(key)) {
                	group.setEntryUUID(getAttributeOrNa(attributes, key));
                } else {
                    group.addAttribute((BasicAttribute) attributes.get(key));
                }
                if (groupIdentifyer.equals(key)) {
                    group.setCn(getAttributeOrNa(attributes, key));
                }
            }

	    Attribute membersAttribute = attributes.get(groupMemberAttribut);
	    if (membersAttribute != null) {
	    	NamingEnumeration<?> members = membersAttribute.getAll();
            	while (members.hasMoreElements()) {
                    String memberDN = (String) members.nextElement();
                    member = new LdapUser(getUidForDN(memberDN), this);
                    member.setDn(memberDN);
                    group.addUser(member);
		}
            }
        } catch (NamingException ex) {
            handleNamingException(group, ex);
        }
        return group;
    }

    private LdapNode fillObjectClasses(LdapNode node, Attributes attributes) {
        try {

        	NamingEnumeration<?> objectClasses = attributes.get(LdapKeys.OBJECT_CLASS).getAll();
            while (objectClasses.hasMoreElements()) {
                String oc = (String) objectClasses.nextElement();
                node.addObjectClass(oc.trim());
            }
        } catch (NamingException ex) {
            handleNamingException(node, ex);
        }
        return node;
    }

    private File getUserAvatarAsFile(ImageIcon avatar, String uid) {
        String tmpDir = Configuration.getInstance().getTmpDir();
        File folder = new File(tmpDir + "/ldap/" + instanceName + TAG_AVATARS);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(tmpDir + "/ldap/" + instanceName + TAG_AVATARS + "/" + uid + ".png");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                throw new LdapException(instanceName + ":" + uid + ":" + file.getAbsolutePath(), ex);
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
            throw new LdapException(instanceName + ":" + uid + ":" + file.getAbsolutePath(), ex);
        }
        try {
            ImageIO.write(bi, "png", file);
        } catch (IOException ex) {
            throw new LdapException(instanceName + ":" + uid + ":" + file.getAbsolutePath(), ex);
        }
        return file;
    }

	private Object convertImageToBinary(final File avatar) {
		Object photo = null;
		if (avatar.exists()) {
			long fileLength = avatar.length();

			Logger.info("converting "+avatar.getAbsolutePath()+" Size: "+fileLength);

			byte buffer[] = new byte[(int) fileLength];
			InputStream is;
			try {
				is = new BufferedInputStream(new FileInputStream(avatar));
				int len = is.read(buffer, 0, (int) fileLength);
				Logger.info("reading "+avatar.getName()+" into buffer: "+len+" bytes.");
				photo = buffer;
			} catch (FileNotFoundException ex) {
				Logger.error(ex.getLocalizedMessage(), ex);
			} catch (IOException ex) {
				Logger.error(ex.getLocalizedMessage(), ex);
			}
		} else {
			Logger.error(avatar.getAbsolutePath()+" does not exists!");
		}
		return photo;
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
        baseDn = Configuration.getProperty(instanceName + ".base_dn");
        basePeopleDn = Configuration.getProperty(instanceName + LdapKeys.ATTR_OU_PEOPLE) + "," + baseDn;
        baseGroupDn = Configuration.getProperty(instanceName + ".ou_group") + "," + baseDn;
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, Configuration.getProperty(instanceName + ".principal"));
        env.put(Context.SECURITY_CREDENTIALS, Configuration.getProperty(instanceName + ".credentials"));
        env.put(LdapKeys.LDAP_ATTRIBUTES_BINARY, "jpegPhoto");
        adminGroupIdentifiyer = Configuration.getProperty(instanceName + ".admin.group.id", LdapKeys.ADMIN_GROUP_CN).trim();
        userIdentifyer = Configuration.getProperty(instanceName + ".user.id.attribute", LdapKeys.USER_ID_ATTRIBUTE).trim();
        userObjectClass = Configuration.getProperty(instanceName + ".user.object.class", LdapKeys.USER_OBJECTCLASS).trim();
        groupIdentifyer = Configuration.getProperty(instanceName + ".group.id.attribute", LdapKeys.GROUP_ID_ATTRIBTUE).trim();
        groupObjectClass = Configuration.getProperty(instanceName + ".group.object.class", LdapKeys.GROUP_OBJECTCLASS).trim();
        groupMemberAttribut = Configuration.getProperty(instanceName + ".group.member.attribute", LdapKeys.GROUP_MEMBER_ATTRIBUTE).trim();
        String userObjectClassesConfiguration = Configuration.getProperty("ldap.user.objectClasses");
        if (userObjectClassesConfiguration == null) {
            throw new IllegalStateException("Configuration 'ldap.user.objectClasses' not found, but is mandatory");
        }
        String groupObjectClassesConfiguration = Configuration.getProperty("ldap.group.objectClasses");
        if (groupObjectClassesConfiguration == null) {
            throw new IllegalStateException("Configuration 'ldap.group.objectClasses' not found, but is mandatory");
        }
        userObjectClasses = userObjectClassesConfiguration.split(",");
        groupObjectClasses = groupObjectClassesConfiguration.split(",");
        env.put(Context.PROVIDER_URL, Configuration.getProperty(instanceName + ".url") + "/" + baseDn);
        if (Configuration.getProperty(instanceName + ".url").startsWith("ldaps")) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            if (Configuration.getProperty("keystore.path") != null) {
                env.put("java.naming.ldap.factory.socket", "com.innoq.ldap.ssl.SelfSignedSSLSocketFactory");
                Logger.info("Using Keystore from " + Configuration.getProperty("keystore.path") + " for Certificate Validation");
            } else if ("true".equals(Configuration.getProperty(instanceName + ".disable.cert.validation"))) {
                env.put("java.naming.ldap.factory.socket", "com.innoq.ldap.ssl.AllowAllSSLSocketFactory");
                Logger.info("Disable SSL Cert check for " + instanceName);
            }
        }
        defaultValues.put("jabberServer", Configuration.getProperty("ldap.jabberServer", LdapKeys.DEFAULT_JABBER_SERVER));
        defaultValues.put("sshKey", Configuration.getProperty("ldap.sshKey", LdapKeys.DEFAULT_SSH_KEY));

        try {
            ctx = new InitialDirContext(env);
            online = true;
            Logger.info("connected to " + instanceName + "\n");
            if ("true".equals(Configuration.getProperty(instanceName + ".ou.autocreate"))) {
                checkOus();
            }
        } catch (NamingException ex) {
            online = false;
            Logger.error("could not connect to " + instanceName + ":\n"+ex.getLocalizedMessage(), ex);
            handleNamingException(instanceName + ": loadProperties", ex);
        }
    }

    private void checkOus() {
        Logger.info("checking Organisation Units\n");
        String[] ous = {basePeopleDn, baseGroupDn};
        List<String> exists = new ArrayList<>();
        try {
            String query = "(objectClass=organizationalUnit)";
            String ouAttribute;
            SearchResult searchResult;
            Attributes attributes;
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration<SearchResult> results = ctx.search("", query, controls);
            while (results.hasMore()) {
                searchResult = results.next();
                attributes = searchResult.getAttributes();
                ouAttribute = getAttributeOrNa(attributes, "ou");
                for (String ou : ous) {
                    if (getOuAttributeFromDN(ou).equals(ouAttribute)) {
                        Logger.info("existing Organisation Unit " + ou);
                        exists.add(ou.trim());
                    }
                }
            }
            
            Hashtable<String, String> environment = (Hashtable<String, String>) ctx.getEnvironment().clone();
            environment.put(Context.PROVIDER_URL, Configuration.getProperty(instanceName + ".url"));
            DirContext dirContext = new InitialDirContext(environment);
            for (String ou : ous) {
            	Logger.info("checking ou: " + ou);
                if (!exists.contains(ou)) {
                	Logger.info("creating Organisation Unit " + ou);
                    dirContext.bind(ou, null, getOuAttributes(ou));
                    creationCount++;
                }
            }
            dirContext.close();
        } catch (NamingException ex) {
            handleNamingException(instanceName + ": checkOus", ex);
        }
    }

    private BasicAttributes getOuAttributes(String dn) {
        BasicAttributes attrs = new BasicAttributes();
        BasicAttribute ocattrs = new BasicAttribute(LdapKeys.OBJECT_CLASS);
        ocattrs.add("organizationalUnit");
        attrs.put(ocattrs);
        attrs.put("ou", getOuAttributeFromDN(dn));
        Logger.info("Attributes for " + dn + ":\n" + attrs.toString());
        return attrs;
    }

    private void checkDirs() {
        String tmpDir = Configuration.getInstance().getTmpDir();
        String ldapDir = tmpDir + "/ldap";
        String[] subFolders = {instanceName + TAG_AVATARS};
        for (String subFolder : subFolders) {
            File aFolder = new File(ldapDir + "/" + subFolder);
            if (!aFolder.exists()) {
                aFolder.mkdirs();
            }
        }
    }

    private void handleNamingException(String position, NamingException ne) {
        if (ne instanceof javax.naming.CommunicationException) {
            this.online = false;
            Logger.error(position + " Instance offline try to re-connect the next time!", ne);
        } else if (ne instanceof javax.naming.AuthenticationException) {
        	Logger.error(position + " AuthenticationException !", ne);
        } else {
            throw new LdapException(instanceName + ": checkOus", ne);
        }
    }

    private void handleNamingException(Node node, NamingException ne) {
        if (ne instanceof javax.naming.CommunicationException) {
            this.online = false;
            Logger.error(node.getName() + " Instance " + instanceName + " offline try to re-connect the next time!", ne);
        } else if (ne instanceof javax.naming.AuthenticationException) {
        	Logger.error(" AuthenticationException: " + node.toString(), ne);
        } else {
            throw new LdapException(node, ne);
        }
    }
}
