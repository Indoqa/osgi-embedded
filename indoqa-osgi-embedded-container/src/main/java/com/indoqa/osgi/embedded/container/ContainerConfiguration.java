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

import static com.indoqa.osgi.embedded.container.DirectoryValidator.checkDirectory;

import java.nio.file.Path;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerConfiguration.class);

    private static final String PROPERTY_FILEINSTALL_POLL = "felix.fileinstall.poll";
    private static final String PROPERTY_FILEINSTALL_DIR = "felix.fileinstall.dir";
    private static final String PROPERTY_FILEINSTALL_LOG_LEVEL = "felix.fileinstall.log.level";
    private static final String PROPERTY_FILEINSTALL_NEW_START = "felix.fileinstall.bundles.new.start";
    private static final String PROPERTY_FILEINSTALL_FILTER = "felix.fileinstall.filter";
    private static final String PROPERTY_FILEINSTALL_TMP_DIR = "felix.fileinstall.tmpdir";
    private static final String PROPERTY_FILEINSTALL_NO_INITIAL_DELAY = "felix.fileinstall.noInitialDelay";
    private static final String PROPERTY_FILEINSTALL_START_TRANSIENT = "felix.fileinstall.bundles.startTransient";
    private static final String PROPERTY_FILEINSTALL_START_ACTIVATION_POLICY = "felix.fileinstall.bundles.startActivationPolicy";
    private static final String PROPERTY_FILEINSTALL_START_LEVEL = "felix.fileinstall.start.level";
    private static final String PROPERTY_FILEINSTALL_ACTIVE_LEVEL = "felix.fileinstall.active.level";
    private static final String PROPERTY_FILEINSTALL_ENABLE_CONFIG_SAVE = "felix.fileinstall.enableConfigSave";
    private static final String PROPERTY_FILEINSTALL_UPDATE_WITH_LISTENERS = "felix.fileinstall.bundles.updateWithListeners";
    private static final int DEFAULT_FILEINSTALL_LOG_LEVEL = 2;

    private static final String PROPERTY_OSGI_STORAGE_DIR = "org.osgi.framework.storage";
    private static final String PROPERTY_OSGI_STORAGE_CLEAN = "org.osgi.framework.storage.clean";
    private static final String DEFAULT_OSGI_STORAGE_CLEAN = "onFirstInit";

    private static final String PROPERTY_REMOTE_SHELL_PORT = "osgi.shell.telnet.port";
    private static final String DEFAULT_REMOTE_SHELL_PORT = "6666";

    private Integer fileInstallPoll;
    private Path fileInstallDir;
    private Integer fileInstallLogLevel = DEFAULT_FILEINSTALL_LOG_LEVEL;
    private Boolean fileInstallNewStart;
    private String fileInstallFilter;
    private Path fileInstallTmpDir;
    private Boolean fileInstallNoInitialDelay;
    private Boolean fileInstallStartTransient;
    private Boolean fileInstallStartActivitationPolicy;
    private Integer fileInstallStartLevel;
    private Integer fileInstallActiveLevel;
    private Boolean fileInstallEnableConfigSave;
    private Boolean fileInstallUpdateWithListeners;

    private Path frameworkStorage;
    private String frameworkStorageClean = DEFAULT_OSGI_STORAGE_CLEAN;

    private String remoteShellPort = DEFAULT_REMOTE_SHELL_PORT;

    private boolean remoteShellBundlesEnabled = false;
    private boolean slf4jBridgeActivated = false;

    public ContainerConfiguration setEnableRemoteShell(boolean enabled) {
        this.remoteShellBundlesEnabled = enabled;
        return this;
    }

    public ContainerConfiguration setFileInstallActiveLevel(Integer activeLevel) {
        this.fileInstallActiveLevel = activeLevel;
        return this;
    }

    public ContainerConfiguration setFileInstallDir(Path dir) {
        this.fileInstallDir = dir;
        return this;
    }

    public ContainerConfiguration setFileInstallEnableConfigSave(Boolean enableConfigSave) {
        this.fileInstallEnableConfigSave = enableConfigSave;
        return this;
    }

    public ContainerConfiguration setFileInstallFilter(String filter) {
        this.fileInstallFilter = filter;
        return this;
    }

    public ContainerConfiguration setFileInstallLogLevel(Integer logLevel) {
        this.fileInstallLogLevel = logLevel;
        return this;
    }

    public ContainerConfiguration setFileInstallNewStart(Boolean newStart) {
        this.fileInstallNewStart = newStart;
        return this;
    }

    public ContainerConfiguration setFileInstallNoInitialDelay(Boolean noInitialDelay) {
        this.fileInstallNoInitialDelay = noInitialDelay;
        return this;
    }

    public ContainerConfiguration setFileInstallPollIntervall(Integer poll) {
        this.fileInstallPoll = poll;
        return this;
    }

    public ContainerConfiguration setFileInstallStartActivitationPolicy(Boolean startActivitationPolicy) {
        this.fileInstallStartActivitationPolicy = startActivitationPolicy;
        return this;
    }

    public ContainerConfiguration setFileInstallStartLevel(Integer startLevel) {
        this.fileInstallStartLevel = startLevel;
        return this;
    }

    public ContainerConfiguration setFileInstallStartTransient(Boolean startTransient) {
        this.fileInstallStartTransient = startTransient;
        return this;
    }

    public ContainerConfiguration setFileInstallTmpDir(Path tmpDir) {
        this.fileInstallTmpDir = tmpDir;
        return this;
    }

    public ContainerConfiguration setFileInstallUpdateWithListeners(Boolean updateWithListeners) {
        this.fileInstallUpdateWithListeners = updateWithListeners;
        return this;
    }

    public ContainerConfiguration setFrameworkStorage(Path frameworkStorage) {
        this.frameworkStorage = frameworkStorage;
        return this;
    }

    public ContainerConfiguration setFrameworkStorageClean(String frameworkStorageClean) {
        this.frameworkStorageClean = frameworkStorageClean;
        return this;
    }

    public ContainerConfiguration setRemoteShellPort(String remoteShellPort) {
        this.remoteShellPort = remoteShellPort;
        return this;
    }

    public ContainerConfiguration setSlf4jBridgeActivated(boolean activated) {
        this.slf4jBridgeActivated = activated;
        return this;
    }

    protected void apply(Map<String, Object> config) {
        this.applyProperty(config, PROPERTY_FILEINSTALL_POLL, this.fileInstallPoll);
        this.applyProperty(config, PROPERTY_FILEINSTALL_LOG_LEVEL, this.fileInstallLogLevel);
        this.applyProperty(config, PROPERTY_FILEINSTALL_NEW_START, this.fileInstallNewStart);
        this.applyProperty(config, PROPERTY_FILEINSTALL_FILTER, this.fileInstallFilter);
        this.applyProperty(config, PROPERTY_FILEINSTALL_TMP_DIR, this.fileInstallTmpDir);
        this.applyProperty(config, PROPERTY_FILEINSTALL_NO_INITIAL_DELAY, this.fileInstallNoInitialDelay);
        this.applyProperty(config, PROPERTY_FILEINSTALL_START_TRANSIENT, this.fileInstallStartTransient);
        this.applyProperty(config, PROPERTY_FILEINSTALL_START_ACTIVATION_POLICY, this.fileInstallStartActivitationPolicy);
        this.applyProperty(config, PROPERTY_FILEINSTALL_START_LEVEL, this.fileInstallStartLevel);
        this.applyProperty(config, PROPERTY_FILEINSTALL_ACTIVE_LEVEL, this.fileInstallActiveLevel);
        this.applyProperty(config, PROPERTY_FILEINSTALL_ENABLE_CONFIG_SAVE, this.fileInstallEnableConfigSave);
        this.applyProperty(config, PROPERTY_FILEINSTALL_UPDATE_WITH_LISTENERS, this.fileInstallUpdateWithListeners);

        checkDirectory(this.fileInstallDir, PROPERTY_FILEINSTALL_DIR);
        this.applyProperty(config, PROPERTY_FILEINSTALL_DIR, this.fileInstallDir.toAbsolutePath().toString());

        checkDirectory(this.frameworkStorage, PROPERTY_OSGI_STORAGE_DIR);
        this.applyProperty(config, PROPERTY_OSGI_STORAGE_DIR, this.frameworkStorage.toAbsolutePath().toString());

        this.applyProperty(config, PROPERTY_OSGI_STORAGE_CLEAN, this.frameworkStorageClean);

        if (this.areRemoteShellBundlesEnabled()) {
            this.applyProperty(config, PROPERTY_REMOTE_SHELL_PORT, this.remoteShellPort);
        }
    }

    protected boolean areRemoteShellBundlesEnabled() {
        return this.remoteShellBundlesEnabled;
    }

    protected boolean isSlf4jBridgeActivated() {
        return this.slf4jBridgeActivated;
    }

    private void applyProperty(Map<String, Object> config, String name, Object value) {
        if (value != null) {
            config.put(name, String.valueOf(value));
            LOGGER.info("Setting property '" + name + "': " + value);
        }
    }
}
