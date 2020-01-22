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
package org.jenkinsci.plugins.genexus.server.services.clients;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.jenkinsci.plugins.genexus.server.services.common.NaiveSSLHelper;
import org.jenkinsci.plugins.genexus.server.services.common.ServiceData;
import org.jenkinsci.plugins.genexus.server.services.common.ServiceInfo;

/**
 *
 * @author jlr
 */
public abstract class BaseClient {

    private final static String SERVICE_CLIENT_VERSION = "16.0.1.0";
    
    protected String getClientVersion() {
        return SERVICE_CLIENT_VERSION;
    }
    
    protected final ServiceData serviceData;

    BaseClient(ServiceData data) {
        serviceData = data;
    }

    protected abstract ServiceInfo getServiceInfo();    
    
    protected static class BindingData {

        public boolean isSecure;
        public URL url;
        public String bindingName;
    }

    protected BindingData getBindingData(ServiceData serviceData) throws MalformedURLException {
        ServiceInfo serviceInfo = getServiceInfo();
        
        BindingData bindingData = new BindingData();
        bindingData.isSecure = true;
        bindingData.url = getServiceURL(serviceData.getServerURL(), serviceInfo.secureIdentifier, true);
        bindingData.bindingName = serviceInfo.secureBindingName;

        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) bindingData.url.openConnection();
            connection.setRequestMethod("HEAD");
            NaiveSSLHelper.makeHttpsURLConnectionTrustEveryone(connection);
            connection.connect();
        } catch (IOException ioEx) {
            bindingData.isSecure = false;
            bindingData.url = getServiceURL(serviceData.getServerURL(), serviceInfo.nonSecureIdentifier, false);
            bindingData.bindingName = serviceInfo.nonSecureBindingName;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return bindingData;
    }

    private static URL getServiceURL(URL serverURL, String service, boolean useHTTPS) throws MalformedURLException {
        String HTTP_PROTOCOL = "http";
        String HTTPS_PROTOCOL = "https";

        // choose scheme
        String scheme = useHTTPS ? HTTPS_PROTOCOL : HTTP_PROTOCOL;

        // add service query
        String path = serverURL.getFile();
        if (!path.endsWith("/")) {
            path += "/";
        }
        path += service;

        return new URL(scheme, serverURL.getHost(), serverURL.getPort(), path);
    }
}
