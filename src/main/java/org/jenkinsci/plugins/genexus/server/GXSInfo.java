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

import org.jenkinsci.plugins.genexus.server.GXSConnection;
import com.fasterxml.jackson.annotation.JsonCreator;
import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;

/**
 * {@link GXSInfo} for {@link GeneXusServerSCM}.
 * @author jlr
 */
public class GXSInfo implements Serializable, Comparable<GXSInfo> {

    public final String serverUrl;

    public final String kbName;

    public final String kbVersion;

    public final long revision;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy HH:mm:ss")
    public final Date revisionDate;

    GXSInfo(GXSConnection gxsConnection, long revision, Date revisionDate) {
        this(gxsConnection.getServerURL(), gxsConnection.getKbName(), gxsConnection.getKbVersion(), revision, revisionDate);
    }
    
    @JsonCreator(mode = PROPERTIES)
    GXSInfo(
            @JsonProperty("serverUrl") String serverUrl,
            @JsonProperty("kbName") String kbName,
            @JsonProperty("kbVersion") String kbVersion,
            @JsonProperty("revision") long revision,
            @JsonProperty("revisionDate") Date revisionDate) {
        this.serverUrl = serverUrl;
        this.kbName = kbName;
        this.kbVersion = kbVersion;
        this.revision = revision;
        this.revisionDate = revisionDate;
    }

    @Override
    public int compareTo(GXSInfo that) {
        int compareResult;

        compareResult = this.serverUrl.compareTo(that.serverUrl);
        if (compareResult != 0) {
            return compareResult;
        }

        compareResult = this.kbName.compareTo(that.kbName);
        if (compareResult != 0) {
            return compareResult;
        }

        compareResult = this.kbVersion.compareTo(that.kbVersion);
        if (compareResult != 0) {
            return compareResult;
        }

        return Long.compare(this.revision, that.revision);
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (that == null || getClass() != that.getClass()) {
            return false;
        }

        GXSInfo thatInfo = (GXSInfo) that;
        return revision == thatInfo.revision
                && revisionDate.equals(thatInfo.revisionDate)
                && kbVersion.equals(thatInfo.kbVersion)
                && kbName.equals(thatInfo.kbName)
                && serverUrl.equals(thatInfo.serverUrl);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.serverUrl);
        hash = 67 * hash + Objects.hashCode(this.kbName);
        hash = 67 * hash + Objects.hashCode(this.kbVersion);
        hash = 67 * hash + (int) (this.revision ^ (this.revision >>> 32));
        hash = 67 * hash + Objects.hashCode(this.revisionDate);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(serverUrl).append("/").append(kbName);

        if (StringUtils.isNotBlank(kbVersion)) {
            sb.append(String.format(" (ver.%s)", kbVersion));
        }

        sb.append(String.format(" (rev.%s)", revision));

        return sb.toString();
    }

    private static final long serialVersionUID = 1L;
}
