/**
 * QueryBuilder
 * 27.05.2012
 * @author Philipp Haussleiter
 *
 * TODO
 * <ul>
 * <li>Support OR</li>
 * <li>Support NOT</li>
 * </ul>
 */
package com.innoq.liqid.model;

/**
 * This Builder helps to Build specific Queries for a specific System.
 * @author Philipp Haußleiter
 */
public interface QueryBuilder {

    /**
     * Adds a QueryPart to the QueryBuilder.
     * @param key the key of the query (e.g. uid).
     * @param value the value of the query - or part of it (e.g. ph*, phl).
     * @return The QueryBuilder.
     */
    public QueryBuilder append(String key, String value);

    /**
     * Returns the complete Query of that Builder.
     * @return the complete Query.
     */
    public String getQuery();
}
