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
package org.jenkinsci.plugins.genexus.server;

import hudson.model.Run;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.digester3.Digester;
import org.jenkinsci.plugins.genexus.server.GXSChangeLogSet.Action;
import org.jenkinsci.plugins.genexus.server.GXSChangeLogSet.LogEntry;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author jlr
 */
public class GXSChangeLogParser extends ChangeLogParser {

    public GXSChangeLogParser() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public GXSChangeLogSet parse(@SuppressWarnings("rawtypes") Run build, RepositoryBrowser<?> browser, File changelogFile) throws IOException, SAXException {
        List<LogEntry> logs = parse(changelogFile);
        return new GXSChangeLogSet(build, browser, logs);
    }

    public static List<LogEntry> parse(File changelogFile) throws IOException {
        try (InputStream stream = new FileInputStream(changelogFile)) {
            InputSource source = new InputSource(stream);
            source.setSystemId(changelogFile.toURI().toURL().toString());
            return parse(source);
        }
    }

    public static List<LogEntry> parse(InputStream stream) throws IOException {
        InputSource source = new InputSource(stream);
        return parse(source);
    }

    private static List<LogEntry> parse(InputSource source) throws IOException {
        ArrayList<LogEntry> logs = new ArrayList<>();

        try {
            Digester digester = createDigester(logs);
            digester.parse(source);
        } catch (IOException | SAXException e) {
            String sourceId = source.getSystemId() != null ? source.getSystemId() : "";
            throw new IOException("Failed to parse " + sourceId, e);
        }

        logs.forEach((logEntry) -> logEntry.finish());

        return logs;
    }

    private static Digester createDigester(boolean secure) throws SAXException {
        Digester digester = new Digester();
        if (secure) {
            digester.setXIncludeAware(false);
            try {
                digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                digester.setFeature("http://xml.org/sax/features/external-general-entities", false);
                digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                digester.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (ParserConfigurationException ex) {
                throw new SAXException("Failed to securely configure xml digester parser", ex);
            }
        }
        return digester;
    }

    private static Digester createDigester(ArrayList<LogEntry> logs) throws SAXException {
        boolean secure = (!Boolean.getBoolean(GXSChangeLogParser.class.getName() + ".UNSAFE"));
        Digester digester = createDigester(secure);
        digester.push(logs);

        digester.addObjectCreate("*/logentry", LogEntry.class);
        digester.addSetProperties("*/logentry");
        digester.addBeanPropertySetter("*/logentry/author", "user");

        /* times in changelog.xml are in UTC (output from a call to "TeamDev.exe -utc ...") */
        digester.addCallMethod("*/logentry/date", "setDateFromUTCDate", 0);

        digester.addBeanPropertySetter("*/logentry/msg");
        digester.addSetNext("*/logentry", "add");

        digester.addObjectCreate("*/logentry/actions/action", Action.class);
        digester.addSetProperties("*/logentry/actions/action");
        digester.addBeanPropertySetter("*/logentry/actions/action/objectGuid");
        digester.addBeanPropertySetter("*/logentry/actions/action/objectType");
        digester.addBeanPropertySetter("*/logentry/actions/action/objectTypeGuid");
        digester.addBeanPropertySetter("*/logentry/actions/action/objectName");
        digester.addBeanPropertySetter("*/logentry/actions/action/objectDescription");
        digester.addSetNext("*/logentry/actions/action", "addAction");

        return digester;
    }
}
