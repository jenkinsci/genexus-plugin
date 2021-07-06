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
package org.jenkinsci.plugins.genexus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

/**
 *
 * @author jlr
 */
public class GeneXusInstallationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConfigRoundtrip() throws Exception {
        GeneXusInstallation.DescriptorImpl descriptor = jenkins.getInstance().getDescriptorByType(GeneXusInstallation.DescriptorImpl.class);

        TestCases testCases = CreateConfigRoundtripTestCases();
        descriptor.setInstallations(testCases.inputs.toArray(new GeneXusInstallation[testCases.inputs.size()]));

        List<GeneXusInstallation> results = Arrays.asList(descriptor.getInstallations());
        jenkins.assertEqualDataBoundBeans(testCases.expectedOutputs, results);
    }

    private TestCases CreateConfigRoundtripTestCases() {
        TestCases testCases = new TestCases();

        testCases.addCase(new GeneXusInstallation("Evo3", "C:\\gx\\evo3", "msb12xxx"));
        testCases.addCase(new GeneXusInstallation("v15" , "C:\\gx\\v15 ", ""        ));
        testCases.addCase(new GeneXusInstallation(""    , ""            , ""        ));
        testCases.addCase(new GeneXusInstallation(null  , null          , null      ));

        return testCases;
    }

    private class TestCases {

        public final List<GeneXusInstallation> inputs = new ArrayList<>();
        public final List<GeneXusInstallation> expectedOutputs = new ArrayList<>();

        public void addCase(GeneXusInstallation input) {
            addCase(input, input);
        }

        public void addCase(GeneXusInstallation input, GeneXusInstallation output) {
            inputs.add(input);
            expectedOutputs.add(output);
        }
    }

}
