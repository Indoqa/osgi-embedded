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
package com.indoqa.osgi.embedded.test.integration;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.indoqa.osgi.embedded.container.ContainerConfiguration;
import com.indoqa.osgi.embedded.container.EmbeddedOSGiContainer;
import com.indoqa.osgi.embedded.sample.interfaces.DateService;
import com.indoqa.osgi.embedded.sample.provider.DateServiceProvider;
import com.indoqa.osgi.embedded.services.EmbeddedOSGiServiceProvider;

public class OSGiEmbeddedIntegrationTest {

    private static final String DIR_STORAGE = "./target/test-storage/";
    private static final String DIR_BUNDLES = "./target/test-bundles/";

    private static final String DIR_SAMPLE_BUNDLE = "../indoqa-osgi-embedded-sample-bundle/target/";
    private static final String FILE_NAME_SAMPLE_BUNDLE = "indoqa-osgi-embedded-sample-bundle-0.1.0-SNAPSHOT.jar";

    private EmbeddedOSGiContainer embeddedOSGiContainer;
    private DateServiceProvider dateServiceProvider;

    @Before
    public void initializeEmbeddedOSGiContainer() throws IOException {
        Path bundlesDirectory = this.prepareDirectory(DIR_BUNDLES);
        Path storageDirectory = this.prepareDirectory(DIR_STORAGE);
        ContainerConfiguration config = new ContainerConfiguration()
            .setFrameworkStorage(storageDirectory)
            .setSlf4jBridgeActivated(true)
            .addFileInstallDir(bundlesDirectory)
            .setFileInstallNoInitialDelay(true)
            .setFileInstallPollInterval(250);
        Collection<EmbeddedOSGiServiceProvider> providers = this.initializeProviders();

        this.embeddedOSGiContainer = new EmbeddedOSGiContainer();
        this.embeddedOSGiContainer.setContainerConfiguration(config);
        this.embeddedOSGiContainer.setEmbeddedOSGiServiceProviders(providers);
        this.embeddedOSGiContainer.addSystemPackage("com.indoqa.osgi.embedded.sample.interfaces");

        this.embeddedOSGiContainer.initialize();
    }

    @Test
    public void installAndUninstallBundle() throws IOException {
        DateService[] dateServices = this.dateServiceProvider.getDateServices();
        assertEquals(0, dateServices.length);

        File bundle = new File(DIR_BUNDLES + FILE_NAME_SAMPLE_BUNDLE);
        FileUtils.copyFile(new File(DIR_SAMPLE_BUNDLE + FILE_NAME_SAMPLE_BUNDLE), bundle);
        this.sleep(1000);
        dateServices = this.dateServiceProvider.getDateServices();
        assertEquals(1, dateServices.length);

        bundle.delete();
        this.sleep(1000);
        dateServices = this.dateServiceProvider.getDateServices();
        assertEquals(0, dateServices.length);
    }

    @After
    public void shutdownEmbeddedOSGiContainer() {
        this.embeddedOSGiContainer.destroy();
    }

    private Collection<EmbeddedOSGiServiceProvider> initializeProviders() {
        Collection<EmbeddedOSGiServiceProvider> providers = new ArrayList<>();
        this.dateServiceProvider = new DateServiceProvider();
        providers.add(this.dateServiceProvider);
        return providers;
    }

    private Path prepareDirectory(String dir) throws IOException {
        Path path = Paths.get(dir);
        Files.createDirectories(path);
        FileUtils.cleanDirectory(path.toFile());
        return path;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
