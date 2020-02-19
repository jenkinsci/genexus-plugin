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
package org.jenkinsci.plugins.genexus.helpers;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Util;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author jlr
 */
public class CredentialsHelper {

    public static StandardUsernamePasswordCredentials getUsernameCredentials(Item context, String credentialsId, String url) {
        return getCredentials(StandardUsernamePasswordCredentials.class, context, credentialsId, url);
    }

    public static <C extends Credentials> C getCredentials(Class<C> cClass, Item context, String credentialsId, String url) {
        return credentialsId == null ? null
                : CredentialsMatchers.firstOrNull(
                        CredentialsProvider.lookupCredentials(
                                cClass,
                                context,
                                ACL.SYSTEM,
                                URIRequirementBuilder.fromUri(url).build()
                        ),
                        CredentialsMatchers.withId(credentialsId)
                );
    }

    public static ListBoxModel getCredentialsList(Item context, String credentialsId, String url) {
        StandardListBoxModel result = new StandardListBoxModel();

        List<DomainRequirement> reqs = Collections.<DomainRequirement>emptyList();
        if (url != null) {
            url = Util.fixEmptyAndTrim(url);
            reqs = URIRequirementBuilder.fromUri(url).build();
        }

        CredentialsMatcher matcher = CredentialsMatchers.anyOf(CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class));

        return result
                .includeEmptyValue()
                .includeMatchingAs(ACL.SYSTEM, context, StandardUsernamePasswordCredentials.class, reqs, matcher)
                .includeCurrentValue(credentialsId);
    }
}
