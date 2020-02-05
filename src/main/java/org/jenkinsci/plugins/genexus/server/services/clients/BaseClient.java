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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.BindingProvider;
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

    private BindingData bindingData;

    protected BindingData getBindingData() throws MalformedURLException {
        if (bindingData == null) {
            bindingData = getBindingData(serviceData);
        }
        return bindingData;
    }

    private BindingData getBindingData(ServiceData serviceData) throws MalformedURLException {
        ServiceInfo serviceInfo = getServiceInfo();

        BindingData binding = new BindingData();

        URL secureURL = getSecureServiceURL(serviceData.getServerURL());
        binding.url = secureURL;
        binding.isSecure = true;
        binding.bindingName = serviceInfo.secureBindingName;

        if (checkForNotFound(secureURL)) {
            // If the "secure" URL is not found (404) it might be an old gxserver
            // configured without authentication.

            // If the "non-secure" URL does resolve the (404) we fall back on it.
            URL nonSecureURL = getNonSecureServiceURL(serviceData.getServerURL());
            if (!checkForNotFound(nonSecureURL)) {
                binding.url = nonSecureURL;
                binding.isSecure = false;
                binding.bindingName = serviceInfo.nonSecureBindingName;
            }
        }

        return binding;
    }

    protected void PrepareClient(BindingProvider bindingProvider) throws MalformedURLException {
        AddMessageContextProperties(bindingProvider);
        NaiveSSLHelper.makeWebServiceClientTrustEveryone(bindingProvider);
    }

    protected void AddMessageContextProperties(BindingProvider bindingProvider) throws MalformedURLException {
        BindingData binding = getBindingData();
        Map requestContext = bindingProvider.getRequestContext();

        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, binding.url.toString());

        requestContext.put(ServiceData.GXSERVER_ISSECURE_PROPERTY, Boolean.toString(binding.isSecure));
        if (binding.isSecure) {
            requestContext.put(ServiceData.GXSERVER_USERNAME_PROPERTY, serviceData.getUserName());
            requestContext.put(ServiceData.GXSERVER_PASSWORD_PROPERTY, serviceData.getUserPassword());
        }
    }

    private Boolean checkForNotFound(URL url)
    {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            if (connection == null)
                return false;

            connection.setRequestMethod("HEAD");

            if (connection instanceof HttpsURLConnection) {
                NaiveSSLHelper.makeHttpsURLConnectionTrustEveryone((HttpsURLConnection)connection);
            }

            int response = connection.getResponseCode();
            return response == HttpURLConnection.HTTP_NOT_FOUND;
        } catch (IOException ioEx) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private URL getServiceURL(URL baseURL, Boolean secure) throws MalformedURLException {
        return BaseClient.getServiceURL(
                baseURL,
                secure? getServiceInfo().secureIdentifier : getServiceInfo().nonSecureIdentifier,
                secure);

    }

    private URL getSecureServiceURL(URL baseURL) throws MalformedURLException {
        return getServiceURL(baseURL, true);
    }

    private URL getNonSecureServiceURL(URL baseURL) throws MalformedURLException {
        return getServiceURL(baseURL, false);
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
