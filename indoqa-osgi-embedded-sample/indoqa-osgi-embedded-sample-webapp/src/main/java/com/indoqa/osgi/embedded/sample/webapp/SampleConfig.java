package com.indoqa.osgi.embedded.sample.webapp;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.indoqa.osgi.embedded.container.EmbeddedOSGiContainer;

@Configuration
public class SampleConfig {

    @Bean
    public EmbeddedOSGiContainer createEmbeddedOSGiContainer() {
        EmbeddedOSGiContainer embeddedOSGiContainer = new EmbeddedOSGiContainer();

        File bundlesDirectory = new File("./target/sample-bundles");
        bundlesDirectory.mkdirs();
        embeddedOSGiContainer.setBundlesDirectory(bundlesDirectory);

        File storageDirectory = new File("./target/sample-storage");
        storageDirectory.mkdirs();
        embeddedOSGiContainer.setStorageDirectory(storageDirectory);

        embeddedOSGiContainer
            .setSystemPackages("com.indoqa.osgi.embedded.sample.interface,com.indoqa.osgi.embedded.sample.interfaces");
        return embeddedOSGiContainer;
    }
}
