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
package org.jenkinsci.plugins.genexus.server.clients.common;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jlr
 */
public class ServiceData {

    private final URL serverURL;
    private final String userName;
    private final String userPassword;

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";

    public ServiceData(URL serverURL, String userName, String userPassword) {
        this.serverURL = serverURL;
        this.userName = userName;
        this.userPassword = userPassword;
    }

    public ServiceData(String serverPath, String user, String password) throws MalformedURLException {
        this(serverPath, user, password, HTTPS_PROTOCOL);
    }

    public ServiceData(String serverPath, String user, String password, String protocol) throws MalformedURLException {
        this(createURL(serverPath, protocol), user, password);
    }

    private static URL createURL(String serverPath, String protocol) throws MalformedURLException {
        if (StringUtils.isBlank(serverPath)) {
            throw new IllegalArgumentException("serverPath");
        }

        URL url = new URL(serverPath);
        return new URL(protocol, url.getHost(), url.getPort(), url.getFile());
    }

    public URL getServerURL() {
        return serverURL;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public static final String GXSERVER_ISSECURE_PROPERTY = "org.jenkinsci.plugins.genexus.server.services.common.servicedata.issecure";
    public static final String GXSERVER_USERNAME_PROPERTY = "org.jenkinsci.plugins.genexus.server.services.common.serviceinfo.username";
    public static final String GXSERVER_PASSWORD_PROPERTY = "org.jenkinsci.plugins.genexus.server.services.common.serviceinfo.password";
}
