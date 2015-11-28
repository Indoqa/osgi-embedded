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

import java.util.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
 * Internally the container runs on Felix 5 and uses the FileInstall Bundle
 * (https://felix.apache.org/documentation/subprojects/apache-felix-file-install.html) to load the extension bundles.
 * <p/>
 * This implementation allows setting following properties:
 * <ul>
 * <li>@see {@link #setSystemPackages(String)} - all packages that are exported to the plugins</li>
 * <li>@see {@link ContainerConfiguration} for the possible configuration options of the pre-installed bundles</li>
 * </ul>
 */
public class EmbeddedOSGiContainer {

    private static final String SYSTEM_PACKAGE_SEPARATOR = ",";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Felix felix;
    private ContainerConfiguration containerConfiguration = new ContainerConfiguration();
    private HostActivator hostActivator;
    private StringBuilder systemPackages = new StringBuilder();

    private Collection<EmbeddedOSGiServiceProvider> embeddedOSGiServiceProviders = emptyList();

    public void addSystemPackage(String additionalPackage) {
        if (additionalPackage == null || "".equals(additionalPackage)) {
            throw new EmbeddedOSGiContainerInitializationException("An empty system package cannot be added.");
        }

        if (this.systemPackages.length() > 0) {
            this.systemPackages.append(SYSTEM_PACKAGE_SEPARATOR);
        }

        this.systemPackages.append(additionalPackage);
    }

    @PreDestroy
    public void destroy() {
        this.destroyServiceProviders();
        this.stopFelix();
    }

    public Collection<Bundle> getInstalledBundles() {
        return Arrays.asList(this.hostActivator.getBundles());
    }

    @PostConstruct
    public void initialize() {
        this.createHostActivator();
        this.exportSlf4jPackages();
        this.startFelix();
        this.initializeServiceProviders();
    }

    public void setContainerConfiguration(ContainerConfiguration containerConfiguration) {
        Objects.nonNull(containerConfiguration);
        this.containerConfiguration = containerConfiguration;
    }

    public void setEmbeddedOSGiServiceProviders(Collection<EmbeddedOSGiServiceProvider> providers) {
        Objects.nonNull(providers);
        this.embeddedOSGiServiceProviders = providers;
    }

    protected void configHostActivator(Map<String, Object> config) {
        List<BundleActivator> activators = new ArrayList<BundleActivator>();
        activators.add(this.hostActivator);
        config.put(SYSTEMBUNDLE_ACTIVATORS_PROP, activators);
    }

    protected void configSystemExtraClasspath(Map<String, Object> config) {
        config.put(FRAMEWORK_SYSTEMPACKAGES_EXTRA, this.systemPackages.toString());
        this.logger.info("Setting property '" + FRAMEWORK_SYSTEMPACKAGES_EXTRA + "': " + this.systemPackages);
    }

    protected void startFelix() {
        try {
            this.logger.info("Going to startup embedded OSGi container.");

            this.felix = new Felix(this.createFelixContainerConfiguration());
            this.felix.start();

            int hashCode = System.identityHashCode(this.felix);
            this.logger.info("Embedded OSGi container has been started successfully: container-hashCode=" + hashCode);
        } catch (Exception e) {
            throw new EmbeddedOSGiContainerInitializationException(
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
        } catch (BundleException | InterruptedException e) {
            this.logger.error(
                "Error while shuting down embedded OSGi container: container-hashCode=" + System.identityHashCode(this.felix), e);
        }
    }

    private void configBundles(Map<String, Object> config) {
        this.containerConfiguration.apply(config);
    }

    private Map<String, Object> createFelixContainerConfiguration() {
        Map<String, Object> config = new HashMap<String, Object>();

        this.configHostActivator(config);
        this.configSystemExtraClasspath(config);
        this.configBundles(config);

        return config;
    }

    private BundleActivator createHostActivator() {
        this.hostActivator = new HostActivator(this.containerConfiguration.areRemoteShellBundlesEnabled(),
            this.containerConfiguration.isSlf4jBridgeActivated());
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

    private void exportSlf4jPackages() {
        if (this.containerConfiguration.isSlf4jBridgeActivated()) {
            this.addSystemPackage("org.osgi.service.log");
            this.addSystemPackage("org.slf4j;version=1.7.12");
            this.addSystemPackage("org.slf4j.spi;version=1.7.12");
            this.addSystemPackage("org.slf4j.helpers;version=1.7.12");
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
