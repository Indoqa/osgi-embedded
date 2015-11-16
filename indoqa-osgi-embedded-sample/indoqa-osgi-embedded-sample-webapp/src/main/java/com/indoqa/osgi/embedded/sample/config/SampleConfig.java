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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.indoqa.osgi.embedded.container.ContainerConfiguration;
import com.indoqa.osgi.embedded.container.EmbeddedOSGiContainer;
import com.indoqa.osgi.embedded.container.EmbeddedOSGiContainerInitializationException;

@Configuration
@Profile("webapp")
public class SampleConfig {

    private static void createDirectory(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new EmbeddedOSGiContainerInitializationException("Error while creating directory '" + path.toAbsolutePath() + "'.",
                e);
        }
    }

    @Bean
    public EmbeddedOSGiContainer createEmbeddedOSGiContainer() {
        EmbeddedOSGiContainer embeddedOSGiContainer = new EmbeddedOSGiContainer();

        ContainerConfiguration config = new ContainerConfiguration();
        embeddedOSGiContainer.setContainerConfiguration(config);

        Path bundlesDirectory = Paths.get("./target/sample-bundles");
        createDirectory(bundlesDirectory);
        config.setFileInstallDir(bundlesDirectory);

        Path storageDirectory = Paths.get("./target/sample-storage");
        createDirectory(storageDirectory);
        config.setFrameworkStorage(storageDirectory);

        embeddedOSGiContainer.setSystemPackages("com.indoqa.osgi.embedded.sample.interfaces");
        return embeddedOSGiContainer;
    }
}
