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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.jenkinsci.plugins.genexus.helpers.XmlHelper;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jlr
 */
public class VersionListTest {

    public VersionListTest() {
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
        return "<Versions/>";
    }

    protected VersionList getCase1() {
        VersionList list = new VersionList();

        VersionInfo item;

        item = new VersionInfo();
        item.remindsCount = 0;
        item.type = UUID.fromString("00000000-0000-0000-0000-000000000010");
        item.id = 0;
        item.name = "SomeKBName";
        item.guid = UUID.fromString("c8ca22d3-7a2c-438e-a1b3-1c592319195c");
        item.isTrunk = true;

        item = new VersionInfo();
        item.name = "Ñómvrë çòmplikâdo";
        item.type = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
        item.id = 42;
        item.guid = UUID.fromString("123e4567-e89b-12d3-a456-426655440001");
        item.isTrunk = true;
        item.parentId = 0;
        item.isFrozen = false;

        item = new VersionInfo();
        item.name = "branch";
        item.type = UUID.fromString("123e4567-e89b-12d3-a456-426655440000");
        item.id = 48;
        item.guid = UUID.fromString("123e4567-e89b-12d3-a456-426655440002");
        item.isTrunk = false;
        item.parentId = 42;
        item.isFrozen = true;

        return list;
    }

    protected String getCase2() {
        return "<Versions>\n"
                + "  <Version remindsCount=\"0\" type=\"00000000-0000-0000-0000-000000000010\" id=\"0\" name=\"EnableGAM\" guid=\"c8ca22d3-7a2c-438e-a1b3-1c592319195c\" isTrunk=\"True\" />\n"
                + "  <Version remindsCount=\"0\" type=\"00000000-0000-0000-0000-000000000010\" id=\"2\" name=\"BeforeOperationalTables\" guid=\"9337adb2-22bc-44e2-9a6c-e151e4aaebb6\" isFrozen=\"True\" />\n"
                + "  <Version remindsCount=\"0\" type=\"00000000-0000-0000-0000-000000000010\" id=\"3\" name=\"OperationalTables\" guid=\"a7a499fd-dfc3-44cb-8889-19ab73e8dd20\" parentId=\"2\" />\n"
                + "  <Version remindsCount=\"0\" type=\"00000000-0000-0000-0000-000000000010\" id=\"5\" name=\"BeforeGAMUpdate\" guid=\"ea64282c-3154-45da-b20e-ff1d0a63df47\" parentId=\"3\" isFrozen=\"True\" />\n"
                + "  <Version remindsCount=\"0\" type=\"00000000-0000-0000-0000-000000000010\" id=\"6\" name=\"GAMUpdate\" guid=\"95954da9-79bc-4419-bd83-0902bc7bba50\" parentId=\"5\" />\n"
                + "</Versions>";
    }

    /**
     * Test of parse method, of class KBList.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testXmlSerialization() throws Exception {

        System.out.println("XmlSerialization");

        XmlRoundtripHelper.testRoundTrip(getEmptyCase(), "Empty List", VersionList.class);
        XmlRoundtripHelper.testRoundTrip(getCase1(), "Phabricated", VersionList.class);
        XmlRoundtripHelper.testRoundTrip(getCase2(), "Obtained from an actual GXserver", VersionList.class);
    }
}
