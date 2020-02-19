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

import hudson.Util;
import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 *
 * @author jlr
 */
public final class GXSChangeLogSet extends ChangeLogSet {
    private final List<LogEntry> logs;

    /**
     * @GuardedBy this
     */
    private GXSRevisionState revisionState;

    @SuppressWarnings("unchecked") 
    GXSChangeLogSet(Run<?,?> build, RepositoryBrowser<?> browser, List<LogEntry> logs) {
        super(build, browser);
        this.logs = prepareChangeLogEntries(logs);
    }

    public boolean isEmptySet() {
        return logs.isEmpty();
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    public Iterator<LogEntry> iterator() {
        return logs.iterator();
    }

    @Override
    public String getKind() {
        return "GXserver";
    }

    @Nonnull
    public synchronized GXSRevisionState getRevisionState() throws IOException {
        if(revisionState==null)
            revisionState = GeneXusServerSCM.parseRevisionFile(getRun());
        return revisionState;
    }
    
    private List<LogEntry> prepareChangeLogEntries(List<LogEntry> items) {
        items = removeDuplicatedEntries(items);
        
        // we want recent changes first
        Collections.sort(items, new ReverseByRevisionComparator());
        for (LogEntry log : items) {
            log.setParent(this);
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Removes duplicate entries, 
     * 
     * This was taken from SVN which seems to need it for svn:externals.
     * Maybe we don't need it here
     *
     * @param items list of items
     * @return filtered list without duplicated entries
     */
    static List<LogEntry> removeDuplicatedEntries(List<LogEntry> items) {
        Set<LogEntry> entries = new HashSet<>(items);
        return new ArrayList<>(entries);
    }

    /**
     * One commit.
     * <p>
     * Setter methods are public only so that the objects can be constructed from Digester.
     * So please consider this object read-only.
     */
    public static class LogEntry extends ChangeLogSet.Entry {
        private int revision;
        private User author;
        private Date date;
        private String msg;
        private final List<Action> actions = new ArrayList<>();

        /**
         * Gets the {@link GXSChangeLogSet} to which this change set belongs.
         */
        public GXSChangeLogSet getParent() {
            return (GXSChangeLogSet)super.getParent();
        }

        // because of the classloader difference, we need to extend this method to make it accessible
        // to the rest of SubversionSCM
        @Override
        @SuppressWarnings("rawtypes")
        protected void setParent(ChangeLogSet changeLogSet) {
            super.setParent(changeLogSet);
        }

        /**
         * Gets the revision of the commit.
         *
         * <p>
         * If the commit made the repository revision 1532, this
         * method returns 1532.
         * 
         * @return revision number
         */
        @Exported
        public int getRevision() {
            return revision;
        }

        public void setRevision(int revision) {
            this.revision = revision;
        }
        
        @Override
        public String getCommitId() {
            return String.valueOf(revision);
        }

        @Override
        public User getAuthor() {
            if(author==null)
                return User.getUnknown();
            return author;
        }
        
        @Override
        public long getTimestamp() {
            if (date == null)
                return -1;
            
            return date.getTime();
        }

        @Override
        public Collection<String> getAffectedPaths() {
            return new AbstractList<String>() {
                public String get(int index) {
                    return actions.get(index).objectName;
                }
                public int size() {
                    return actions.size();
                }
            };
        }
        
        public void setUser(String author) {
            this.author = User.get(author);
        }

        @Exported
        public String getUser() {// digester wants read/write property, even though it never reads. Duh.
            return author!=null ? author.getDisplayName() : "unknown";
        }

        @Exported
        public Date getDate() {
            return DateUtils.cloneIfNotNull(date);
        }

        @Exported
        public String getDisplayDate() {
            return DateUtils.toDisplayDate(date);
        }
        
        public void setDate(Date date) {
            this.date = DateUtils.cloneIfNotNull(date);
        }
        
        public void setDateFromUTCDate(String utcDate) {
            date = DateUtils.fromUTCstring(utcDate);
        }        
        
        @Override @Exported
        public String getMsg() {
            return msg;
        }

        @Exported
        public String getComment() {
            return getMsg();
        }
        
        public void setMsg(String msg) {
            this.msg = msg;
        }

        public void addAction( Action p ) {
            p.entry = this;
            actions.add(p);
        }

        /**
         * Gets the files that are changed in this commit.
         * @return
         *      can be empty but never null.
         */
        @Exported
        public List<Action> getActions() {
            return actions;
        }
        
        @Exported
        public int getActionsCount() {
            return actions.size();
        }
        
        @Override
        public Collection<Action> getAffectedFiles() {
            return actions;
        }
        
        void finish() {
            Collections.sort(actions, (Action o1, Action o2) -> {
                String path1 = Util.fixNull(o1.getObjectName());
                String path2 = Util.fixNull(o2.getObjectName());
                return path1.compareTo(path2);
            });
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LogEntry that = (LogEntry) o;

            if (revision != that.revision) {
                return false;
            }
            if (author != null ? !author.equals(that.author) : that.author != null) {
                return false;
            }
            if (date != null ? !date.equals(that.date) : that.date != null) {
                return false;
            }
            if (msg != null ? !msg.equals(that.msg) : that.msg != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = revision;
            result = 31 * result + (author != null ? author.hashCode() : 0);
            result = 31 * result + (date != null ? date.hashCode() : 0);
            result = 31 * result + (msg != null ? msg.hashCode() : 0);
            return result;
        }
    }

    /**
     * A file in a commit.
     * <p>
     * Setter methods are public only so that the objects can be constructed from Digester.
     * So please consider this object read-only.
     */
    @ExportedBean(defaultVisibility=999)
    public static class Action implements AffectedFile {
        
        private static final String MODIFIED = "Modified";
        private static final String INSERTED = "Inserted";
        private static final String DELETED = "Deleted";
        
        private LogEntry entry;
        private String type;

        /**
         * Object Id
         */
        private String objectGuid;

        /**
         * Object Type Id
         */
        private String objectTypeGuid;
        
        /**
         * Object Type
         */
        private String objectType;
        
        /**
         * Object Name
         */
        private String objectName;

        /**
         * Object Description
         */
        private String objectDescription;

        /**
         * Sets the {@link LogEntry} of which this action is a member.
         * 
         * @param entry value to set
         */
        public void setLogEntry(LogEntry entry) {
            this.entry = entry;
        }

        /**
         * Gets the {@link LogEntry} of which this action is a member.
         * 
         * @return log entry
         */
        public LogEntry getLogEntry() {
            return entry;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
        
        public void setObjectGuid(String objectGuid) {
            this.objectGuid = objectGuid;
        }
        
        public String getObjectGuid() {
            return objectGuid;
        }
        
        public void setObjectTypeGuid(String objectTypeGuid) {
            this.objectTypeGuid = objectTypeGuid;
        }
        
        @Exported
        public String getObjectTypeGuid() {
            return objectTypeGuid;
        }
        
        public void setObjectType(String objectType) {
            this.objectType = objectType;
        }
        
        public String getObjectType() {
            return objectType;
        }
        
        public void setObjectName(String objectName) {
            this.objectName = objectName;
        }

        @Exported(name="file")
        public String getObjectName() {
            return objectName;
        }

        public void setObjectDescription(String objectDescription) {
            this.objectDescription = objectDescription;
        }
        
        public String getObjectDescription() {
            return objectDescription;
        }
        
        /**
         * Inherited from AffectedFile
         *
         * Since 2.TODO this no longer returns the path relative to repository root,
         * but the path relative to the workspace root. Use getValue() instead.
         */
        public String getPath() {
                return objectName;
        }
        
        @Exported
        public EditType getEditType() {
            if (type.equals(INSERTED))
                return EditType.ADD;
            if(type.equals(DELETED))
                return EditType.DELETE;
            
            /* type.equals(MODIFIED) */
            return EditType.EDIT;
        }
    }

    private static final class ReverseByRevisionComparator implements Comparator<LogEntry>, Serializable {
        private static final long serialVersionUID = 1L;

        public int compare(LogEntry a, LogEntry b) {
            return b.getRevision() - a.getRevision();
        }
    }
}
