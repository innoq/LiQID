/**
 * LdapQueryBuilder
 * 27.05.2012
 * @author Philipp Haussleiter
 *
 */
package com.innoq.ldap.connector;

import com.innoq.liqid.model.QueryBuilder;
import java.util.Map;
import java.util.TreeMap;

public class LdapQueryBuilder implements QueryBuilder {

    private Map<String, String> elements = new TreeMap<String, String>();

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
