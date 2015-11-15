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
package com.indoqa.osgi.embedded.sample.webapp;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;

import com.indoqa.osgi.embedded.sample.interfaces.DateService;
import com.indoqa.osgi.embedded.sample.interfaces.DateServiceProvider;

@WebServlet(urlPatterns = "/")
public class SampleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private ApplicationContext applicationContext;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.applicationContext = WebappIntiallizer.getApplicationContext();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        DateServiceProvider dateServiceProvider = this.applicationContext.getBean(DateServiceProvider.class);
        if (dateServiceProvider == null) {
            writer.write("\nCouldn't find bean of type " + DateService.class.getName());
        } else {
            DateService[] dateServices = dateServiceProvider.getDateServices();
            writer.write("\nFound " + dateServices.length + " date service instance(s).");

            for (DateService eachDateService : dateServices) {
                writer.write("\n* DateService output: " + eachDateService.getDate());
            }
        }
    }
}
