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
import java.io.InputStream;
import java.nio.charset.Charset;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jlr
 */
public class KBListTest {

    public KBListTest() {
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

    /**
     * Test of parse method, of class KBList.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testParse_InputStream() throws Exception {

        final String xmlSample
                = "<KnowledgeBases>\n"
                + "  <KB Name=\"Ñómvrë çòmplikâdo\""
                + "    Description=\"asdfasdfasdf\""
                + "    URL=\"\""
                + "    Tags=\"\""
                + "    TeamDevMode=\"Yes\""
                + "    PublishUser=\"NT AUTHORITY\\SYSTEM\""
                + "    PublishDate=\"Monday, 05 August 2019\""
                + "    KBImage=\"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\""
                + "  />\n"
                + "  <KB Name=\"BC\""
                + "    Description=\"\""
                + "    URL=\"https://example.com/kb1\""
                + "    Tags=\"\""
                + "    TeamDevMode=\"Yes\""
                + "    PublishUser=\"local\\admin\""
                + "    PublishDate=\"Saturday, 22 June 2019\""
                + "    KBImage=\"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\""
                + "  />"
                + "</KnowledgeBases>\n";

        System.out.println("parse");
        InputStream stream = new ByteArrayInputStream(xmlSample.getBytes(Charset.forName("UTF-8")));

        KBList result = KBList.parse(stream);
        assertNotNull(result);
        assertNotNull(result.getKBs());

        assertEquals(result.getKBs().size(), 2);
    }
}
