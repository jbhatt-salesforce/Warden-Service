/* Copyright (c) 2015-2016, Salesforce.com, Inc.
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *   
 *      Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 *      Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *      Neither the name of Salesforce.com nor the names of its contributors may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
package com.salesforce.dva.argus.util.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;

/**
 * Create an election root node, under which client nodes are created. Each client node participates in election to determine its index position, and
 * total number of peers.
 *
 * @author  Dilip Devaraj (ddevaraj@salesforce.com)
 */
public class ClientNode implements Runnable {

    //~ Static fields/initializers *******************************************************************************************************************

    private static final String CLIENT_NODE_PREFIX = "/c_";

    //~ Instance fields ******************************************************************************************************************************

    private Logger _logger = LoggerFactory.getLogger(ClientNode.class);
    private ZooKeeper zooKeeper;
    private final String electionRootPath;
    private String clientNodePath;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Creates a new ClientNode object.
     *
     * @param   zookeeperURL      The zookeeper cluster to connect to. Cannot be null.
     * @param   electionRootPath  The root path under which client ephemeral nodes are created
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public ClientNode(final String zookeeperURL, String electionRootPath) throws IOException {
        requireArgument(zookeeperURL != null, "Zookeeper cluster url cannot be null.");
        requireArgument(electionRootPath != null, "Election root path cannot be null.");
        if (zooKeeper == null) {
            try {
                _logger.debug("Starting ZooKeeper:");
                zooKeeper = new ZooKeeper(zookeeperURL, 3000, new ClientNodeWatcher());
                _logger.debug("Finished starting ZooKeeper: " + zooKeeper);
            } catch (IOException e) {
                _logger.error(e.toString());
                zooKeeper = null;
            }
        }
        this.electionRootPath = electionRootPath;
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * Gets node index and its peer count.
     *
     * @return  DOCUMENT ME!
     */
    public ClientsResult calculateAllNodesResult() {
        final List<String> childNodePaths = getChildren(electionRootPath, false);

        _logger.info("Total peers = {} ", childNodePaths.size());
        Collections.sort(childNodePaths);

        int index = childNodePaths.indexOf(clientNodePath.substring(clientNodePath.lastIndexOf('/') + 1));

        return new ClientsResult(index, childNodePaths.size());
    }

    /**
     * Create a zookeeper node.
     *
     * @param   path       The path of znode to create
     * @param   watch      Whether to watch this node or not
     * @param   ephimeral  Create ephemeral or permanent node
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public String createNode(final String path, final boolean watch, final boolean ephimeral) {
        String createdNodePath = null;

        try {
            final Stat nodeStat = zooKeeper.exists(path, watch);

            if (nodeStat == null) {
                createdNodePath = zooKeeper.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE,
                    (ephimeral ? CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.PERSISTENT));
            } else {
                createdNodePath = path;
            }
        } catch (KeeperException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return createdNodePath;
    }

    /**
     * Gets list of children for a znode.
     *
     * @param   path   The path of the znode
     * @param   watch  Whether to watch this node or not
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public List<String> getChildren(final String path, final boolean watch) {
        List<String> childNodes = null;

        try {
            childNodes = zooKeeper.getChildren(path, watch);
        } catch (KeeperException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return childNodes;
    }

    @Override
    public void run() {
        final String rootNodePath = createNode(electionRootPath, false, false);

        if (rootNodePath == null) {
            throw new IllegalStateException("Unable to create/access election root node with path: " + electionRootPath);
        }
        clientNodePath = createNode(rootNodePath + CLIENT_NODE_PREFIX, false, true);
        if (clientNodePath == null) {
            throw new IllegalStateException("Unable to create/access client node with path: " + electionRootPath);
        }
        _logger.info("Client node created with path: {}", clientNodePath);
    }

    //~ Inner Classes ********************************************************************************************************************************

    /**
     * Subscribes to events from zookeeper server. On session expiry, close the current zookeeper client object. If number of peers change recalculate
     * client results
     *
     * @author  Dilip Devaraj (ddevaraj@salesforce.com)
     */
    public class ClientNodeWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            _logger.debug("Client, event received:{}", event);

            final EventType eventType = event.getType();

            if (event.getState() == Watcher.Event.KeeperState.Expired) {
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    _logger.error("Exception in closing expired zookeeper client: {}", e);
                }
            }
            if (EventType.NodeChildrenChanged.equals(eventType)) {
                _logger.debug("Node children changed");
                calculateAllNodesResult();
            }
        }
    }

    /**
     * ClientsResult holds the positional index after election, and the number of peers.
     *
     * @author  Dilip Devaraj (ddevaraj@salesforce.com)
     */
    public final class ClientsResult {

        private final int nodePosition;
        private final int numberOfPeers;

        /**
         * Creates a new ClientsResult object.
         *
         * @param  nodePosition   DOCUMENT ME!
         * @param  numberOfPeers  DOCUMENT ME!
         */
        public ClientsResult(final int nodePosition, final int numberOfPeers) {
            this.nodePosition = nodePosition;
            this.numberOfPeers = numberOfPeers;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getNodePosition() {
            return nodePosition;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getNumberOfPeers() {
            return numberOfPeers;
        }
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
