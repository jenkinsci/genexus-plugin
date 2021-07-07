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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jenkinsci.plugins.genexus.helpers.UTCDateTimeAdapter;

/**
 *
 * @author jlr
 */
@XmlAccessorType(XmlAccessType.NONE)
public class RevisionInfo {

    @XmlAttribute(name = "RevisionId")
    public int id;

    @XmlAttribute(name = "guid")
    public UUID guid;

    @XmlAttribute(name = "user")
    public String author;

    @XmlAttribute(name = "commitDate")
    @XmlJavaTypeAdapter(UTCDateTimeAdapter.class)
    public Date date;

    @XmlAttribute(name = "IsDisabled")
    public boolean isDisabled;

    @XmlAttribute(name = "Comment")
    public String comment;

    @XmlElementWrapper(name = "Actions")
    @XmlElement(name = "Action")
    private final ArrayList<ActionInfo> actions;

    public List<ActionInfo> getActions() {
        return actions;
    }

    public RevisionInfo() {
        this.id = 0;
        this.guid = new UUID(0, 0);
        this.author = "";
        this.date = new Date(0);
        this.isDisabled = false;
        this.comment = "";
        this.actions = new ArrayList<>();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof RevisionInfo)) {
            return false;
        }

        RevisionInfo otherRevisionInfo = (RevisionInfo) other;
        return otherRevisionInfo.id == this.id
                && Objects.equals(otherRevisionInfo.guid, this.guid)
                && Objects.equals(otherRevisionInfo.author, this.author)
                && Objects.equals(otherRevisionInfo.date, this.date)
                && otherRevisionInfo.isDisabled == this.isDisabled
                && Objects.equals(otherRevisionInfo.comment, this.comment)
                && Objects.equals(otherRevisionInfo.actions, this.actions)
                && true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.id;
        hash = 97 * hash + Objects.hashCode(this.guid);
        hash = 97 * hash + Objects.hashCode(this.author);
        hash = 97 * hash + Objects.hashCode(this.date);
        hash = 97 * hash + (this.isDisabled ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.comment);
        hash = 97 * hash + Objects.hashCode(this.actions);
        return hash;
    }
}
