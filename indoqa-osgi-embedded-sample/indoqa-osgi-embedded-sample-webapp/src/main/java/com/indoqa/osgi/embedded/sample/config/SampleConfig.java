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
package com.indoqa.osgi.embedded.sample.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.indoqa.osgi.embedded.container.ContainerConfiguration;
import com.indoqa.osgi.embedded.container.EmbeddedOSGiContainer;
import com.indoqa.osgi.embedded.container.EmbeddedOSGiContainerInitializationException;
import com.indoqa.osgi.embedded.services.EmbeddedOSGiServiceProvider;

@Configuration
public class SampleConfig {

    @Inject
    private Collection<EmbeddedOSGiServiceProvider> serviceProviders;

    @Bean
    public EmbeddedOSGiContainer createEmbeddedOSGiContainer() {
        EmbeddedOSGiContainer container = new EmbeddedOSGiContainer();

        // Register SLF4J
        container.addSystemPackage("org.slf4j;version=1.7.12");
        container.addSystemPackage("org.slf4j.spi;version=1.7.12");
        container.addSystemPackage("org.slf4j.helpers;version=1.7.12");

        // Register the sample interface
        container.addSystemPackage("com.indoqa.osgi.embedded.sample.interfaces");

        Path bundlesDirectory = this.initializeDirectory(Paths.get("./target/sample-bundles"));
        Path storageDirectory = this.initializeDirectory(Paths.get("./target/sample-storage"));

        ContainerConfiguration config = new ContainerConfiguration().setFileInstallDir(bundlesDirectory)
            .setFrameworkStorage(storageDirectory)
            .setEnableRemoteShell(true);
        container.setContainerConfiguration(config);

        container.setEmbeddedOSGiServiceProviders(this.serviceProviders);

        return container;
    }

    private Path initializeDirectory(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new EmbeddedOSGiContainerInitializationException("Error while creating directory '" + dir.toAbsolutePath() + "'.",
                e);
        }
        return dir;
    }
}
