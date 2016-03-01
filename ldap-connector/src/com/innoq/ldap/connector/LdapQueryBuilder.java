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
 * LdapQueryBuilder
 * 27.05.2012
 */
package com.innoq.ldap.connector;

import com.innoq.liqid.model.QueryBuilder;
import java.util.Map;
import java.util.TreeMap;

public class LdapQueryBuilder implements QueryBuilder {

    private Map<String, String> elements = new TreeMap<>();

    /**
     * {@inheritDoc}
     */
    public String getQuery() {
        if (elements.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (elements.size() > 1) {
            sb.append("(&");
        }
        for (String key : elements.keySet()) {
            sb.append("(").append(key).append("=").append(elements.get(key)).append(")");
        }
        if (elements.size() > 1) {
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public QueryBuilder append(String key, String value) {
        elements.put(key.trim(), value.trim());
        return this;
    }
}
