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
package org.jenkinsci.plugins.genexus.server.clients.common;

import java.io.IOException;
import java.text.MessageFormat;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jenkinsci.plugins.genexus.server.services.contracts.BoolProp;
import org.jenkinsci.plugins.genexus.server.services.contracts.DateTimeProp;
import org.jenkinsci.plugins.genexus.server.services.contracts.GuidProp;
import org.jenkinsci.plugins.genexus.server.services.contracts.IntProp;
import org.jenkinsci.plugins.genexus.server.services.contracts.LongProp;
import org.jenkinsci.plugins.genexus.server.services.contracts.ObjectFactory;
import org.jenkinsci.plugins.genexus.server.services.contracts.StringProp;
import org.jenkinsci.plugins.genexus.server.services.contracts.TransferProp;
import org.jenkinsci.plugins.genexus.server.services.contracts.XmlProp;

/**
 *
 * @author jlr
 */
public class TransferPropHelper {

    public static StringProp createStringProp(String propName, String propValue) {
        ObjectFactory of = new ObjectFactory();
        StringProp sp = of.createStringProp();
        sp.setName(propName);
        sp.setValue(propValue);
        return sp;
    }

    public static GuidProp createGuidProp(String propName, String propValue) {
        ObjectFactory of = new ObjectFactory();
        GuidProp sp = of.createGuidProp();
        sp.setName(propName);
        sp.setValue(propValue);
        return sp;
    }

    public static IntProp createIntProp(String propName, int propValue) {
        ObjectFactory of = new ObjectFactory();
        IntProp sp = of.createIntProp();
        sp.setName(propName);
        sp.setValue(propValue);
        return sp;
    }

    private static String getMessageForWrongType(TransferProp value, String expected) {
        return MessageFormat.format("Unexpected value type: {0. Was expecting {1}}", value.getClass().getName(), expected);
    }

    public static Boolean getBooleanValue(TransferProp prop) throws IOException {
        if (prop instanceof BoolProp) {
            return ((BoolProp) prop).isValue();
        }

        throw new IOException(getMessageForWrongType(prop, BoolProp.class.getName()));
    }

    public static XMLGregorianCalendar getDateTimeValue(TransferProp prop) throws IOException {
        if (prop instanceof DateTimeProp) {
            return ((DateTimeProp) prop).getValue();
        }

        throw new IOException(getMessageForWrongType(prop, DateTimeProp.class.getName()));
    }

    public static String getGuidValue(TransferProp prop) throws IOException {
        if (prop instanceof GuidProp) {
            return ((GuidProp) prop).getValue();
        }

        throw new IOException(getMessageForWrongType(prop, GuidProp.class.getName()));
    }

    public static int getIntValue(TransferProp prop) throws IOException {
        if (prop instanceof IntProp) {
            return ((IntProp) prop).getValue();
        }

        throw new IOException(getMessageForWrongType(prop, IntProp.class.getName()));
    }

    public static Long getLongValue(TransferProp prop) throws IOException {
        if (prop instanceof LongProp) {
            return ((LongProp) prop).getValue();
        }

        throw new IOException(getMessageForWrongType(prop, LongProp.class.getName()));
    }

    public static String getStringValue(TransferProp prop) throws IOException {
        if (prop instanceof StringProp) {
            return ((StringProp) prop).getValue();
        }

        throw new IOException(getMessageForWrongType(prop, StringProp.class.getName()));
    }

    public static String getXmlValue(TransferProp prop) throws IOException {
        if (prop instanceof XmlProp) {
            return ((XmlProp) prop).getValue();
        }

        throw new IOException(getMessageForWrongType(prop, XmlProp.class.getName()));
    }
}
