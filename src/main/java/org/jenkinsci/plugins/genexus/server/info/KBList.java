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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
@XmlRootElement(name = "KnowledgeBases")
@XmlAccessorType(XmlAccessType.FIELD)
public class KBList {

    @XmlElement(name = "KB")
    private List<KbInfo> kbs = null;

    public List<KbInfo> getKBs() {
        return kbs;
    }

    public void setKBs(List<KbInfo> kbs) {
        this.kbs = kbs;
    }

    public static KBList parse(InputStream stream) throws IOException, ParserConfigurationException, SAXException, JAXBException {
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8.name());
        InputSource source = new InputSource(reader);

        return KBList.parse(source);
    }

    public static KBList parse(InputSource source) throws IOException, ParserConfigurationException, SAXException, JAXBException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(source);

        return KBList.parse(doc);
    }

    public static KBList parse(Document doc) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(KBList.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        KBList result = (KBList) jaxbUnmarshaller.unmarshal(doc);
        return result;
    }
}
