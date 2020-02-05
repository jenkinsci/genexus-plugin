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

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author jlr
 */
public class KbInfo {

    @XmlType
    @XmlEnum(String.class)
    public enum TeamDevMode {
        @XmlEnumValue("Yes")
        MERGE,
        @XmlEnumValue("No")
        LOCK
    }

    @XmlAttribute(name = "Name")
    String name = "";

    @XmlAttribute(name = "Description")
    String description = "";

    @XmlAttribute(name = "URL")
    URL url;

    @XmlAttribute(name = "KBImage")
    String base64Image = "";

    @XmlAttribute(name = "Tags")
    String tags = "";

    @XmlAttribute(name = "TeamDevMode")
    TeamDevMode teamDevMode;

    @XmlAttribute(name = "PublishUser")
    String publishUser = "";

    @XmlAttribute(name = "PublishDate")
    @XmlJavaTypeAdapter(PublishDateAdapter.class)
    LocalDate publishDate;

    public KbInfo() {
        this.teamDevMode = TeamDevMode.MERGE;
        this.publishDate = LocalDate.of(1970, 1, 1);
    }

    public static class PublishDateAdapter extends XmlAdapter<String, LocalDate> {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.ROOT);

        @Override
        public LocalDate unmarshal(String xml) throws Exception {
            LocalDate date = LocalDate.from(formatter.parse(xml));
            return date;
        }

        @Override
        public String marshal(LocalDate value) throws Exception {
            return formatter.format(value);
        }
    }

    public static class TeamDevModeAdapter extends XmlAdapter<String, TeamDevMode> {

        @Override
        public TeamDevMode unmarshal(String xml) throws Exception {
            return xml.compareToIgnoreCase("yes") == 0 ? TeamDevMode.MERGE : TeamDevMode.LOCK;
        }

        @Override
        public String marshal(TeamDevMode mode) throws Exception {
            return mode == TeamDevMode.MERGE ? "Yes" : "No";
        }
    }
}
