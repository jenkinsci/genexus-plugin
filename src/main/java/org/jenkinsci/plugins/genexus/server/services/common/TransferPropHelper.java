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
package org.jenkinsci.plugins.genexus.server.services.common;

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

    public static StringProp CreateStringProp(String propName, String propValue) {
        ObjectFactory of = new ObjectFactory();
        StringProp sp = of.createStringProp();
        sp.setName(propName);
        sp.setValue(propValue);
        return sp;
    }

    public static GuidProp CreateGuidProp(String propName, String propValue) {
        ObjectFactory of = new ObjectFactory();
        GuidProp sp = of.createGuidProp();
        sp.setName(propName);
        sp.setValue(propValue);
        return sp;
    }

    public static Boolean getBooleanValue(TransferProp prop) {
        return ((BoolProp) prop).isValue();
    }

    public static XMLGregorianCalendar getDateTimeValue(TransferProp prop) {
        return ((DateTimeProp) prop).getValue();
    }

    public static String getGuidValue(TransferProp prop) {
        return ((GuidProp) prop).getValue();
    }

    public static int getIntValue(TransferProp prop) {
        return ((IntProp) prop).getValue();
    }

    public static Long getLongValue(TransferProp prop) {
        return ((LongProp) prop).getValue();
    }

    public static String getStringValue(TransferProp prop) {
        return ((StringProp) prop).getValue();
    }

    public static String getXmlValue(TransferProp prop) {
        return ((XmlProp) prop).getValue();
    }
}
