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
package com.indoqa.osgi.embedded.sample.provider;

import javax.inject.Named;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.indoqa.osgi.embedded.sample.interfaces.DateService;
import com.indoqa.osgi.embedded.services.EmbeddedOSGiServiceProvider;

@Named
public class DateServiceProvider implements EmbeddedOSGiServiceProvider {

    private ServiceTracker<DateService, DateService> dateServiceTracker;

    @Override
    public void destroy() {
        this.dateServiceTracker.close();
    }

    public DateService[] getDateServices() {
        return this.dateServiceTracker.getServices(new DateService[0]);
    }

    @Override
    public void initialize(BundleContext bundleContext) {
        this.dateServiceTracker = new ServiceTracker<DateService, DateService>(bundleContext, DateService.class.getName(), null);
        this.dateServiceTracker.open();
    }
}
