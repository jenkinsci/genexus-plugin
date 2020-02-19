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
package org.jenkinsci.plugins.genexus.builders;

import hudson.model.FreeStyleProject;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.genexus.GeneXusInstallation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

/**
 *
 * @author jlr
 * @author mmarsicano
 */
public class GeneXusBuilderTest {
    
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @BeforeClass
    public static void setUpClass() {
    }
    
    @Before
    public void setUp() {
        System.out.println("GeneXusBuilderTest: @Before method");
        
        GeneXusInstallation[] gxInstallations = new GeneXusInstallation[] {
            new GeneXusInstallation("Evo3", "C:\\gx\\evo3", null),
            new GeneXusInstallation("v15", "C:\\gx\\v15", null)
        };
        
        GeneXusInstallation.DescriptorImpl descriptor = jenkins.getInstance().getDescriptorByType(GeneXusInstallation.DescriptorImpl.class);
        descriptor.setInstallations(gxInstallations);
    }
    
    @Test
    public void testConfigRoundtrip() throws Exception {
      
        FreeStyleProject project = jenkins.createFreeStyleProject();
        
        TestCases testCases = CreateConfigRoundtripTestCases();
        
        for (GeneXusBuilder builder : testCases.inputs) {
            project.getBuildersList().add(builder);
        }
        
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(testCases.expectedOutputs, project.getBuildersList());
    }
    
    @After
    public void tearDown() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    
    private TestCases CreateConfigRoundtripTestCases() {
        TestCases testCases = new TestCases();
        
        // existing id
        testCases.addCase(new GeneXusBuilder("Evo3", "kbpath", "kbversion", "kbenvironment", "kbDbCredentialsId", true));

        // non existing id results in ""
        testCases.addCase(
                new GeneXusBuilder("NotAnId", "kbpath", "kbversion", "kbenvironment", "kbDbCredentialsId",true),
                new GeneXusBuilder(""       , "kbpath", "kbversion", "kbenvironment", "kbDbCredentialsId",true)
        );

        // false is also preserved 
        testCases.addCase(new GeneXusBuilder("Evo3", "kbpath", "kbversion", "kbenvironment", "kbDbCredentialsId", false));
        
        // null values are converted to default values
        testCases.addCase(
                new GeneXusBuilder(null, null    , null, null, "kbDbCredentialsId", true),
                new GeneXusBuilder(""  , "KBpath", ""  , ""  , "kbDbCredentialsId", true)
        );
        
        return testCases;
    }
    
    /**
     * Test of getGxInstallationId method, of class GeneXusBuilder.
     */
    @Test
    public void testGetGxInstallationId() {
        System.out.println("getGxInstallationId");
        String expResult = "v15";
        GeneXusBuilder instance = new GeneXusBuilder(expResult, "C:\\", "version", "environment", "kbDbCredentialsId", false);
        String result = instance.getGxInstallationId();
        assertEquals(expResult, result);
    }
    
    private class TestCases {
        public final List<GeneXusBuilder> inputs = new ArrayList<>();
        public final List<GeneXusBuilder> expectedOutputs = new ArrayList<>();
        
        public void addCase(GeneXusBuilder input) {
            addCase(input, input);
        }
        
        public void addCase(GeneXusBuilder input, GeneXusBuilder output) {
            inputs.add(input);
            expectedOutputs.add(output);
        }
    }
}
