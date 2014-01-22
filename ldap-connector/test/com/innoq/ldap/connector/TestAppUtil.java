/*
 Copyright (C) 2014 innoQ Deutschland GmbH

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

import com.innoq.ldap.util.App;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TestAppUtil {

    private static final Logger LOG = Logger.getLogger(TestAppUtil.class.getName());
    private PrintStream ps;
    private ByteArrayOutputStream baos;
    private String content;


    @Test
    public void testHelp() throws UnsupportedEncodingException {
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        String[] args = {};
        App.setOutStream(ps);
        App.main(args);
        content = baos.toString("utf-8");
        assertTrue(content.length() > 0);
        System.out.println(content);
    }

    @Test
    public void testGenerator() throws UnsupportedEncodingException {
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        String[] args = {"-generate"};
        App.setOutStream(ps);
        App.main(args);
        content = baos.toString("utf-8");
        assertTrue(content.length() > 0);
        System.out.println(content);
    }
}
