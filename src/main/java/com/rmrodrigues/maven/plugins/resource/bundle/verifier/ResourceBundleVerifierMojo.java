package com.rmrodrigues.maven.plugins.resource.bundle.verifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * The Class MyMojo.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class ResourceBundleVerifierMojo extends AbstractMojo {

    /** The log. */
    private Log log = getLog();
    /**
     * Location of the file.
     */
    @Parameter(property = "mainLocaleFile")
    private String localeFile;

    /** The locales. */
    @Parameter
    private String[] locales;

    /**
     * Execute method.
     *
     * @throws MojoExecutionException the mojo execution exception
     */
    public void execute() throws MojoExecutionException {
        final String mainLocaleFileUrl = localeFile;
        log.info(localeFile);
        if (mainLocaleFileUrl == null) {
            throw new MojoExecutionException("<localeFile> property not found. Please define your main locale file.");
        }
        final File mainLocaleFile = new File(this.localeFile);

        // Validations
        validate(mainLocaleFile);
        final List<BundlePair> bundleList = new ArrayList<BundlePair>();
        initializeFile(bundleList, mainLocaleFileUrl);
        verifyMainResourceFile(bundleList);

        // Load locale files

        initializeLocaleFiles(bundleList);

        validate(bundleList);
    }

    /**
     * Verify main resource file.
     *
     * @param bundleList the bundle list
     * @throws MojoExecutionException
     */
    private void verifyMainResourceFile(List<BundlePair> bundleList) throws MojoExecutionException {
        boolean hasErrors = false;
        final String resourceFileName = new File(bundleList.get(0).getFilePath()).getName();
        log.info("------------------------------------------------------------------------");
        log.info("Checking File: " + resourceFileName);
        log.info("------------------------------------------------------------------------");
        for (Entry<Object, Object> curMainPair : bundleList.get(0).getProperties().entrySet()) {
            if ("".equals((((String) curMainPair.getValue()).trim()))) {
                log.error("The key: \"" + curMainPair.getKey() + "\" has no value. Please check it out.");
                hasErrors = true;
            }
        }
        if (hasErrors) {
            throw new MojoExecutionException(
                    "Resource Bundle: " + resourceFileName + " has keys with no value defined.");
        }
    }

    /**
     * Initialize locale files.
     *
     * @param bundleList the bundle list
     * @throws MojoExecutionException the mojo execution exception
     */
    private void initializeLocaleFiles(List<BundlePair> bundleList) throws MojoExecutionException {
        for (int i = 0; i < locales.length; i++) {
            initializeFile(bundleList, locales[i]);
        }
    }

    /**
     * Initialize file.
     *
     * @param bundleList the bundle list
     * @param localeFile the locale file
     * @throws MojoExecutionException the mojo execution exception
     */
    private void initializeFile(List<BundlePair> bundleList, String localeFile) throws MojoExecutionException {
        Properties prop = null;
        InputStream input = null;
        try {
            prop = new Properties();
            input = new FileInputStream(new File(localeFile));
            prop.load(input);
            bundleList.add(new BundlePair(localeFile, prop));
        } catch (IOException io) {
            log.error(io);
            throw new MojoExecutionException("Unable to load file: " + localeFile);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        }

    }

    /**
     * Validate.
     *
     * @param mainLocaleFile the main locale file
     * @throws MojoExecutionException the mojo execution exception
     */
    private void validate(File mainLocaleFile) throws MojoExecutionException {
        if (!mainLocaleFile.exists()) {
            throw new MojoExecutionException("localeFile: " + this.localeFile + " not found.");
        }
        if (locales == null || locales.length == 0) {
            throw new MojoExecutionException("There are no locales defined.");
        }

    }

    /**
     * Validate.
     *
     * @param bundlePairList the bundle pair list
     * @throws MojoExecutionException the mojo execution exception
     */
    private void validate(List<BundlePair> bundlePairList) throws MojoExecutionException {
        boolean hasErrors = false;
        // Get the main file
        final BundlePair mainBundleResource = bundlePairList.get(0);
        log.info("Main Bundle Resource File: " + mainBundleResource.getFilePath());
        log.debug("Total Entries: " + mainBundleResource.getProperties().entrySet().size() + "\n");
        // Remove the main file from list
        bundlePairList.remove(mainBundleResource);
        // Compare with each other file to find left keys
        for (BundlePair curBundlePair : bundlePairList) {
            final List<String> errorMessages = new ArrayList<String>();
            final List<String> warnMessages = new ArrayList<String>();
            int emptyValues = 0;
            int totalEntries = 0;
            int sameValue = 0;
            int missingEntries = 0;
            log.info("------------------------------------------------------------------------");
            log.info("Checking File: " + curBundlePair.getFilePath());
            log.info("------------------------------------------------------------------------");
            final Properties curProperties = curBundlePair.getProperties();
            for (Entry<Object, Object> curMainPair : mainBundleResource.getProperties().entrySet()) {
                totalEntries++;
                final Object value = curProperties.getProperty(curMainPair.getKey().toString());
                if (value == null) {
                    missingEntries++;
                    errorMessages.add("The key: \"" + curMainPair.getKey() + "\" is missing.");
                } else {
                    if ("".equals(value.toString())) {
                    	errorMessages
                                .add("The key: \"" + curMainPair.getKey() + "\" has no value. Please check it out.");
                        emptyValues++;
                    } else if (curMainPair.getValue().equals(value)) {
                        sameValue++;
                        warnMessages.add("The key: \"" + curMainPair.getKey() + "\" has the same(\"" + value.toString()
                                + "\"). Please check it out.");
                    }

                }
            }
            printWarnMessages(warnMessages);
            printErrorMessages(errorMessages);

            log.info("Total: " + totalEntries + ", Empty Values: " + emptyValues + ", Same Values: " + sameValue
                    + ", Missing Entries: " + missingEntries + "\n");
            if (emptyValues > 0 || missingEntries > 0) {
                hasErrors = true;
            }

        }

        if (hasErrors) {
            throw new MojoExecutionException("There are missing/emptry entries in your Locale resources.");
        }
        log.info("------------------------------------------------------------------------");
        log.info("LOCALE VERIFICATION SUCCESSFUL");
        log.info("------------------------------------------------------------------------");

    }

    /**
     * Prints the warn messages.
     *
     * @param warnMessages the warn messages
     */
    private void printWarnMessages(List<String> warnMessages) {
        for (String message : warnMessages) {
            log.warn(message);
        }

    }

    /**
     * Prints the error messages.
     *
     * @param errorMessages the error messages
     */
    private void printErrorMessages(List<String> errorMessages) {
        for (String message : errorMessages) {
            log.error(message);
        }

    }
}
