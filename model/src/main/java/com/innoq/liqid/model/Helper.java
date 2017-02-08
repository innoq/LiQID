/*
 Copyright (C) 2017 innoQ Deutschland GmbH

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
package com.innoq.liqid.model;

import java.util.Set;

/**
 * Helper 11.12.2011.
 */
public interface Helper {

    /**
     * We use this method to check the given credentials to be valid. To prevent
     * the check every action call, the result (if true) will be cached for 60s.
     *
     * @param uid the given uid.
     * @param password the given password.
     * @return true if credentials are valid, otherwise false.
     */
    boolean checkCredentials(final String uid, final String password);

    /**
     * Finds a Set of users by a given uid or *.
     *
     * @param uid the given uid.
     * @return result set of users matching the uid.
     */
    Set<Node> findUsers(final String uid);

    /**
     * Finds a List of Users with a QueryBuilder.
     *
     * @param qb the QueryBuilder.
     * @return a List of Nodes.
     */
    Set<Node> findUsers(final QueryBuilder qb);

    /**
     * Finds a List of Groups by a given CN (or Part of it).
     *
     * @param cn the given CN to search.
     * @return a List of Nodes.
     */
    Set<Node> findGroups(final String cn);

    /**
     * Finds a List of Groups with a QueryBuilder.
     *
     * @param qb the QueryBuilder.
     * @return a List of Nodes.
     */
    Set<Node> findGroups(final QueryBuilder qb);

    /**
     * Finds a Group with a given cn.
     *
     * @param cn the given cn.
     * @return a group, will return an empty group, when not found.
     */
    Node getGroup(final String cn);

    /**
     * Find all groups of a User.
     *
     * @param user the user, having the groups.
     * @return the list of Groups.
     */
    Set<Node> getGroupsForUser(Node user);

    /**
     * Finds a User with a given uid.
     *
     * @param uid the given uid.
     * @return a user, will return an empty user, when not found.
     */
    Node getUser(final String uid);

    /**
     * Find all Users of a Group.
     *
     * @param group the Group containing the users.
     * @return the list of Users.
     */
    Set<Node> getUsersForGroup(Node group);

    /**
     * Reloads the Helpers Configuration.
     */
    void reload();

    /**
     * Delete the User Object from the Directory.
     *
     * @param node the User to be deleted.
     * @return true if the user was deleted, false otherwise.
     * @throws Exception if rmUser fails.
     */
    boolean rmUser(final Node node) throws Exception;

    /**
     * Delete the Group Object from the Directory.
     *
     * @param node the Group to be deleted.
     * @return true if the group was deleted, false otherwise.
     * @throws Exception if rmGroup fails.
     */
    boolean rmGroup(final Node node) throws Exception;

    /**
     * Write the User Object to the Directory. Creates a new one or updates the
     * old one.
     *
     * @param node updated user.
     * @return true if the user was saved, false otherwise.
     * @throws Exception if setUser fails.
     * @see Helper#setUserAsUser(com.innoq.liqid.model.Node, java.lang.String,
     * java.lang.String)
     */
    boolean setUser(final Node node) throws Exception;

    /**
     * Write the User Object to the Directory using specific Credentials.
     * Creates a new one or updates the old one. This method also allows
     * self-updates for normal users.
     *
     * @param node updated user.
     * @param uid the uid of that user.
     * @param password the password of that user.
     * @return true if the user was saved, false otherwise.
     * @throws Exception if setUser fails.
     */
    boolean setUserAsUser(final Node node,
            final String uid,
            final String password) throws Exception;

    /**
     * Write the Group Object to the Directory. Creates a new one or updates the
     * old one.
     *
     * @param node updated group
     * @return true if the group was saved, false otherwise.
     * @throws Exception if setGroup fails.
     */
    boolean setGroup(final Node node) throws Exception;

    /**
     * Returns the Principal Node.
     *
     * @return the Principal of that Helper as a Node.
     */
    Node getPrincipal();

    /**
     * Returns the count of creating operations for the given directory.
     *
     * @return Number of Creation Operations.
     */
    long getCreationCount();

    /**
     * Returns the count of deleting operations for the given directory.
     *
     * @return Number of Deletion Operations.
     */
    long getDeletionCount();

    /**
     * Returns the count of modification operations for the given directory.
     *
     * @return Number of Update Operations.
     */
    long getModificationCount();

    /**
     * Returns the count of query operations for the given directory.
     *
     * @return Number of Query Operations.
     */
    long getQueryCount();

    /**
     * Returns the count of validation operations for the given directory.
     *
     * @return Number of Validation Operations.
     */
    long getValidationCount();
}
