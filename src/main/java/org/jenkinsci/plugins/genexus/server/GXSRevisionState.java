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

import hudson.scm.SCMRevisionState;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link GXSRevisionState} for {@link GeneXusServerSCM}.
 * {@link Serializable} since we compute this remote.
 * @author jlr
 */
public class GXSRevisionState extends SCMRevisionState implements Serializable {

    private final long revision;
    private final Date revisionDate;

    public static final GXSRevisionState MIN_REVISION = new GXSRevisionState(0, new Date(0));
    
    GXSRevisionState(long revision, Date revisionDate) {
        this.revision = revision;
        this.revisionDate = revisionDate;
    }

    public long getRevision() {
        return revision;
    }
    
    public Date getRevisionDate() {
        return DateUtils.cloneIfNotNull(revisionDate);
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.ROOT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return "GXserverRevisionState{" + revision + "," + sdf.format(revisionDate) + "}";
    }

    private static final long serialVersionUID = 1L;
}
