/*
 * Copyright 2005-2014 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.appfactory.ext.appserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.common.threading.StratosThreadPool;
import org.apache.stratos.messaging.domain.topology.Cluster;
import org.apache.stratos.messaging.domain.topology.Member;
import org.apache.stratos.messaging.domain.topology.MemberStatus;
import org.apache.stratos.messaging.domain.topology.Service;
import org.apache.stratos.messaging.event.Event;
import org.apache.stratos.messaging.event.topology.CompleteTopologyEvent;
import org.apache.stratos.messaging.listener.topology.CompleteTopologyEventListener;
import org.apache.stratos.messaging.message.receiver.topology.TopologyEventReceiver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/*
* This class is use to listen to complete stratos topology
* It decides whether the current node should be a notifier based on the node ip in the topology
* */

public class LeaderElector {

    private static final Log log = LogFactory.getLog(LeaderElector.class);

    private static LeaderElector leaderElector = new LeaderElector();
    private static boolean isNotifyEligible;
    private boolean terminated;
    private TopologyEventReceiver topologyEventReceiver;

    private LeaderElector() {
        isNotifyEligible = false;
        this.terminated = false;
        this.topologyEventReceiver = new TopologyEventReceiver();
        ExecutorService executorService = StratosThreadPool.getExecutorService(
                "appfactory.stratos.thread.executor.service2", 1);
        topologyEventReceiver.setExecutorService(executorService);
        addEvenListener();
        topologyEventReceiver.execute();
        log.info("Stratos Manager topology receiver thread started");
    }

    public static boolean isIsNotifyEligible() {
        return isNotifyEligible;
    }

    public static LeaderElector getInstance() {
        return leaderElector;
    }

    private void addEvenListener() {
        //add listener to Complete Topology Event
        topologyEventReceiver.addEventListener(new CompleteTopologyEventListener() {

            @Override
            protected void onEvent(Event event) {

                if(log.isDebugEnabled()) {
                    log.debug("[CompleteTopologyEventListener] Received: " + event.getClass());
                }

                try {
                    String currentNodeIp = InetAddress.getLocalHost().getHostAddress();
                    Map<String, InetAddress> memberIpMap = new HashMap<String, InetAddress>();

                    for (Service service : ((CompleteTopologyEvent) event).getTopology()
                            .getServices()) {
                        for (Cluster cluster : service.getClusters()) {
                            for (Member member : cluster.getMembers()) {
                                MemberStatus memStatus = member.getStatus();
                                if (MemberStatus.Active.equals(memStatus)) {
                                    memberIpMap.put(member.getDefaultPublicIP(),
                                                    InetAddress.getByName(member.getDefaultPublicIP()));
                                }
                            }
                        }
                    /*if the current node ip is not there in the service cluster; it should be a
                    different service cluster*/
                        if (memberIpMap.get(currentNodeIp) != null) {
                            break;
                        } else {
                            memberIpMap.clear();
                        }
                    }

                    if (memberIpMap.isEmpty()) {
                        return;
                    }

                    List<InetAddress> memberIpList = new ArrayList<InetAddress>(memberIpMap.values());
                    Collections.sort(memberIpList, new Comparator<InetAddress>() {

                        @Override
                        public int compare(InetAddress addr1, InetAddress addr2) {
                            byte[] ba1 = addr1.getAddress();
                            byte[] ba2 = addr2.getAddress();

                            // general ordering: ipv4 before ipv6
                            if (ba1.length < ba2.length) {
                                return -1;
                            }
                            if (ba1.length > ba2.length) {
                                return 1;
                            }

                            // we have 2 ips of the same type, so we have to compare each byte
                            for (int i = 0; i < ba1.length; i++) {
                                int b1 = unsignedByteToInt(ba1[i]);
                                int b2 = unsignedByteToInt(ba2[i]);
                                if (b1 == b2) {
                                    continue;
                                }
                                if (b1 < b2) {
                                    return -1;
                                } else {
                                    return 1;
                                }
                            }
                            return 0;
                        }

                        private int unsignedByteToInt(byte b) {
                            return (int) b & 0xFF;
                        }
                    });

                    if ((memberIpMap.get(currentNodeIp) != null) && currentNodeIp.equals(
                            memberIpList.get(0).getHostAddress())) {
                        isNotifyEligible = true;
                    } else {
                        isNotifyEligible = false;
                    }

                } catch (UnknownHostException e) {
                    log.error("Unable to identify the host ip " + e.getMessage(), e);
                }
            }
        });
    }

    //terminate Topology Receiver
    public void terminate() {
        topologyEventReceiver.terminate();
        log.info("Stratos Manager topology receiver thread terminated");
    }
}
