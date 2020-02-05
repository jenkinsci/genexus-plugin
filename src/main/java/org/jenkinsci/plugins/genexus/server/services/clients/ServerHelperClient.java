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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import org.jenkinsci.plugins.genexus.server.info.ServerInfo;
import org.jenkinsci.plugins.genexus.server.services.common.ServiceData;
import org.jenkinsci.plugins.genexus.server.services.common.ServiceInfo;
import org.jenkinsci.plugins.genexus.server.services.common.TransferPropConstants;
import org.jenkinsci.plugins.genexus.server.services.common.TransferPropHelper;
import org.jenkinsci.plugins.genexus.server.services.contracts.ArrayOfServerMessage;
import org.jenkinsci.plugins.genexus.server.services.contracts.ArrayOfTransferProp;
import org.jenkinsci.plugins.genexus.server.services.helper.IServerHelper;
import org.jenkinsci.plugins.genexus.server.services.helper.IServerHelperIsServerAliveGXServerExceptionFaultFaultMessage;
import org.jenkinsci.plugins.genexus.server.services.helper.IServerHelperServerInfoGXServerExceptionFaultFaultMessage;
import org.jenkinsci.plugins.genexus.server.services.helper.ServerHelper;
import org.jenkinsci.plugins.genexus.server.services.helper.SimpleTransfer;

/**
 *
 * @author jlr
 */
public class ServerHelperClient extends BaseClient {

    private static final ServiceInfo SERVER_HELPER_INFO = new ServiceInfo(
            "HelperService.svc/secure",
            "HelperService.svc",
            "CustomBinding_IServerHelper",
            "BasicHttpBinding_IServerHelper"
    );

    @Override
    protected ServiceInfo getServiceInfo() {
        return SERVER_HELPER_INFO;
    }

    public ServerHelperClient(String serverURL, String user, String password) throws MalformedURLException {
        super(new ServiceData(serverURL, user, password));
    }

    private IServerHelper serverHelper = null;

    private IServerHelper getServerHelper() throws IOException {
        if (serverHelper == null) {
            BindingData binding = getBindingData();
            ServerHelper service = new ServerHelper();

            IServerHelper port = binding.isSecure
                    ? service.getCustomBindingIServerHelper()
                    : service.getBasicHttpBindingIServerHelper();

            PrepareClient((BindingProvider) port);

            serverHelper = port;
        }
        return serverHelper;
    }

    public Boolean isServerAlive() throws IOException {
        try {
            return getServerHelper().isServerAlive(getClientVersion());
        } catch (IServerHelperIsServerAliveGXServerExceptionFaultFaultMessage ex) {
            Logger.getLogger(ServerHelperClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("Error accessing GXserver", ex);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ServerHelperClient.class.getName());

    public ServerInfo getServerInfo() throws IOException {
        try {
            Holder<SimpleTransfer> parameters = new Holder<>(new SimpleTransfer());
            Holder<ArrayOfServerMessage> messages = new Holder<>(new ArrayOfServerMessage());
            Holder<ArrayOfTransferProp> properties = new Holder<>(new ArrayOfTransferProp());

            properties.value.getTransferProp().add(
                    TransferPropHelper.CreateStringProp(TransferPropConstants.CLIENT_GXVERSION, getClientVersion())
            );

            properties.value.getTransferProp().add(
                    TransferPropHelper.CreateStringProp(TransferPropConstants.SERVER_OPERATION, "")
            );

            getServerHelper().serverInfo(parameters, messages, properties);

            ServerInfo serverInfo = new ServerInfo();

            properties.value.getTransferProp().forEach((prop) -> {
                switch (prop.getName()) {
                    case TransferPropConstants.SERVER_GXVERSION:
                        serverInfo.serverVersion = TransferPropHelper.getStringValue(prop);
                        break;

                    case TransferPropConstants.SERVER_AVAILABLE:
                        serverInfo.isAvailable = TransferPropHelper.getBooleanValue(prop);
                        break;

                    case TransferPropConstants.SERVER_SECURE:
                        serverInfo.isSecure = TransferPropHelper.getBooleanValue(prop);
                        break;
                    case TransferPropConstants.SUPPORTS_TOKEN_AUTHENTICATION:
                        serverInfo.supportsTokenAuthentication = TransferPropHelper.getBooleanValue(prop);
                        break;

                    case TransferPropConstants.ALLOWS_GXTEST:
                        serverInfo.allowsGXtest = TransferPropHelper.getBooleanValue(prop);
                        break;

                    case TransferPropConstants.SERVER_CUSTOM_BINDING:
                        serverInfo.allowsCustomBinding = TransferPropHelper.getBooleanValue(prop);
                        break;
                }
            });

            return serverInfo;
        } catch (IServerHelperServerInfoGXServerExceptionFaultFaultMessage ex) {
            Logger.getLogger(ServerHelperClient.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("Error accessing GXserver", ex);
        }
    }
}
