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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOMFeature;
import org.jenkinsci.plugins.genexus.helpers.XmlHelper;
import org.jenkinsci.plugins.genexus.server.info.KBList;
import org.jenkinsci.plugins.genexus.server.info.VersionList;
import org.jenkinsci.plugins.genexus.server.services.common.ServiceData;
import org.jenkinsci.plugins.genexus.server.services.common.ServiceInfo;
import org.jenkinsci.plugins.genexus.server.services.common.TransferPropConstants;
import org.jenkinsci.plugins.genexus.server.services.common.TransferPropHelper;
import org.jenkinsci.plugins.genexus.server.services.contracts.ArrayOfServerMessage;
import org.jenkinsci.plugins.genexus.server.services.contracts.ArrayOfTransferProp;
import org.jenkinsci.plugins.genexus.server.services.teamwork.FileTransfer;
import org.jenkinsci.plugins.genexus.server.services.teamwork.SimpleTransfer;
import org.jenkinsci.plugins.genexus.server.services.teamwork.ITeamWorkService2;
import org.jenkinsci.plugins.genexus.server.services.teamwork.ITeamWorkService2GetVersionsGXServerExceptionFaultFaultMessage;
import org.jenkinsci.plugins.genexus.server.services.teamwork.ITeamWorkService2HostedKBsGXServerExceptionFaultFaultMessage;
import org.jenkinsci.plugins.genexus.server.services.teamwork.TeamWorkService2;
import org.xml.sax.SAXException;

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
            TeamWorkService2 service = new TeamWorkService2();
            ITeamWorkService2 port = service.getCustomBindingITeamWorkService2(new MTOMFeature(true));

            PrepareClient((BindingProvider) port);

            teamWorkService2 = port;
        }

        return teamWorkService2;
    }

    public KBList GetHostedKBs() throws IOException {
        try {
            SimpleTransfer parameters = new SimpleTransfer();
            Holder<ArrayOfServerMessage> messages = new Holder<>(new ArrayOfServerMessage());
            Holder<ArrayOfTransferProp> properties = getBasicProperties();

            FileTransfer transfer = getTeamWorkService2().hostedKBs(parameters, messages, properties);
            byte[] bytes = transfer.getFileByteStream();
            //String xmlContent = getString(bytes);
            InputStream stream = new ByteArrayInputStream(bytes);
            
            return XmlHelper.parse(stream, KBList.class);
        } catch (ITeamWorkService2HostedKBsGXServerExceptionFaultFaultMessage ex) {
            Logger.getLogger(TeamWorkService2Client.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("Error accessing GXserver", ex);
        } catch (SAXException | ParserConfigurationException | JAXBException ex) {
            Logger.getLogger(TeamWorkService2Client.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("Failed to parse KB list", ex);
        }
    }

    public VersionList GetVersions(String KBname) throws IOException {
        try {
            SimpleTransfer parameters = new SimpleTransfer();
            Holder<ArrayOfServerMessage> messages = new Holder<>(new ArrayOfServerMessage());
            Holder<ArrayOfTransferProp> properties = getBasicProperties();

            properties.value.getTransferProp().add(
                    TransferPropHelper.CreateStringProp(TransferPropConstants.SERVER_KB_NAME, KBname)
            );

            FileTransfer transfer = getTeamWorkService2().getVersions(parameters, messages, properties);
            byte[] bytes = transfer.getFileByteStream();
            //String xmlContent = getString(bytes);
            InputStream stream = new ByteArrayInputStream(bytes);
            
            return XmlHelper.parse(stream, VersionList.class);
        } catch (ITeamWorkService2GetVersionsGXServerExceptionFaultFaultMessage ex) {
            Logger.getLogger(TeamWorkService2Client.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("Error accessing GXserver", ex);
        } catch (SAXException | ParserConfigurationException | JAXBException ex) {
            Logger.getLogger(TeamWorkService2Client.class.getName()).log(Level.SEVERE, null, ex);
            throw new IOException("Failed to parse Version list", ex);
        }
    }

    private Holder<ArrayOfTransferProp> getBasicProperties() {
        Holder<ArrayOfTransferProp> properties = new Holder<>(new ArrayOfTransferProp());

        properties.value.getTransferProp().addAll(Arrays.asList(
                TransferPropHelper.CreateStringProp(TransferPropConstants.CLIENT_GXVERSION, getClientVersion()),
                TransferPropHelper.CreateStringProp(TransferPropConstants.CLIENT_USER, "Anonymous"),
                TransferPropHelper.CreateGuidProp(TransferPropConstants.CLIENT_INSTANCE, UUID.randomUUID().toString())
        ));
        return properties;
    }
    private String getString(byte[] bytes) throws IOException {
        String s = new String(bytes, StandardCharsets.UTF_8);
        return s;
    }
}
