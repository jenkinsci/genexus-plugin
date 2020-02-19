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
package org.jenkinsci.plugins.genexus.server.info;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jlr
 */
public class RevisionListTest {

    public RevisionListTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    protected String getEmptyCase() {
        return "<Revisions/>";
    }

    protected String getCase1() {
        return "<Revisions>\n"
                + "    <Revision guid=\"8a392a49-2f62-4b47-b1bd-4b215dafbac1\" user=\"Local\\admin\" commitDate=\"12/31/2019 16:19:33\" IsDisabled=\"false\" Comment=\"Updated  modules - hiragana (平仮名) and katakana (片仮名) – and kanji (漢字)\" RevisionId=\"3\">\n"
                + "    <Actions>\n"
                + "      <Action guid=\"4f454e73-7d8f-4a0f-908a-1a355f3634a5\" key=\"c88fffcd-b6f8-0000-8fec-00b5497e2117-2\" typeDescriptor=\"Module\" name=\"GeneXus\" description=\"GeneXus Core Modules\" operation=\"Modified\" userName=\"GeneXus\" timestamp=\"12/04/2019 16:36:10\" />\n"
                + "      <Action guid=\"fb4bd2a4-ed0e-4418-8a55-860dd04d4ad3\" key=\"c88fffcd-b6f8-0000-8fec-00b5497e2117-16\" typeDescriptor=\"Module\" name=\"GeneXusSecurityCommon\" description=\"GeneXus Security Common module contains all GAM public domains. This module is installed in the KB when integrated security property (GAM) is activated.\" operation=\"Modified\" userName=\"GeneXus\" timestamp=\"12/06/2019 17:45:18\" />\n"
                + "      <Action guid=\"9c06daaa-8132-41f6-a0f7-3afc9b0ab999\" key=\"c88fffcd-b6f8-0000-8fec-00b5497e2117-17\" typeDescriptor=\"Module\" name=\"GeneXusSecurity\" description=\"GeneXus Security module is a set of interfaces used to access GAM API. This module is installed in the KB when integrated security property (GAM) is activated.  \" operation=\"Modified\" userName=\"GeneXus\" timestamp=\"12/06/2019 19:45:27\" />\n"
                + "    </Actions>\n"
                + "  </Revision>\n"
                + "  <Revision guid=\"9bb1e743-246b-4bb1-80f7-8f5630eafb5b\" user=\"Local\\admin\" commitDate=\"12/07/2019 21:22:52\" IsDisabled=\"false\" Comment=\"Enabled GAM\" RevisionId=\"2\">\n"
                + "    <Actions>\n"
                + "      <Action guid=\"c8ca22d3-7a2c-438e-a1b3-1c592319195c\" key=\"00000000-0000-0000-0000-000000000010-0\" typeDescriptor=\"\" name=\"EnableGAM\" description=\"Version Properties\" operation=\"Modified\" userName=\"local\\admin\" />\n"
                + "      <Action guid=\"05a06416-797b-41cd-9b4b-c185a4f31670\" key=\"00000000-0000-0000-0000-000000000002-2\" typeDescriptor=\"MODEL\" name=\".Net Environment\" description=\"Environment Properties\" operation=\"Modified\" userName=\"local\\admin\" />\n"
                + "      <Action guid=\"5a2b3396-62a6-45a0-928e-41775485e855\" key=\"00000000-0000-0000-0000-000000000006-2\" typeDescriptor=\"Category\" name=\"GAM\" description=\"GAM\" operation=\"Inserted\" userName=\"ARTECH\\JLR\" timestamp=\"12/07/2019 21:17:17\" />\n"
                + "      <Action guid=\"ff8995f9-8b6e-43c0-a00a-77c0d26091e8\" key=\"00000000-0000-0000-0000-000000000006-3\" typeDescriptor=\"Category\" name=\"ToBeDefined\" description=\"To Be Defined\" operation=\"Inserted\" userName=\"ARTECH\\JLR\" timestamp=\"12/07/2019 21:17:18\" />\n"
                + "      <Action guid=\"fb4bd2a4-ed0e-4418-8a55-860dd04d4ad3\" key=\"c88fffcd-b6f8-0000-8fec-00b5497e2117-16\" typeDescriptor=\"Module\" name=\"GeneXusSecurityCommon\" description=\"GeneXus Security Common module contains all GAM public domains. This module is installed in the KB when integrated security property (GAM) is activated.\" operation=\"Inserted\" userName=\"GeneXus\" timestamp=\"12/06/2019 05:41:55\" />\n"
                + "      <Action guid=\"9c06daaa-8132-41f6-a0f7-3afc9b0ab999\" key=\"c88fffcd-b6f8-0000-8fec-00b5497e2117-17\" typeDescriptor=\"Module\" name=\"GeneXusSecurity\" description=\"GeneXus Security module is a set of interfaces used to access GAM API. This module is installed in the KB when integrated security property (GAM) is activated.  \" operation=\"Inserted\" userName=\"GeneXus\" timestamp=\"12/06/2019 07:30:09\" />\n"
                + "      <Action guid=\"6ac49ef5-e1eb-459c-aefd-331de0d9fa8c\" key=\"88313f43-5eb2-0000-0028-e8d9f5bf9588-1\" typeDescriptor=\"Language\" name=\"English\" description=\"English\" operation=\"Modified\" userName=\"genexus\\genexus\" timestamp=\"04/03/2012 18:05:36\" />\n"
                + "      <Action guid=\"2929579a-3d77-416d-9c5a-7b33ba32efb7\" key=\"dcdcdcdc-dfe0-4a57-ae8f-c6e31b0dcbc0-2\" typeDescriptor=\"Data Store\" name=\"GAM\" description=\"GAM\" operation=\"Inserted\" userName=\"ARTECH\\JLR\" timestamp=\"12/07/2019 21:18:22\" />\n"
                + "    </Actions>\n"
                + "  </Revision>\n"
                + "  <Revision guid=\"7499fd53-4c38-4398-8703-82fe319b76ba\" user=\"Local\\admin\" commitDate=\"12/07/2019 21:15:43\" IsDisabled=\"true\" Comment=\"EnableGAM is now hosted by GeneXus Server\" RevisionId=\"1\">\n"
                + "    <Actions />\n"
                + "  </Revision>\n"
                + "</Revisions>";
    }

    @Test
    public void testXmlSerialization() throws Exception {
        System.out.println("XmlSerialization");

        XmlRoundtripHelper.testRoundTrip(getEmptyCase(), "Empty List", RevisionList.class);
        XmlRoundtripHelper.testRoundTrip(getCase1(), "1", RevisionList.class);
    }
}
