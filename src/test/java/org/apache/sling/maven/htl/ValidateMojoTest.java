/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Licensed to the Apache Software Foundation (ASF) under one
 ~ or more contributor license agreements.  See the NOTICE file
 ~ distributed with this work for additional information
 ~ regarding copyright ownership.  The ASF licenses this file
 ~ to you under the Apache License, Version 2.0 (the
 ~ "License"); you may not use this file except in compliance
 ~ with the License.  You may obtain a copy of the License at
 ~
 ~   http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
package org.apache.sling.maven.htl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ValidateMojoTest {

    private static final String ERROR_SLY = "src/main/resources/apps/projects/error.sly";
    private static final String WARNING_SLY = "src/main/resources/apps/projects/warning.sly";
    private static final String SCRIPT_HTML = "src/main/resources/apps/projects/script.html";
    private static final String EXCLUDE_HTML = "src/main/resources/apps/projects/exclude.html";
    private static final String INVALID_OPTIONS_SLY = "src/main/resources/apps/projects/invalid-options.sly";
    private static final String NON_DEFAULT_OPTIONS_SLY = "src/main/resources/apps/projects/non-default-options.sly";
    private static final String DATA_SLY_TEST_CONSTANT_VALUES_SLY = "src/main/resources/apps/projects/data-sly-test-constant-values.sly";
    private static final String TEST_PROJECT = "test-project";
    private static final String EXPLICIT_INCLUDES_POM = "explicit-includes.pom.xml";
    private static final String EXPLICIT_EXCLUDES_POM = "explicit-excludes.pom.xml";
    private static final String FAIL_ON_WARNINGS_POM = "fail-on-warnings.pom.xml";
    private static final String DEFAULT_INCLUDES_POM = "default-includes.pom.xml";
    private static final String GENERATE_JAVA_CLASSES_POM = "generate-java-classes.pom.xml";
    private static final String GENERATE_JAVA_CLASSES_WITH_PREFIX_POM = "generate-java-classes-with-prefix.pom.xml";
    private static final String SKIP_POM = "skip.pom.xml";
    private static final String GENERATE_JAVA_CLASSES_IGNORE_IMPORTS_POM = "generate-java-classes-ignore-imports.pom.xml";
    private static final String INVALID_OPTIONS_POM = "invalid-options.pom.xml";
    private static final String NON_DEFAULT_OPTIONS_POM = "non-default-options.pom.xml";
    private static final String DATA_SLY_TEST_CONSTANT_VALUES_POM_XML = "data-sly-test-constant-values.pom.xml";
    private static final String FAIL_ON_ERRORS_FALSE_POM_XML = "fail-on-errors-false.pom.xml";


    @Rule
    public MojoRule mojoRule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
            super.before();
            /*
             * Make sure the base directory is initialised properly for this test
             */
            System.setProperty("basedir", new File("src" + File.separator + "test" + File.separator + "resources" + File
                    .separator + TEST_PROJECT).getAbsolutePath());
        }
    };

    @After
    public void tearDown() {
        File baseDir = new File(System.getProperty("basedir"));
        FileUtils.deleteQuietly(new File(baseDir, "target"));
    }

    @Test
    public void testExplicitIncludes() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, EXPLICIT_INCLUDES_POM);
        try {
            validateMojo.execute();
        } catch (MojoFailureException e) {
            List<File> processedFiles = validateMojo.getProcessedFiles();
            assertEquals("Expected 5 files to process.", 5, processedFiles.size());
            assertTrue("Expected error.sly to be one of the processed files.", processedFiles.contains(new File(baseDir, ERROR_SLY)));
            assertTrue("Expected warning.sly to be one of the processed files.", processedFiles.contains(new File(baseDir, WARNING_SLY)));
            assertTrue("Expected compilation errors.", validateMojo.hasErrors());
            assertTrue("Expected compilation warnings.", validateMojo.hasWarnings());
        }
    }

    @Test
    public void testExplicitExcludes() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, EXPLICIT_EXCLUDES_POM);
        validateMojo.execute();
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertEquals("Expected 1 file to process.", 1, processedFiles.size());
        assertTrue("Expected script.html to be the only processed file.", processedFiles.contains(new File(baseDir, SCRIPT_HTML)));
        assertFalse("Did not expect compilation errors.", validateMojo.hasErrors());
        assertFalse("Did not expect compilation warnings.", validateMojo.hasWarnings());
    }

    @Test
    public void testFailOnWarnings() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, FAIL_ON_WARNINGS_POM);
        Exception exception = null;
        try {
            validateMojo.execute();
        } catch (MojoFailureException e) {
            exception = e;
        }
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertNotNull("Expected a MojoFailureException.", exception);
        assertEquals("Expected 1 file to process.", 1, processedFiles.size());
        assertTrue("Expected warning.sly to be one of the processed files.", processedFiles.contains(new File(baseDir, WARNING_SLY)));
        assertTrue("Expected compilation warnings.", validateMojo.hasWarnings());
    }

    @Test
    public void testDefaultIncludes() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, DEFAULT_INCLUDES_POM);
        Exception exception = null;
        try {
            validateMojo.execute();
        } catch (MojoFailureException e) {
            exception = e;
        }
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertNotNull("Expected a MojoFailureException.", exception);
        assertEquals("Expected 2 files to process.", 2, processedFiles.size());
        assertTrue("Expected exclude.html to be one of the processed files.", processedFiles.contains(new File(baseDir, EXCLUDE_HTML)));
        assertTrue("Expected script.html to be one of the processed files.", processedFiles.contains(new File(baseDir, SCRIPT_HTML)));
        assertFalse("Did not expect compilation warnings.", validateMojo.hasWarnings());
    }

    @Test
    public void testFailOnErrorsFalse() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, FAIL_ON_ERRORS_FALSE_POM_XML);
        validateMojo.execute();
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertEquals("Expected 1 file to process.", 1, processedFiles.size());
        assertTrue("Expected error.sly to be one of the processed files.", processedFiles.contains(new File(baseDir, ERROR_SLY)));
        assertFalse("Did not expect compilation warnings.", validateMojo.hasWarnings());
        assertTrue("Expected compilation errors.", validateMojo.hasErrors());
    }

    @Test
    public void testGenerateJavaClasses() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, GENERATE_JAVA_CLASSES_POM);
        validateMojo.execute();
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertEquals("Expected 1 files to process.", 1, processedFiles.size());
        assertTrue("Expected script.html to be one of the processed files.", processedFiles.contains(new File(baseDir,
                SCRIPT_HTML)));
        String generatedSourceCode = FileUtils.readFileToString(new File(baseDir,
                "target/generated-sources/htl/apps/projects/script__002e__html.java"), StandardCharsets.UTF_8);
        assertTrue(generatedSourceCode.contains("org.apache.sling.settings.SlingSettingsService.class"));
        assertTrue(generatedSourceCode.contains("apps.projects.Pojo"));
    }

    @Test
    public void testGenerateJavaClassesWithPrefix() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, GENERATE_JAVA_CLASSES_WITH_PREFIX_POM);
        validateMojo.execute();
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertEquals("Expected 1 files to process.", 1, processedFiles.size());
        assertTrue("Expected script.html to be one of the processed files.", processedFiles.contains(new File(baseDir,
                SCRIPT_HTML)));
        String generatedSourceCode = FileUtils.readFileToString(new File(baseDir,
                "target/generated-sources/htl/org/apache/sling/scripting/sightly/apps/projects/script__002e__html.java"), StandardCharsets.UTF_8);
        assertTrue(generatedSourceCode.contains("org.apache.sling.settings.SlingSettingsService.class"));
        assertTrue(generatedSourceCode.contains("apps.projects.Pojo"));
    }

    @Test
    public void testSkip() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, SKIP_POM);
        validateMojo.execute();
        assertEquals(0, validateMojo.getProcessedFiles().size());
    }

    @Test
    public void testGenerateJavaClassesIgnoreImports() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, GENERATE_JAVA_CLASSES_IGNORE_IMPORTS_POM);
        validateMojo.execute();
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertEquals("Expected 1 files to process.", 1, processedFiles.size());
        assertTrue("Expected script.html to be one of the processed files.", processedFiles.contains(new File(baseDir,
                SCRIPT_HTML)));
        String generatedSourceCode = FileUtils.readFileToString(new File(baseDir,
                "target/generated-sources/htl/apps/projects/script__002e__html.java"), StandardCharsets.UTF_8);
        assertFalse(generatedSourceCode.contains("import org.apache.sling.settings.SlingSettingsService;"));
        assertTrue(generatedSourceCode.contains("apps.projects.Pojo"));
    }

    @Test
    public void testInvalidOptions() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, INVALID_OPTIONS_POM);
        Exception exception = null;
        try {
            validateMojo.execute();
        } catch (MojoFailureException e) {
            exception = e;
        }
        assertNotNull("Expected a MojoFailureException.", exception);
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertEquals("Expected 1 files to process.", 1, processedFiles.size());
        assertTrue("Expected invalid-options.sly to be one of the processed files.", processedFiles.contains(new File(baseDir,
                INVALID_OPTIONS_SLY)));
        assertTrue("Expected compilation warnings.", validateMojo.hasWarnings());
    }

    @Test
    public void testNonDefaultOptions() throws Exception {
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, NON_DEFAULT_OPTIONS_POM);
        Exception exception = null;
        try {
            validateMojo.execute();
        } catch (MojoFailureException e) {
            exception = e;
        }
        assertNull("Did not expect a MojoFailureException.", exception);
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertEquals("Expected 1 files to process.", 1, processedFiles.size());
        assertTrue("Expected non-default-options.sly to be one of the processed files.", processedFiles.contains(new File(baseDir,
                NON_DEFAULT_OPTIONS_SLY)));
        assertFalse("Did not expect compilation warnings.", validateMojo.hasWarnings());
        assertFalse("Did not expect compilation errors.", validateMojo.hasErrors());
    }

    @Test
    public void testDataSlyTestConstantValues() throws Exception {
        DefaultBuildContext context = spy(new DefaultBuildContext());
        File baseDir = new File(System.getProperty("basedir"));
        ValidateMojo validateMojo = getMojo(baseDir, DATA_SLY_TEST_CONSTANT_VALUES_POM_XML, context);
        Exception exception = null;
        try {
            validateMojo.execute();
        } catch (MojoFailureException e) {
            exception = e;
        }
        List<File> processedFiles = validateMojo.getProcessedFiles();
        assertNotNull("Expected a MojoFailureException.", exception);
        assertEquals("Expected 1 file to process.", 1, processedFiles.size());
        assertTrue("Expected " + DATA_SLY_TEST_CONSTANT_VALUES_SLY + " to be the processed file.", processedFiles.contains(new File(baseDir,
                DATA_SLY_TEST_CONSTANT_VALUES_SLY)));
        assertTrue("Expected compilation warnings.", validateMojo.hasWarnings());
        verify(context, times(5)).addMessage(any(), anyInt(), anyInt(), anyString(), eq(BuildContext.SEVERITY_WARNING),
                isNull());
    }

    private ValidateMojo getMojo(File baseDir, String pomFile) throws Exception {
        return getMojo(baseDir, pomFile, null);
    }

    private ValidateMojo getMojo(File baseDir, String pomFile, DefaultBuildContext buildContext) throws Exception {
        Logger log = new ConsoleLogger();
        if (buildContext == null) {
            buildContext = new DefaultBuildContext();
        }
        buildContext.enableLogging(log);
        // use lookupConfiguredMojo to also consider default values (https://issues.apache.org/jira/browse/MPLUGINTESTING-23)
        // similar to MojoRule#lookupConfiguredMojo(File, String) but with custom pom file name
        MavenProject project = readMavenProject(baseDir, pomFile);
        MavenSession session = mojoRule.newMavenSession(project);
        MojoExecution execution = mojoRule.newMojoExecution("validate");
        ValidateMojo validateMojo = (ValidateMojo) mojoRule.lookupConfiguredMojo(session, execution);
        validateMojo.setBuildContext(buildContext);
        return validateMojo;
    }

    /**
     * Copied from {@link org.apache.maven.plugin.testing.MojoRule#readMavenProject(java.io.File)} but customized to allow custom pom names
     */
    private MavenProject readMavenProject(File basedir, String pomFileName) throws Exception {
        File pom = new File(basedir, pomFileName);
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setBaseDirectory(basedir);
        ProjectBuildingRequest configuration = request.getProjectBuildingRequest();
        configuration.setRepositorySession(new DefaultRepositorySystemSession());
        MavenProject project = mojoRule.lookup(ProjectBuilder.class).build(pom, configuration).getProject();
        Assert.assertNotNull(project);
        return project;
    }
}
