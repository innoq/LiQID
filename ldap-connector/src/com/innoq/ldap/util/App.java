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
package com.innoq.ldap.util;

import com.innoq.ldap.connector.LdapKeys;
import static com.innoq.liqid.utils.Configuration.SEP;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class App {

    private static final String HR = "------------------------------------------------------------------------------";
    private final String out = System.getProperty("user.home") + SEP + ".liqid" + SEP + "liqid.properties";
    private static PrintStream outputStream = System.out;
    private static boolean isTest;
    public static void main(String[] args) {
        int i = 0;
        if (args.length > 0) {
            App app = new App();
            for (String arg : args) {
                if (arg.startsWith("--")) {
                    arg = arg.substring(1, arg.length());
                }
                if ("-generate".equals(arg)) {
                    outputStream.println(app.getExampelConfig());
                    if(!isTest)System.exit(0);
                }
                i++;
            }
        } else {
            App.help();
        }
    }

    public static void setOutStream(PrintStream outStream) {
        isTest = true;
        outputStream = outStream;
    }

    private String getExampelConfig() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        StringBuilder sb = new StringBuilder();
        sb.append("\n# example liqid.properties").append("\n");
        sb.append("# created ").append(sdf.format(new Date())).append("\n");
        sb.append("# created with java -jar ldap-connetor.jar -generate\n"); 
        sb.append("\n# LDAP Settings\n");
        sb.append("\n## LDAP Listing, divided by \",\"").append("\n");
        sb.append("ldap.listing=ldap1\n");
        sb.append("## Default LDAP (with leading information)\n");
        sb.append("default.ldap=ldap1\n");
        sb.append("\n\n## Mandatory Configuration:").append("\n");
        sb.append("ldap.user.objectClasses=person\n");
        sb.append("ldap.group.objectClasses=groupOfUniqueNames\n");
        sb.append("\n### OUs for this LDAP instances\n");
        sb.append("ldap1.ou_people=ou=users\n");
        sb.append("ldap1.ou_group=ou=roles\n");
        sb.append("\n### ldap|ldaps :// <host>:<port>\n");
        sb.append("ldap1.url=ldap://localhost:389\n");
        sb.append("ldap1.principal=dc=Manager,dc=example,dc=com\n");
        sb.append("ldap1.credentials=password\n");
        sb.append("ldap1.base_dn=dc=example,dc=com\n");
        sb.append("\n\n## Optional Configuration:").append("\n");
        sb.append("### Use for Self-Signed Certifcates of use own Keystore\n");
        sb.append("# ldap1.disable.cert.validation=true\n\n");
        sb.append("### User ID Attribute - DEFAULT: ").append(LdapKeys.USER_ID_ATTRIBUTE).append("\n");
        sb.append("# ldap1.user.id.attribute=cn\n\n");
        sb.append("### User ObjectClass - DEFAULT: ").append(LdapKeys.USER_OBJECTCLASS).append("\n");
        sb.append("# ldap1.user.object.class=person\n\n");
        sb.append("### Group ID Attribute - DEFAULT: ").append(LdapKeys.GROUP_ID_ATTRIBTUE).append("\n");
        sb.append("# ldap1.group.id.attribute=cn\n\n");
        sb.append("### Group ObjectClass - DEFAULT: ").append(LdapKeys.GROUP_OBJECTCLASS).append("\n");
        sb.append("# ldap1.group.object.class=groupOfUniqueNames\n\n");
        sb.append("### Group Member Attribute - DEFAULT: ").append(LdapKeys.GROUP_MEMBER_ATTRIBUTE).append("\n");
        sb.append("# ldap1.group.member.attribute=uniqueMember\n");
        sb.append("\n### Define your own Keystore here:").append("\n");
        sb.append("# keystore.password=changeme").append("\n");
        sb.append("# keystore.path=").append(out.replace(".properties", ".keystore")).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    private static void help() {
        outputStream.println("\nHELP\n" + HR);
        outputStream.println("java -jar ldap-connetor.jar ");
        outputStream.println("  -help                  this help");
        outputStream.println("  -generate              generates an example liqid.properties as output.");
        outputStream.println(HR + "\n");
        if(!isTest)System.exit(0);
    }
}
