/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.apm.instrumenter;

import java.lang.instrument.Instrumentation;

/**
 * This class provides a wrapper around the ByteMan agent, to establish required system
 * properties and the manager class.
 *
 * @author gbrown
 */
public class APMAgent {

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        agentArgs = addManager(agentArgs);
        initProperties();

        org.jboss.byteman.agent.Main.premain(agentArgs, inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        agentArgs = addManager(agentArgs);
        initProperties();

        org.jboss.byteman.agent.Main.agentmain(agentArgs, inst);
    }

    protected static String addManager(String agentArgs) {
        if (agentArgs == null || agentArgs.trim().isEmpty()) {
            agentArgs = "";
        } else {
            agentArgs += ",";
        }
        agentArgs += "manager:org.hawkular.apm.instrumenter.ClientManager";

        return agentArgs;
    }

    protected static void initProperties() {
        System.setProperty("org.jboss.byteman.transform.all", "");
    }
}
