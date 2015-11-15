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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.indoqa.osgi.embedded.container.EmbeddedOSGiContainer;
import com.indoqa.osgi.embedded.sample.interfaces.DateService;
import com.indoqa.osgi.embedded.sample.interfaces.DateServiceProvider;
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
        this.embeddedOSGiContainer = new EmbeddedOSGiContainer();

        File bundlesDirectory = new File(DIR_BUNDLES);
        bundlesDirectory.mkdirs();
        FileUtils.cleanDirectory(bundlesDirectory);
        this.embeddedOSGiContainer.setBundlesDirectory(bundlesDirectory);

        File storageDirectory = new File(DIR_STORAGE);
        storageDirectory.mkdirs();
        FileUtils.cleanDirectory(storageDirectory);
        this.embeddedOSGiContainer.setStorageDirectory(storageDirectory);

        this.embeddedOSGiContainer
            .setSystemPackages("com.indoqa.osgi.embedded.sample.interface,com.indoqa.osgi.embedded.sample.interfaces");

        Collection<EmbeddedOSGiServiceProvider> providers = new ArrayList<>();
        this.dateServiceProvider = new DateServiceProvider();
        providers.add(this.dateServiceProvider);
        this.embeddedOSGiContainer.setEmbeddedOSGiServiceProviders(providers);

        this.embeddedOSGiContainer.initialize();
    }

    @Test
    public void installBundle() throws IOException {
        DateService[] dateServices = this.dateServiceProvider.getDateServices();
        assertEquals(0, dateServices.length);

        FileUtils.copyFile(new File(DIR_SAMPLE_BUNDLE + FILE_NAME_SAMPLE_BUNDLE), new File(DIR_BUNDLES + FILE_NAME_SAMPLE_BUNDLE));

        this.sleep(5);

        dateServices = this.dateServiceProvider.getDateServices();
        assertEquals(1, dateServices.length);
    }

    @After
    public void shutdownEmbeddedOSGiContainer() {
        this.embeddedOSGiContainer.destroy();
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(SECONDS.toMillis(seconds));
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
