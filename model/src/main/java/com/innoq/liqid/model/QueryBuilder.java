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
