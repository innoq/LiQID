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
/**
 * LdapException
 * 22.05.2012
 */
package com.innoq.ldap.connector;

import com.innoq.liqid.model.Node;

public class LdapException extends RuntimeException {

    public LdapException(String msg, Throwable thrwbl){
        super(msg, thrwbl);
    }

    public LdapException(Node node, Throwable thrwbl) {
        super(thrwbl);
        if (node instanceof LdapGroup) {
            ((LdapGroup) node).debug();
        }
        if (node instanceof LdapUser) {
            ((LdapUser) node).debug();
        }
    }
}
