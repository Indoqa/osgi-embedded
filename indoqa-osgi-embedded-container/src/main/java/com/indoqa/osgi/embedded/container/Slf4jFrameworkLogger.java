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

import org.apache.felix.framework.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

// see https://searchcode.com/codesearch/view/29423790/#l-15
public class Slf4jFrameworkLogger extends Logger {

    public Slf4jFrameworkLogger() {
        System.out.println("--> created!");
        this.setLogLevel(4);
    }

    @Override
    protected void doLog(Bundle bundle, ServiceReference sr, int level, String msg, Throwable throwable) {
        System.out.println("Logging2: " + msg);
    }

    @Override
    protected void doLog(int level, String msg, Throwable throwable) {
        System.out.println("Logging1: " + msg);
    }
}
