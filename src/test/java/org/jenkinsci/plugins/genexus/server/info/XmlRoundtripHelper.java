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
import org.jenkinsci.plugins.genexus.helpers.XmlHelper;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author jlr
 */
public class XmlRoundtripHelper {

    public static <T> T testRoundTrip(T input, String caseDescription, Class<T> tClass) throws Exception {
        System.out.println("Case: " + caseDescription);

        String xmlString = XmlHelper.createXml(input);
        T output = XmlHelper.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8)), tClass);
        assertEquals(input, output);
        return output;
    }

    public static <T> void testRoundTrip(String inputString, String caseDescription, Class<T> tClass) throws Exception {
        T inputList = XmlHelper.parse(new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8)), tClass);
        T outputList = testRoundTrip(inputList, caseDescription, tClass);

        String normalizedInput = XmlHelper.normalizeXmlString(inputString);
        String normalizedOutput = XmlHelper.createXml(outputList, true);
        assertEquals(normalizedInput, normalizedOutput);
    }
}
