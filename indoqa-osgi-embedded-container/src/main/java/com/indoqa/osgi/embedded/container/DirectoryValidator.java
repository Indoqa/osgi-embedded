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

import java.nio.file.Files;
import java.nio.file.Path;

/*default*/ final class DirectoryValidator {

    private DirectoryValidator() {
        // hide utility class constructor
    }

    static void checkDirectory(Path directory, String type) {
        if (directory == null) {
            throw new EmbeddedOSGiContainerInitializationException("The '" + type + "' directory is not set.");
        }

        if (!Files.exists(directory)) {
            throw new EmbeddedOSGiContainerInitializationException(
                "The " + type + "  directory '" + directory.toAbsolutePath() + "' doesn't exist.");
        }

        if (!Files.isDirectory(directory)) {
            throw new EmbeddedOSGiContainerInitializationException(
                "The " + type + "  directory value '" + directory.toAbsolutePath() + "' is not a directory.");
        }

        if (!Files.isReadable(directory)) {
            throw new EmbeddedOSGiContainerInitializationException(
                "The " + type + "  directory '" + directory.toAbsolutePath() + "' is not readable.");
        }
    }
}
