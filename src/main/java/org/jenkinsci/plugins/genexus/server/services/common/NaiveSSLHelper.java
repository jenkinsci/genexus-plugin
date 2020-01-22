/*
 * The MIT License
 *
 * Copyright 2020 GeneXus S.A..
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.genexus.server.services.common;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.ws.BindingProvider;

/**
 * Ideas and code taken from
 * https://erikwramner.wordpress.com/2013/03/27/trust-self-signed-ssl-certificates-and-skip-host-name-verification-with-jax-ws
 * by Erik Wramner https://github.com/erik-wramner and
 * http://www.howardism.org/Technical/Java/SelfSignedCerts.html by Howard Abrams
 * https://github.com/howardabrams
 *
 *
 * @author jlr
 */
public class NaiveSSLHelper {

    public static void makeWebServiceClientTrustEveryone(Object webServicePort) {
        if (webServicePort instanceof BindingProvider) {
            BindingProvider bp = (BindingProvider) webServicePort;
            Map requestContext = bp.getRequestContext();
            requestContext.put(JAXWS_SSL_SOCKET_FACTORY, getTrustingSSLSocketFactory());
            requestContext.put(JAXWS_HOSTNAME_VERIFIER, new NaiveHostnameVerifier());
        } else {
            throw new IllegalArgumentException(
                    "Web service port "
                    + webServicePort.getClass().getName()
                    + " does not implement "
                    + BindingProvider.class.getName());
        }
    }

    public static void makeHttpsURLConnectionTrustEveryone(HttpsURLConnection connection) {
        connection.setHostnameVerifier(new NaiveHostnameVerifier());
        connection.setSSLSocketFactory(getTrustingSSLSocketFactory());
    }
    
    public static SSLSocketFactory getTrustingSSLSocketFactory() {
        return SSLSocketFactoryHolder.INSTANCE;
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        TrustManager[] trustManagers = new TrustManager[]{new NaiveTrustManager()};
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(new KeyManager[0], trustManagers, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            return null;
        }
    }

    private static interface SSLSocketFactoryHolder {

        public static final SSLSocketFactory INSTANCE = createSSLSocketFactory();
    }

    private static class NaiveHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostName, SSLSession session) {
            return true;
        }
    }

    /**
     * This Trust Manager is "naive" because it trusts everyone. 
     **/    
    private static class NaiveTrustManager implements X509TrustManager {
        /**
         * Doesn't throw an exception, so this is how it approves a certificate.
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], String)
         **/
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        /**
         * Doesn't throw an exception, so this is how it approves a certificate.
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], String)
         * */
        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static final String JAXWS_HOSTNAME_VERIFIER
            = "com.sun.xml.internal.ws.transport.https.client.hostname.verifier";
            //= JAXWSProperties.HOSTNAME_VERIFIER;
    private static final String JAXWS_SSL_SOCKET_FACTORY
            = "com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory";
            //= JAXWSProperties.SSL_SOCKET_FACTORY;
}
