/**
 * App 08.07.2013 consolving.de
 *
 * @author Philipp Haussleiter<philipp@consolving.de>
 *
 */
package com.innoq.ldap.util;

import static com.innoq.liqid.utils.Configuration.SEP;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private final static String HR = "------------------------------------------------------------------------------";
    private String out = System.getProperty("user.home") + SEP + ".liqid" + SEP + "liqid.properties";

    public static void main(String[] args) {
        int i = 0;
        if (args.length > 0) {
            App app = new App();
            for (String arg : args) {
                if (arg.startsWith("--")) {
                    arg = arg.substring(1, arg.length());
                }
                if ("-generate".equals(arg)) {
                    System.out.println(app.getExampelConfig());
                }
                if ("-generate".equals(args[i - 1]) && "-out".equals(arg)) {
                    app.out = args[i + 1];
                    app.writeConfig(app.getExampelConfig());
                }
                i++;
            }
        } else {
            App.help();
        }
    }

    private FileOutputStream getFile() {
        File file = new File(out);
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);

        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fos;
    }

    private void writeConfig(final String content) {
        try {
            FileOutputStream fos = getFile();
            fos.write(content.getBytes("UTF-8"));
            fos.flush();
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getExampelConfig() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        StringBuilder sb = new StringBuilder();
        sb.append("# example liqid.properties").append("\n");
        sb.append("# created ").append(sdf.format(new Date())).append("\n");
        sb.append("\n# Mandatory").append("\n");
        sb.append("\n# Example Object Classes:\n");
        sb.append("ldap.user.objectClasses=top, organizationalPerson, inetOrgPerson, person, posixAccount").append("\n");
        sb.append("ldap.group.objectClasses=groupOfNames").append("\n");
        sb.append("\n# LDAP Settings:\n");
        sb.append("default.ldap=ldap1\n");
        sb.append("# LDAP Listsing, divided by \",\"").append("\n");
        sb.append("ldap.listing=ldap1").append("\n");

        sb.append("ldap1.base_dn=dc=example,dc=com").append("\n");
        sb.append("ldap1.ou_people=ou=People").append("\n");
        sb.append("ldap1.ou_group=ou=Group").append("\n");
        sb.append("ldap1.url=ldaps://localhost:636").append("\n");
        sb.append("ldap1.principal=cn=admin,dc=example,dc=com").append("\n");
        sb.append("ldap1.credentials=secret").append("\n");
        sb.append("\n");
        return sb.toString();
    }

    private static void help() {
        System.out.println("\nHELP\n" + HR);
        System.out.println("java -jar ldap-connetor.jar ");
        System.out.println("  -help                  this help");
        System.out.println("  -generate              generates an example liqid.properties as output.");
        System.out.println("  -generate -out FILE    generates an example liqid.properties at FILE");
        System.out.println(HR + "\n");
        System.exit(0);
    }
}
