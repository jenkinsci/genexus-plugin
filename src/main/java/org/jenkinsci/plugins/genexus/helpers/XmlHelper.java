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
package org.jenkinsci.plugins.genexus.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author jlr
 */
public class XmlHelper {

    public static <T> T parse(InputStream stream, Class<T> tClass) throws IOException, ParserConfigurationException, SAXException, JAXBException {
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8.name());
        InputSource source = new InputSource(reader);

        return XmlHelper.parse(source, tClass);
    }

    public static <T> T parse(InputSource source, Class<T> tClass) throws IOException, ParserConfigurationException, SAXException, JAXBException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(source);

        return XmlHelper.parse(doc, tClass);
    }

    public static <T> T parse(Document doc, Class<T> tClass) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(tClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        T result = (T) jaxbUnmarshaller.unmarshal(doc);
        return result;
    }

    public static <T> String createXml(T instance) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(instance.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        StringWriter writer = new StringWriter();
        jaxbMarshaller.marshal(instance, writer);
        return writer.toString();
    }
}
