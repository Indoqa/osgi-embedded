/*
 * Licensed to the Indoqa Software Design und Beratung GmbH (Indoqa) under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Indoqa licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indoqa.osgi.embedded.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load this bundle in order to start a basic set of bundles. This basic set contains:
 * <ul>
 * <li>Felix Log</li>
 * <li>Felix Config Admin</li>
 * <li>Felix FileInstall</li>
 * <li>Felix Gogo Shell (including Gogo Command and Gogo Runtime)</li>
 * <li>Felix Remote Shell</li>
 * </ul>
 */
/* default */ class HostActivator implements BundleActivator {

    private static final String INITIAL_BUNDLES_FOLDER = "initial-bundles/";

    private static final Map<String, BundleType> BUNDLES = new ConcurrentHashMap<>();

    static {
        BUNDLES.put("org.apache.felix.configadmin-1.8.8.jar", BundleType.MANDATORY_BUNDLE);
        BUNDLES.put("org.apache.felix.log-1.0.1.jar", BundleType.MANDATORY_BUNDLE);
        BUNDLES.put("org.apache.felix.fileinstall-3.5.0.jar", BundleType.MANDATORY_BUNDLE);

        BUNDLES.put("org.apache.felix.gogo.command-0.16.0.jar", BundleType.REMOTE_SHELL_BUNDLE);
        BUNDLES.put("org.apache.felix.gogo.runtime-0.16.2.jar", BundleType.REMOTE_SHELL_BUNDLE);
        BUNDLES.put("org.apache.felix.gogo.shell-0.12.0.jar", BundleType.REMOTE_SHELL_BUNDLE);

        BUNDLES.put("org.apache.felix.shell.remote-1.1.2.jar", BundleType.LOCAL_SHELL_BUNDLE);

        BUNDLES.put("com.indoqa.osgi.slf4j.bridge-1.0.0.SNAPSHOT.jar", BundleType.SLF4J_BRIDGE);
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean remoteShellEnabled;
    private final boolean slf4jBridgingActivated;
    private BundleContext bundleContext;

    public HostActivator(boolean remoteShellEnabled, boolean slf4jBridgingActivated) {
        this.remoteShellEnabled = remoteShellEnabled;
        this.slf4jBridgingActivated = slf4jBridgingActivated;
    }

    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    public Bundle[] getBundles() {
        if (this.bundleContext != null) {
            return this.bundleContext.getBundles();
        }

        return null;
    }

    @Override
    public void start(BundleContext context) {
        this.bundleContext = context;
        this.startBundles();
    }

    @Override
    public void stop(BundleContext context) {
        this.bundleContext = null;
    }

    protected void closeBundleInputStream(String resourceName, InputStream bundleInputStream) {
        if (bundleInputStream == null) {
            return;
        }

        try {
            bundleInputStream.close();
        } catch (IOException e) {
            throw new EmbeddedOSGiContainerInitializationException("Can't initialize bundle '" + resourceName + "'.", e);
        }
    }

    private void startBundle(String bundleFileName) {
        String resourceName = INITIAL_BUNDLES_FOLDER + bundleFileName;
        InputStream bundleInputStream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
        try {
            Bundle bundle = this.bundleContext.installBundle(bundleFileName, bundleInputStream);
            bundle.start();
        } catch (BundleException e) {
            String msg = "Can't initialize bundle '" + resourceName + "'.";
            this.logger.error(msg, e);
            throw new EmbeddedOSGiContainerInitializationException(msg, e);
        } finally {
            this.closeBundleInputStream(resourceName, bundleInputStream);
        }

        this.logger.info("Started bundle: " + bundleFileName);
    }

    private void startBundles() {
        if (this.slf4jBridgingActivated) {
            this.startBundlesByType(BundleType.SLF4J_BRIDGE);
        }
        this.startBundlesByType(BundleType.MANDATORY_BUNDLE);

        if (this.remoteShellEnabled) {
            this.startBundlesByType(BundleType.LOCAL_SHELL_BUNDLE);
            this.startBundlesByType(BundleType.REMOTE_SHELL_BUNDLE);
        }
    }

    private void startBundlesByType(BundleType type) {
        for (Entry<String, BundleType> entry : BUNDLES.entrySet()) {
            if (type.equals(entry.getValue())) {
                this.startBundle(entry.getKey());
            }
        }
    }

    private enum BundleType {

        MANDATORY_BUNDLE, SLF4J_BRIDGE, REMOTE_SHELL_BUNDLE, LOCAL_SHELL_BUNDLE
    }
}
