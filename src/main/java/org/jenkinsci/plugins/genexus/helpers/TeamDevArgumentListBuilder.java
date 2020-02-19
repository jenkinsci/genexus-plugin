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
package org.jenkinsci.plugins.genexus.helpers;

import org.jenkinsci.plugins.genexus.server.GXSConnection;
import hudson.util.ArgumentListBuilder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jlr
 * @author Acaceres1996
 */
public class TeamDevArgumentListBuilder extends ArgumentListBuilder {

    private static final long serialVersionUID = 1L;

    public TeamDevArgumentListBuilder(String gxPath, GXSConnection gxsConnection) {
        this(gxPath, gxsConnection, /*fromTimestamp=*/ null, /*toTimestamp=*/ null);
    }

    public TeamDevArgumentListBuilder(String gxPath, GXSConnection gxsConnection, Date fromTimestamp, Date toTimestamp) {
        this(gxPath, gxsConnection, fromTimestamp, toTimestamp, /*fromExcluding=*/ false);
    }

    public TeamDevArgumentListBuilder(String gxPath, GXSConnection gxsConnection, Date fromTimestamp, Date toTimestamp, boolean fromExcluding) {
        this(
                gxPath,
                gxsConnection.getServerURL(),
                gxsConnection.getUserName(),
                gxsConnection.getUserPassword(),
                gxsConnection.getKbName(),
                gxsConnection.getKbVersion(),
                fromTimestamp,
                toTimestamp,
                fromExcluding);
    }

    private TeamDevArgumentListBuilder(String gxPath, String serverURL, String userName, String userPassword, String kbName, String kbVersion, Date fromTimestamp, Date toTimestamp, boolean fromExcluding) {

        String pathToTeamDev = gxPath + "\\teamdev.exe";

        add(pathToTeamDev);
        add("history");
        add("/x");
        add("/utc");
        add("/s:" + serverURL);

        if (!userName.isEmpty()) {
            addMasked("/u:" + userName);
            addMasked("/p:" + userPassword);
        }

        add("/kb:" + kbName);

        if (StringUtils.isNotBlank(kbVersion)) {
            add("/v:" + kbVersion);
        }

        if (fromTimestamp != null) {
            add("/from:" + formatDate(actualFromTimestamp(fromTimestamp, fromExcluding)));
        }

        if (toTimestamp != null) {
            add("/to:" + formatDate(toTimestamp));
        }
    }

    private static DateFormat getDateFormat() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (dateFormat instanceof SimpleDateFormat) {
            SimpleDateFormat sdf = (SimpleDateFormat) dateFormat;
            String pattern = sdf.toPattern().replace("Z", "").replace("z", "").trim();
            sdf.applyPattern(pattern);
        }

        return dateFormat;
    }

    private static String formatDate(Date date) {
        return getDateFormat().format(date);
    }

    private Date actualFromTimestamp(Date fromTimestamp, boolean fromExcluding) {
        if (!fromExcluding) {
            return fromTimestamp;
        }

        // returns a datetime 1 second later, so that the initial fromTimestamp is excluded
        return new Date(fromTimestamp.getTime() + 1 * 1000);
    }
}
