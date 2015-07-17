/*
 * Copyright 2014 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.listners.paas;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.common.threading.StratosThreadPool;
import org.apache.stratos.messaging.domain.topology.Cluster;
import org.apache.stratos.messaging.domain.topology.Member;
import org.apache.stratos.messaging.domain.topology.Service;
import org.apache.stratos.messaging.domain.topology.Topology;
import org.apache.stratos.messaging.event.Event;
import org.apache.stratos.messaging.event.topology.MemberActivatedEvent;
import org.apache.stratos.messaging.listener.topology.MemberActivatedEventListener;
import org.apache.stratos.messaging.message.receiver.topology.TopologyEventReceiver;
import org.apache.stratos.messaging.message.receiver.topology.TopologyManager;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.dao.JDBCApplicationDAO;
import org.wso2.carbon.appfactory.core.dto.CartridgeCluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Listener register for stratos member activated event
 */
public class StratosMemberActivatedListener {

    private static final Log log = LogFactory.getLog(StratosMemberActivatedListener.class);
    private static StratosMemberActivatedListener stratosMemberActivatedListener = new StratosMemberActivatedListener();

    private JDBCApplicationDAO applicationDao;
    private TopologyEventReceiver topologyEventReceiver;

    /**
     * Constructor
     */
    private StratosMemberActivatedListener() {
        applicationDao = JDBCApplicationDAO.getInstance();
        topologyEventReceiver = new TopologyEventReceiver();
        ExecutorService executorService = StratosThreadPool.getExecutorService(
                "appfactory.stratos.thread.executor.service1", 1);
        topologyEventReceiver.setExecutorService(executorService);
        addEventListener();
    }

    /**
     * activate the listener
     */
    public void activate() {
        topologyEventReceiver.execute();
        log.info("Stratos member activated receiver thread activated!");
    }

    /**
     * terminate the listener
     */
    public void terminate() {
        if (topologyEventReceiver != null) {
            topologyEventReceiver.terminate();
            log.info("Member activated event receiver thread terminated!");
        }
    }

    /**
     * Get the singleton instance
     *
     * @return {@link StratosMemberActivatedListener}
     */
    public static StratosMemberActivatedListener getInstance() {
        return stratosMemberActivatedListener;
    }

    /**
     * Add Member Activated Event Listener
     */
    private void addEventListener() {
        topologyEventReceiver.addEventListener(new MemberActivatedEventListener() {

            @Override
            protected void onEvent(Event event) {
                if (event instanceof MemberActivatedEvent) {
                    try {
                        // get the read lock
                        TopologyManager.acquireReadLock();

                        if (log.isDebugEnabled()) {
                            log.debug("[MemberActivatedEventListener] Received: " + event.getClass());
                        }
                        // get active member related meta data from event
                        MemberActivatedEvent memberActivatedEvent = (MemberActivatedEvent) event;
                        String clusterId = memberActivatedEvent.getClusterId();
                        String serviceName = memberActivatedEvent.getServiceName();
                        String memberId = memberActivatedEvent.getMemberId();

                        // get the active member
                        Topology topology = TopologyManager.getTopology();
                        Service service = topology.getService(serviceName);
                        if (service != null) {
                            Cluster cluster = service.getCluster(clusterId);
                            if (cluster != null) {
                                Member activeMember = cluster.getMember(memberId);
                                if (activeMember != null) {
                                    // get active public IP Address
                                    String cartridgeActiveIp = getActivePublicIp(activeMember);

                                    if (StringUtils.isNotBlank(cartridgeActiveIp)) {
                                        // persist data to runtime db
                                        persistCartridgeCluster(clusterId, activeMember.getLbClusterId(),
                                                                cartridgeActiveIp);
                                    } else {
                                        log.warn("There is no active IP address for give cartridge!");
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // catch all the exception here and log, since this is a back-ground thread
                        log.error("Error processing member activated event", e);
                    } finally {
                        // release the read lock
                        TopologyManager.releaseReadLock();
                    }
                }
            }
        });
    }

    /**
     * Get the active IP address of the member
     *
     * @param member Cartridge Cluster Member
     * @return public ip of the member if no lb, otherwise public ip of the member
     */
    private String getActivePublicIp(Member member) {
        String publicIp = null;
        if (member != null) {
            // when only one cartridge is present, get member's publicIP
            String lbClusterId = member.getLbClusterId();
            if (lbClusterId == null) {
                publicIp = member.getDefaultPublicIP();
            } else {
                // when cluster of cartridges is present, get publicIP of one lbmember of lbcluster
                List<Member> members = getLbClusterMembers(lbClusterId);
                if (members.size() > 0) {
                    publicIp = members.get(0).getDefaultPublicIP();
                }
            }
        }
        return publicIp;
    }

    /**
     * Get the list of cluster members
     *
     * @param lbClusterId load balancer cluster id
     * @return list of load balancer cluster members
     */
    private List<Member> getLbClusterMembers(String lbClusterId) {
        Topology topology = TopologyManager.getTopology();
        Collection<Service> serviceCollection = topology.getServices();
        if (serviceCollection != null) {
            for (Service service : serviceCollection) {
                if (service != null) {
                    Cluster cluster = service.getCluster(lbClusterId);
                    if (cluster != null) {
                        Collection<Member> members = cluster.getMembers();
                        return new ArrayList<Member>(members);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Save data in runtime database
     *
     * @param clusterId         cluster id of the cartridge
     * @param lbclusterId       cluster id of the load balancer
     * @param cartridgeActiveIp if lb is present, public ip of lb. if not public ip of the cartridge
     */
    private void persistCartridgeCluster(String clusterId, String lbclusterId, String cartridgeActiveIp)
            throws AppFactoryException {
        CartridgeCluster cartridgeCluster = applicationDao.getCartridgeClusterByClusterId(clusterId);
        if (cartridgeCluster != null) {
            cartridgeCluster.setLbClusterId(lbclusterId);
            cartridgeCluster.setActiveIP(cartridgeActiveIp);
            applicationDao.updateCartridgeCluster(cartridgeCluster);
        } else {
            cartridgeCluster = new CartridgeCluster();
            cartridgeCluster.setClusterId(clusterId);
            cartridgeCluster.setLbClusterId(lbclusterId);
            cartridgeCluster.setActiveIP(cartridgeActiveIp);
            applicationDao.addCartridgeCluster(cartridgeCluster);
        }
    }

}
