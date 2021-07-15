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
import java.util.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author jlr
 */
@XmlAccessorType(XmlAccessType.NONE)
public class KBInfo {

    @XmlType
    @XmlEnum(String.class)
    public enum TeamDevMode {
        @XmlEnumValue("Yes")
        MERGE,
        @XmlEnumValue("No")
        LOCK
    }

    @XmlAttribute(name = "Name")
    public String name = "";

    @XmlAttribute(name = "Description")
    public String description = "";

    @XmlAttribute(name = "URL")
    @XmlJavaTypeAdapter(EmptyURLAdapter.class)
    public URL url;

    @XmlAttribute(name = "KBImage")
    public String base64Image = "";

    @XmlAttribute(name = "Tags")
    public String tags = "";

    @XmlAttribute(name = "TeamDevMode")
    public TeamDevMode teamDevMode;

    @XmlAttribute(name = "PublishUser")
    public String publishUser = "";

    @XmlAttribute(name = "PublishDate")
    @XmlJavaTypeAdapter(PublishDateAdapter.class)
    public LocalDate publishDate;

    public KBInfo() {
        this.teamDevMode = TeamDevMode.MERGE;
        this.publishDate = LocalDate.of(1970, 1, 1);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof KBInfo)) {
            return false;
        }

        KBInfo otherKBInfo = (KBInfo) other;
        return Objects.equals(otherKBInfo.name, this.name)
                && Objects.equals(otherKBInfo.description, this.description)
                && Objects.equals(otherKBInfo.url, this.url)
                && Objects.equals(otherKBInfo.base64Image, this.base64Image)
                && Objects.equals(otherKBInfo.tags, this.tags)
                && Objects.equals(otherKBInfo.teamDevMode, this.teamDevMode)
                && Objects.equals(otherKBInfo.publishUser, this.publishUser)
                && Objects.equals(otherKBInfo.publishDate, this.publishDate)
                && true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.description);
        hash = 29 * hash + Objects.hashCode(this.url);
        hash = 29 * hash + Objects.hashCode(this.base64Image);
        hash = 29 * hash + Objects.hashCode(this.tags);
        hash = 29 * hash + Objects.hashCode(this.teamDevMode);
        hash = 29 * hash + Objects.hashCode(this.publishUser);
        hash = 29 * hash + Objects.hashCode(this.publishDate);
        return hash;
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

    public static class EmptyURLAdapter extends XmlAdapter<String, URL> {

        @Override
        public URL unmarshal(String stringValue) throws Exception {
            if ("".equals(stringValue)) {
                return null;
            }
            return new URL(stringValue);
        }

        @Override
        public String marshal(URL urlValue) throws Exception {
            if (urlValue == null) {
                return "";
            }
            return urlValue.toString();
        }
    }
}
