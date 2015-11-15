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

import static java.util.Collections.emptyList;
import static org.apache.felix.framework.util.FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.File;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.osgi.embedded.services.EmbeddedOSGiServiceProvider;

/**
 * This class can be used to run an embedded OSGi container in order to provide a dynamic extension mechanism for a Java application.
 * It requires two properties:
 * <ul>
 * <li>the directory that should be scanned for OSGi bundles to be installed</li>
 * <li>a collection of services that need access to the OSGi bundle context</li>
 * </ul>
 * Internally the container runs on Felix 5 and uses the FileInstall Bundle to load the extension bundles.
 * <p/>
 * This implementation allows setting following properties:
 * <ul>
 * <li>@see {@link #setBundlesDirectory(File)} - has to be set explicitly, find further information at
 * http://felix.apache.org/site/apache-felix-framework-bundle-cache.html</li>
 * <li>@see {@link #setStorageDirectory(File)} - has to be set explicitly</li>
 * <li>@see {@link #setSystemPackages(String)} - all packages that are exported to the plugins</li>
 * <li>@see {@link #setCleanStorageOnFirstInit(boolean)} - default value is true</li>
 * <li>@see {@link #setRemoteShellPort(String)} - the port of the remote shell</li>
 * </ul>
 */
public class EmbeddedOSGiContainer {

    private static final String PROPERTY_FELIX_FILEINSTALL_DIR = "felix.fileinstall.dir";
    private static final String PROPERTY_FELIX_REMOTE_SHELL_PORT = "osgi.shell.telnet.port";
    private static final String PROPERTY_OSGI_STORAGE_DIR = "org.osgi.framework.storage";
    private static final String PROPERTY_OSGI_STORAGE_CLEAN = "org.osgi.framework.storage.clean";

    private static final boolean DEFAULT_VALUE_OSGI_STORAGE_CLEAN = true;
    private static final String DEFAULT_REMOTE_SHELL_PORT = "6666";

    private static final String VALUE_OSGI_STORAGE_CLEAN_ON_FIRST_INIT = "onFirstInit";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private File bundlesDirectory;
    private File storageDirectory;
    private String systemPackages;
    private boolean cleanStorageOnFirstInit = DEFAULT_VALUE_OSGI_STORAGE_CLEAN;

    private String remoteShellPort = DEFAULT_REMOTE_SHELL_PORT;

    private HostActivator hostActivator = null;
    private Felix felix = null;

    @Inject
    private Collection<EmbeddedOSGiServiceProvider> embeddedOSGiServiceProviders = emptyList();

    private static void checkDirectory(File directory, String type) {
        if (directory == null) {
            throw new EmbeddedOSGiInitializationException("The '" + type + "' directory is not set.");
        }

        if (!directory.exists()) {
            throw new EmbeddedOSGiInitializationException(
                "The " + type + "  directory '" + directory.getAbsolutePath() + "' doesn't exist.");
        }

        if (!directory.isDirectory()) {
            throw new EmbeddedOSGiInitializationException(
                "The " + type + "  directory value '" + directory.getAbsolutePath() + "' is not a directory.");
        }

        if (!directory.canRead()) {
            throw new EmbeddedOSGiInitializationException(
                "The " + type + "  directory '" + directory.getAbsolutePath() + "' is not readable.");
        }
    }

    @PreDestroy
    public void destroy() {
        this.destroyServiceProviders();
        this.stopFelix();
    }

    @PostConstruct
    public void initialize() {
        this.startFelix();
        this.initializeServiceProviders();
    }

    public void setBundlesDirectory(File bundlesDirectory) {
        this.bundlesDirectory = bundlesDirectory;
    }

    public void setCleanStorageOnFirstInit(boolean cleanCacheOnInit) {
        this.cleanStorageOnFirstInit = cleanCacheOnInit;
    }

    public void setEmbeddedOSGiServiceProviders(Collection<EmbeddedOSGiServiceProvider> embeddedOSGiServiceProviders) {
        this.embeddedOSGiServiceProviders = embeddedOSGiServiceProviders;
    }

    public void setRemoteShellPort(String remoteShellPort) {
        this.remoteShellPort = remoteShellPort;
    }

    public void setStorageDirectory(File storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public void setSystemPackages(String systemPackages) {
        this.systemPackages = systemPackages;
    }

    protected void configHostActivator(Map<String, Object> config) {
        List<BundleActivator> activators = new ArrayList<BundleActivator>();
        activators.add(this.createHostActivator());
        config.put(SYSTEMBUNDLE_ACTIVATORS_PROP, activators);
    }

    protected void configSystemExtraClasspath(Map<String, Object> config) {
        config.put(FRAMEWORK_SYSTEMPACKAGES_EXTRA, this.systemPackages);
        this.logger.info("Setting OSGi container property '" + FRAMEWORK_SYSTEMPACKAGES_EXTRA + "': " + this.systemPackages);
    }

    protected Bundle[] getInstalledBundles() {
        return this.hostActivator.getBundles();
    }

    protected void setFrameworkDirectoryProperty(Map<String, Object> config, String property, File directory) {
        checkDirectory(directory, property);

        String path = directory.getAbsolutePath();
        config.put(property, path);

        this.logger.info("Setting framework property '" + property + "': " + path);
    }

    protected void setFrameworkProperty(Map<String, Object> config, String property, String value) {
        config.put(property, value);
        this.logger.info("Setting framework property '" + property + "': " + value);
    }

    protected void startFelix() {
        try {
            this.logger.info("Going to startup embedded OSGi container.");

            this.felix = new Felix(this.configureFelixContainer());
            this.felix.start();

            int hashCode = System.identityHashCode(this.felix);
            this.logger.info("Embedded OSGi container has been started successfully: container-hashCode=" + hashCode);
        } catch (Exception e) {
            throw new EmbeddedOSGiInitializationException(
                "Error while starting embedded OSGi container: container-hashCode=" + System.identityHashCode(this.felix), e);
        }
    }

    protected void stopFelix() {
        try {
            this.logger.info("Going to shutdown embedded OSGi container: container-hashCode=" + System.identityHashCode(this.felix));

            this.felix.stop();
            this.felix.waitForStop(0);

            this.logger.info("Shutdown of an embedded OSGi container completed successfully: container-hashCode="
                + System.identityHashCode(this.felix));
        } catch (BundleException e) {
            this.logger.error(
                "Error while shuting down embedded OSGi container: container-hashCode=" + System.identityHashCode(this.felix), e);
        } catch (InterruptedException e) {
            this.logger.error(
                "Error while shuting down embedded OSGi container: container-hashCode=" + System.identityHashCode(this.felix), e);
        }
    }

    private void configFileInstallBundle(Map<String, Object> config) {
        this.setFrameworkDirectoryProperty(config, PROPERTY_FELIX_FILEINSTALL_DIR, this.bundlesDirectory);
    }

    private void configRemoteShellBundle(Map<String, Object> config) {
        this.setFrameworkProperty(config, PROPERTY_FELIX_REMOTE_SHELL_PORT, this.remoteShellPort);
    }

    private void configStorageCleanup(Map<String, Object> config) {
        if (!this.cleanStorageOnFirstInit) {
            return;
        }

        String onFirstInitValue = VALUE_OSGI_STORAGE_CLEAN_ON_FIRST_INIT;
        config.put(PROPERTY_OSGI_STORAGE_CLEAN, onFirstInitValue);
        this.logger.info("Setting framework property '" + PROPERTY_OSGI_STORAGE_CLEAN + "': " + onFirstInitValue);
    }

    private void configStorageDirectory(Map<String, Object> config) {
        if (this.storageDirectory == null) {
            return;
        }

        this.setFrameworkDirectoryProperty(config, PROPERTY_OSGI_STORAGE_DIR, this.storageDirectory);
    }

    private Map<String, Object> configureFelixContainer() {
        Map<String, Object> config = new HashMap<String, Object>();

        this.configHostActivator(config);
        this.configSystemExtraClasspath(config);
        this.configFileInstallBundle(config);
        this.configRemoteShellBundle(config);
        this.configStorageDirectory(config);
        this.configStorageCleanup(config);

        return config;
    }

    private BundleActivator createHostActivator() {
        this.hostActivator = new HostActivator();
        return this.hostActivator;
    }

    private void destroyServiceProviders() {
        this.logger.info("Going to destroy " + this.embeddedOSGiServiceProviders.size() + " service provider(s).");

        for (EmbeddedOSGiServiceProvider serviceProvider : this.embeddedOSGiServiceProviders) {
            serviceProvider.destroy();

            this.logger.info("Destroyed service provider: " + serviceProvider.getClass().getName() + "; service-provider-hashCode="
                + System.identityHashCode(serviceProvider));
        }
    }

    private void initializeServiceProviders() {
        this.logger.info("Going to initialize " + this.embeddedOSGiServiceProviders.size() + " service provider(s).");

        for (EmbeddedOSGiServiceProvider serviceProvider : this.embeddedOSGiServiceProviders) {
            serviceProvider.initialize(this.hostActivator.getBundleContext());

            this.logger.info("Initialized service provider: " + serviceProvider.getClass().getName() + "; service-provider-hashCode="
                + System.identityHashCode(serviceProvider));
        }
    }
}
