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

import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jlr
 */
public class MsBuildArgumentListBuilder extends ArgumentListBuilder {

    private static final String TARGET_PREFIX = "/t:";
    private static final String NO_LOGO = "/nologo";
    private static final String PROPERTY_PREFIX = "/p:";
    private static final String VALUE_SEPARATOR = ";";
    
    public MsBuildArgumentListBuilder() {
    }
    
    public MsBuildArgumentListBuilder(String msBuildFile) {
        setMsBuildFile(msBuildFile);
    }
    
    public void addNoLogo() {
        add(NO_LOGO);
    }
    
    final void setMsBuildFile(String msBuildFile) {
        add(msBuildFile);
    }
    
    public void addTarget(String targetName) {
        addValuedParameter(TARGET_PREFIX, targetName);
    }
    
    public void addTargets(String... targetNames) {
        String allTargets = StringUtils.join(targetNames, VALUE_SEPARATOR);
        addTarget(allTargets);
    }
    
    public void addProperty(String propName, String propValue) {
        addProperty(propName, propValue, false);
    }
    
    public void addProperty(String propName, String propValue, boolean masked) {
        addKeyValuePair(PROPERTY_PREFIX, propName, propValue, masked);
    }
    
    public void addProperty(String propName, Object propValue) {
        addProperty(propName, propValue, false);
    }
    
    public void addProperty(String propName, Object propValue, boolean masked) {
        addKeyValuePair(PROPERTY_PREFIX, propName, propValue.toString(), masked);
    }
    
    public void addParameter(String parameter) {
        add(parameter);
    }
    
    public void addValuedParameter(String parmPrefix, String parmValue) {
        add(parmPrefix + parmValue);
    }
}
