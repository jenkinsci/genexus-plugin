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
import java.util.Map;
import javax.xml.ws.BindingProvider;
import org.jenkinsci.plugins.genexus.server.services.common.NaiveSSLHelper;
import org.jenkinsci.plugins.genexus.server.services.common.ServiceData;
import org.jenkinsci.plugins.genexus.server.services.common.ServiceInfo;
import org.jenkinsci.plugins.genexus.server.services.teamwork.ITeamWorkService2;
import org.jenkinsci.plugins.genexus.server.services.teamwork.TeamWorkService2;

/**
 *
 * @author jlr
 */
public class TeamWorkService2Client extends BaseClient {
    
    private static final ServiceInfo TEAMWORK_SERVICE2_INFO = new ServiceInfo(
            "TeamWorkService2.svc/secure",
            "TeamWorkService2.svc",
            "CustomBinding_ITeamWorkService2",
            "BasicHttpBinding_ITeamWorkService2"
    );

    @Override
    protected ServiceInfo getServiceInfo() {
        return TEAMWORK_SERVICE2_INFO;
    }
    
    public TeamWorkService2Client(String serverURL, String user, String password) throws MalformedURLException {
        super(new ServiceData(serverURL, user, password));
    }
    
    private ITeamWorkService2 teamWorkService2 = null;

    private ITeamWorkService2 getTeamWorkService2() throws IOException {
        if (teamWorkService2 == null) {
            BindingData bindingData = getBindingData(serviceData);
            TeamWorkService2 service = new TeamWorkService2();

            ITeamWorkService2 port = bindingData.isSecure
                    ? service.getCustomBindingITeamWorkService2()
                    : service.getCustomBindingITeamWorkService2();

            BindingProvider bindingProvider = (BindingProvider) port;
            Map requestContext = bindingProvider.getRequestContext();

            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, bindingData.url.toString());
            if (bindingData.isSecure) {
                requestContext.put(BindingProvider.USERNAME_PROPERTY, serviceData.getUserName());
                requestContext.put(BindingProvider.PASSWORD_PROPERTY, serviceData.getUserPassword());
            }

            NaiveSSLHelper.makeWebServiceClientTrustEveryone(port);

            teamWorkService2 = port;
        }
        return teamWorkService2;
    }
    
    public Boolean CheckService() throws IOException {
        ITeamWorkService2 service = getTeamWorkService2();
        return true;
    }
            
}
