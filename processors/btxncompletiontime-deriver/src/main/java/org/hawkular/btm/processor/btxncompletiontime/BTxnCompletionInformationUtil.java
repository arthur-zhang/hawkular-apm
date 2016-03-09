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
package org.hawkular.btm.processor.btxncompletiontime;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hawkular.btm.api.model.btxn.ContainerNode;
import org.hawkular.btm.api.model.btxn.CorrelationIdentifier;
import org.hawkular.btm.api.model.btxn.CorrelationIdentifier.Scope;
import org.hawkular.btm.api.model.btxn.Node;
import org.hawkular.btm.api.model.btxn.Producer;

/**
 * This class represents utility functions to help calculate completion time for
 * business transaction instances..
 *
 * @author gbrown
 */
public class BTxnCompletionInformationUtil {

    private static final Logger log = Logger.getLogger(BTxnCompletionInformationUtil.class.getName());

    /**
     * This method initialises the completion time information for a business transaction
     * instance.
     *
     * @param ci The information
     * @param fragmentBaseTime The base time for the fragment (ns)
     * @param baseDuration The base duration (ms)
     * @param n The node
     */
    public static void initialiseCommunications(BTxnCompletionInformation ci, long fragmentBaseTime,
            long baseDuration, Node n) {
        if (n.getClass() == Producer.class) {
            // Get interaction id
            List<CorrelationIdentifier> cids = n.getCorrelationIds(Scope.Interaction);

            if (!cids.isEmpty()) {
                BTxnCompletionInformation.Communication c = new BTxnCompletionInformation.Communication();

                for (int i = 0; i < cids.size(); i++) {
                    c.getIds().add(cids.get(i).getValue());
                }

                c.setMultipleConsumers(((Producer) n).multipleConsumers());

                // Calculate the base duration for the communication
                c.setBaseDuration(baseDuration + TimeUnit.MILLISECONDS.convert((n.getBaseTime() - fragmentBaseTime),
                        TimeUnit.NANOSECONDS));

                c.setExpire(System.currentTimeMillis() + BTxnCompletionInformation.Communication.DEFAULT_EXPIRY_WINDOW);

                if (log.isLoggable(Level.FINEST)) {
                    log.finest("Adding communication to completion information: ci=" + ci + " comms=" + c);
                }

                ci.getCommunications().add(c);
            }
        } else if (n.containerNode()) {
            ContainerNode cn = (ContainerNode) n;
            for (int i = 0; i < cn.getNodes().size(); i++) {
                initialiseCommunications(ci, fragmentBaseTime, baseDuration, cn.getNodes().get(i));
            }
        }
    }

}
