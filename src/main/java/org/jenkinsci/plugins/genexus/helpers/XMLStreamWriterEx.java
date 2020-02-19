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

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author jlr
 */
public class XMLStreamWriterEx extends IndentingXMLStreamWriter {
    
    public static XMLStreamWriterEx newInstance(File file) throws XMLStreamException, FileNotFoundException {
        return new XMLStreamWriterEx(XMLOutputFactory.newInstance().createXMLStreamWriter(new FileOutputStream(file)));
    }

    public XMLStreamWriterEx(XMLStreamWriter actualWriter) {
        super(actualWriter);
    }

    public AutoCloseable startDocument() throws XMLStreamException {
        return new CloseableDocument();
    }

    public AutoCloseable startElement(String elementName) throws XMLStreamException {
        return new CloseableElement(elementName);
    }

    public void writeSimpleElement(String name, String text) throws XMLStreamException {
        try (AutoCloseable element = startElement(name)) {
            writeCharacters(text);
        } catch (Exception ex) {
            throw new XMLStreamException("error closing element", ex);
        }
    }

    private class CloseableElement implements AutoCloseable {

        public CloseableElement(String elementName) throws XMLStreamException {
            writeStartElement(elementName);
        }

        @Override
        public void close() throws Exception {
            writeEndElement();
        }
    }

    private class CloseableDocument implements AutoCloseable {

        public CloseableDocument() throws XMLStreamException {
            writeStartDocument();
        }

        @Override
        public void close() throws Exception {
            writeEndDocument();
        }
    }
}
