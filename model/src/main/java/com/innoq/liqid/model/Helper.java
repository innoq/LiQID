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
package com.innoq.liqid.model;

import java.util.Set;

/**
 * Helper
 * 11.12.2011
 */
public interface Helper {

    /**
     * We use this method to check the given credentials to be valid.
     * To prevent the check every action call,
     * the result (if true) will be cached for 60s.
     * @param uid the given uid.
     * @param password the given password.
     * @return true if credentials are valid, otherwise false.
     */
    public boolean checkCredentials(final String uid, final String password);

    public Set<Node> findUsers(final String uid);

    /**
     * Finds a List of Users with a QueryBuilder.
     * @param qb
     * @return a List of Nodes.
     */
    public Set<Node> findUsers(final QueryBuilder qb);

    /**
     * Finds a List of Groups by a given CN (or Part of it).
     * @param cn the given CN to search.
     * @return a List of Nodes.
     */
    public Set<Node> findGroups(final String cn);

    /**
     * Finds a List of Groups with a QueryBuilder.
     * @param qb
     * @return a List of Nodes.
     */
    public Set<Node> findGroups(final QueryBuilder qb);

    public Node getGroup(final String cn);

    public Set<Node> getGroupsForUser(Node user);

    public Node getUser(final String uid);

    public Set<Node> getUsersForGroup(Node group);

    /**
     * Reloads the Helpers Configuration.
     */
    public void reload();

    /**
     * Delete the User Object from the Directory.
     * @param node
     * @return true if the user was deleted, false otherwise.
     * @throws Exception
     */
    public boolean rmUser(final Node node) throws Exception;

    /**
     * Delete the Group Object from the Directory.
     * @param node
     * @return true if the group was deleted, false otherwise.
     * @throws Exception
     */
    public boolean rmGroup(final Node node) throws Exception;

    /**
     * Write the User Object to the Directory.
     * Creates a new one or updates the old one.
     * @param node
     * @return true if the user was saved, false otherwise.
     * @throws Exception
     */
    public boolean setUser(final Node node) throws Exception;

    /**
     * Write the Group Object to the Directory.
     * Creates a new one or updates the old one.
     * @param node
     * @return true if the group was saved, false otherwise.
     * @throws Exception
     */
    public boolean setGroup(final Node node) throws Exception;

    /**
     * Returns the Principal Node.
     * @return the Principal of that Helper as a Node.
     */
    public Node getPrincipal();

    /**
     * Returns the count of creating operations for the given directory.
     * @return Number of Creation Operations.
     */
    public long getCreationCount();

    /**
     * Returns the count of deleting operations for the given directory.
     * @return Number of Deletion Operations.
     */
    public long getDeletionCount();

    /**
     * Returns the count of modification operations for the given directory.
     * @return Number of Update Operations.
     */
    public long getModificationCount();

    /**
     * Returns the count of query operations for the given directory.
     * @return Number of Query Operations.
     */
    public long getQueryCount();

    /**
     * Returns the count of validation operations for the given directory.
     * @return Number of Validation Operations.
     */
    public long getValidationCount();
}
