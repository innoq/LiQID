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
package com.innoq.ldap.ssl;

import com.innoq.liqid.utils.Configuration;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SelfSignedSSLSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory socketFactory;

    public SelfSignedSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException, IOException, FileNotFoundException, CertificateException {
        KeyStore keyStore = getCustomKeystore();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
        socketFactory = ctx.getSocketFactory();
    }

    public static SSLSocketFactory getDefault() {
        try {
            return new SelfSignedSSLSocketFactory();
        } catch (NoSuchAlgorithmException ex) {
            return null;
        } catch (KeyManagementException|KeyStoreException|UnrecoverableKeyException ex) {
            return null;
        } catch (IOException|CertificateException ex) {
            return null;
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return socketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return socketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String string, int i, boolean bln) throws IOException {
        return socketFactory.createSocket(socket, string, i, bln);
    }

    @Override
    public Socket createSocket(String string, int i) throws IOException, UnknownHostException {
        return socketFactory.createSocket(string, i);
    }

    @Override
    public Socket createSocket(String string, int i, InetAddress ia, int i1) throws IOException, UnknownHostException {
        return socketFactory.createSocket(string, i, ia, i1);
    }

    @Override
    public Socket createSocket(InetAddress ia, int i) throws IOException {
        return socketFactory.createSocket(ia, i);
    }

    @Override
    public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException {
        return socketFactory.createSocket(ia, i, ia1, i1);
    }

    private KeyStore getCustomKeystore() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, FileNotFoundException, IOException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance("JKS"); /* Get a keystore containing the self-signed certificate) */

        String keystorePassword, keystorePath;
        keystorePassword = Configuration.getProperty("keystore.password");
        keystorePath = Configuration.getProperty("keystore.path");
        if (keystorePassword != null && keystorePath != null) {
            InputStream readStream = new FileInputStream(keystorePath);
            keyStore.load(readStream, keystorePassword.toCharArray());
            Key key = keyStore.getKey("keyAlias", keystorePassword.toCharArray());
            readStream.close();
        }
        return keyStore;
    }
}
