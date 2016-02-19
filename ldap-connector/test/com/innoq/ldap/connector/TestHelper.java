/*
 Copyright (C) 2016 innoQ Deutschland GmbH

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * TestHelper 18.02.2016
 */
public class TestHelper {
	public TestHelper() {

	}

	@Test
	public void testGetIdentifyerFromDN() {
		String[] keys = { "cn", "uid", "ou", "ou", "dc" };
		String[] inputs = { 
				"cn=admin,dc=innoq,dc=com", 
				"uid=U_1455717494839,ou=People,dc=innoq,dc=com",
				"ou=People,dc=innoq,dc=com", 
				"ou=Group,dc=innoq,dc=com", 
				"dc=innoq,dc=com" 
				};
		String[] outputs = { "admin", "U_1455717494839", "People", "Group", "innoq" };
		String value, identifyer;
		for (int count = 0; count < outputs.length; count++) {
			value = LdapHelper.getIdentifyerValueFromDN(inputs[count]);
			identifyer = LdapHelper.getIdentifyerFromDN(inputs[count]);
			assertEquals(value, outputs[count]);
			assertEquals(identifyer, keys[count]);			
		}
	}
}
