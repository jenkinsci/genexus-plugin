/*
 * The MIT License
 *
 * Copyright 2018 GeneXus S.A..
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
package com.genexus.gxserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author jlr
 */
public final class MsBuildArgsHelper {

    private static final String TARGET = "/t:";
    private static final String NO_LOGO = "/nologo";
    private static final String PROPERTY = "/p:";
    private static final String VALUE_SEPARATOR = ";";
    private static final String PARM_SEPARATOR = " ";
    private static final String QUOTE = "\"";

    private final ArrayList<String> simpleParms = new ArrayList<>();
    private final Properties valuedParms = new Properties();
    private final HashMap<String, ArrayList<String>> multiValuedParms = new HashMap<>();

    public MsBuildArgsHelper(String... targetNames) {
        for (String targetName : targetNames) {
            addTarget(targetName);
        }
    }

    public void addTarget(String targetName) {
        addMultiValueParm(TARGET, targetName);
    }

    public void addProperty(String propName, String propValue) {
        addMultiValueParm(PROPERTY, propName + "=" + quoteIfNeeded(propValue));
    }

    public void addProperty(String propName, Object propValue) {
        addProperty(propName, propValue.toString());
    }

    public void addNoLogo() {
        addParameter(NO_LOGO);
    }

    public void addParameter(String parmName) {
        simpleParms.add(parmName);
    }

    public void addParameterValue(String parmName, String parmValue) {
        valuedParms.put(parmName, parmValue);
    }

    public void addMultiValueParm(String parmName, String parmValue) {
        ArrayList<String> values = multiValuedParms.get(parmName);
        if (values == null) {
            values = new ArrayList<>();
            multiValuedParms.put(parmName, values);
        }

        values.add(parmValue);
    }

    @Override
    public String toString() {
        StringBuilder argsBuilder = new StringBuilder();

        for (String parm : simpleParms) {
            argsBuilder.append(parm)
                    .append(PARM_SEPARATOR);
        }

        for (Map.Entry<Object, Object> parm : valuedParms.entrySet()) {
            argsBuilder.append(parm.getKey())
                    .append(parm.getValue())
                    .append(PARM_SEPARATOR);
        }

        for (Map.Entry<String, ArrayList<String>> parm : multiValuedParms.entrySet()) {
            argsBuilder.append(parm.getKey())
                    .append(String.join(VALUE_SEPARATOR, parm.getValue()))
                    .append(PARM_SEPARATOR);
        }

        return argsBuilder.toString().trim();
    }

    private static String quoteIfNeeded(String value) {
        if (!value.contains(" ")) {
            return value;
        }

        return QUOTE + value + QUOTE;
    }
}
