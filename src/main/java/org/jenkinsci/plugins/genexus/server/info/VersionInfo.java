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

import java.util.Objects;
import java.util.UUID;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.jenkinsci.plugins.genexus.helpers.NoCaseSensitiveBooleanAdapter;

/**
 *
 * @author jlr
 */
@XmlAccessorType(XmlAccessType.NONE)
public class VersionInfo {

    @XmlAttribute(name = "id")
    public int id;

    @XmlAttribute(name = "name")
    public String name;

    @XmlAttribute(name = "guid")
    public UUID guid;

    @XmlAttribute(name = "type")
    public UUID type;

    @XmlAttribute(name = "isTrunk", required = false)
    @XmlJavaTypeAdapter(NoCaseSensitiveBooleanAdapter.class)
    public Boolean isTrunk;

    @XmlAttribute(name = "parentId", required = false)
    public int parentId;

    @XmlAttribute(name = "isFrozen", required = false)
    @XmlJavaTypeAdapter(NoCaseSensitiveBooleanAdapter.class)
    public Boolean isFrozen;

    @XmlAttribute(name = "remindsCount")
    public int remindsCount;

    public VersionInfo() {
        this.id = 0;
        this.name = "";
        this.guid = new UUID(0, 0);
        this.type = new UUID(0, 0);
        this.isTrunk = false;
        this.parentId = 0;
        this.isFrozen = false;
        this.remindsCount = 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof VersionInfo)) {
            return false;
        }

        VersionInfo otherVersionInfo = (VersionInfo) other;
        return Objects.equals(otherVersionInfo.name, this.name)
            && otherVersionInfo.id ==  this.id
            && Objects.equals(otherVersionInfo.guid, this.guid)
            && Objects.equals(otherVersionInfo.type, this.type)
            && Objects.equals(otherVersionInfo.isTrunk, this.isTrunk)
            && Objects.equals(otherVersionInfo.isFrozen, this.isFrozen)
            && otherVersionInfo.parentId == this.parentId
            && otherVersionInfo.remindsCount == this.remindsCount
            && true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.id;
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + Objects.hashCode(this.guid);
        hash = 67 * hash + Objects.hashCode(this.type);
        hash = 67 * hash + Objects.hashCode(this.isTrunk);
        hash = 67 * hash + this.parentId;
        hash = 67 * hash + Objects.hashCode(this.isFrozen);
        hash = 67 * hash + this.remindsCount;
        return hash;
    }

}
