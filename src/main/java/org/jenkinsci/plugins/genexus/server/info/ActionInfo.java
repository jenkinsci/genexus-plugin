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

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.genexus.helpers.UTCDateTimeAdapter;

/**
 *
 * @author jlr
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ActionInfo {

    @XmlType
    @XmlEnum(String.class)
    public enum ActionType {
        @XmlEnumValue("Unchanged")
        Unchanged,
        @XmlEnumValue("Inserted")
        Inserted,
        @XmlEnumValue("Modified")
        Modified,
        @XmlEnumValue("Deleted")
        Deleted,
        @XmlEnumValue("Unknown")
        Unknown
    }

    @XmlAttribute(name = "guid")
    public UUID objectGuid;

    @XmlAttribute(name = "key")
    public String objectKey;

    @XmlAttribute(name = "typeDescriptor")
    public String objectType;

    @XmlAttribute(name = "name")
    public String objectName;

    @XmlAttribute(name = "description")
    public String objectDescription;

    @XmlAttribute(name = "operation")
    public ActionInfo.ActionType actionType;

    @XmlAttribute(name = "userName")
    public String userName;

    @XmlAttribute(name = "timestamp", required = false)
    @XmlJavaTypeAdapter(UTCDateTimeAdapter.class)
    public Date editedTimestamp;

    public ActionInfo() {
        this.objectGuid = new UUID(0, 0);
        this.objectKey = "";
        this.objectType = "";
        this.objectName = "";
        this.objectDescription = "";
        this.actionType = ActionType.Unknown;
        this.userName = "";
    }

    public UUID getObjectTypeGuid() {
        return UUID.fromString(StringUtils.left(objectKey, 36));
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ActionInfo)) {
            return false;
        }

        ActionInfo otherActionInfo = (ActionInfo) other;
        return Objects.equals(otherActionInfo.objectGuid, this.objectGuid)
                && Objects.equals(otherActionInfo.objectKey, this.objectKey)
                && Objects.equals(otherActionInfo.objectType, this.objectType)
                && Objects.equals(otherActionInfo.objectName, this.objectName)
                && Objects.equals(otherActionInfo.objectDescription, this.objectDescription)
                && Objects.equals(otherActionInfo.actionType, this.actionType)
                && Objects.equals(otherActionInfo.userName, this.userName)
                && Objects.equals(otherActionInfo.editedTimestamp, this.editedTimestamp)
                && true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.objectGuid);
        hash = 89 * hash + Objects.hashCode(this.objectKey);
        hash = 89 * hash + Objects.hashCode(this.objectType);
        hash = 89 * hash + Objects.hashCode(this.objectName);
        hash = 89 * hash + Objects.hashCode(this.objectDescription);
        hash = 89 * hash + Objects.hashCode(this.actionType);
        hash = 89 * hash + Objects.hashCode(this.userName);
        hash = 89 * hash + Objects.hashCode(this.editedTimestamp);
        return hash;
    }
}
