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
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import static com.salesforce.dva.argus.system.SystemAssert.requireArgument;

/**
 * Barrier class defined for a root path, waits on a specified number of nodes to join the barrier. This is needed to achieve synchronization before
 * the nodes can do a job.
 *
 * @author  Dilip Devaraj (ddevaraj@salesforce.com)
 */
public class Barrier implements Watcher {

    //~ Static fields/initializers *******************************************************************************************************************

    private static Logger _logger = LoggerFactory.getLogger(Barrier.class);
    private static ZooKeeper zooKeeper = null;
    private static Integer mutex;

    //~ Instance fields ******************************************************************************************************************************

    private String rootPath;
    private int size;

    // Node name inside barrier
    private String name;

    //~ Constructors *********************************************************************************************************************************

    /**
     * Barrier constructor.
     *
     * @param  url       The zookeeper cluster to connect to. Cannot be null.
     * @param  rootPath  The barrier root path under which client ephemeral nodes are created
     * @param  size      The maximum number of nodes to join barrier before all of them can proceed
     */
    public Barrier(String url, String rootPath, int size) {
        requireArgument(url != null, "Zookeeper cluster url cannot be null.");
        requireArgument(rootPath != null, "Root path cannot be null.");
        this.rootPath = rootPath;
        this.size = size;
        if (zooKeeper == null) {
            try {
                _logger.debug("Starting ZooKeeper:");
                zooKeeper = new ZooKeeper(url, 3000, this);
                mutex = new Integer(-1);
                _logger.debug("Finished starting ZooKeeper: " + zooKeeper);
            } catch (IOException e) {
                _logger.error(e.toString());
                zooKeeper = null;
            }
        }
        if (zooKeeper != null) {
            try {
                Stat s = zooKeeper.exists(rootPath, false);

                if (s == null) {
                    zooKeeper.create(rootPath, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (KeeperException e) {
                _logger.error("Keeper exception when instantiating barrier: {} ", e.toString());
            } catch (InterruptedException e) {
                _logger.error("Interrupted exception");
            }
        }
        try {
            name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString() + System.currentTimeMillis());
        } catch (UnknownHostException e) {
            _logger.error(e.toString());
        }
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  zooKeeper  DOCUMENT ME!
     */
    public static void setZookeeper(ZooKeeper zooKeeper) {
        _logger.info("Resetting zookeeper client context");
        Barrier.zooKeeper = zooKeeper;
    }

    //~ Methods **************************************************************************************************************************************

    /**
     * Wait until required number of nodes join barrier.
     *
     * @return  true when required number of nodes have entered barrier, else wait
     *
     * @throws  KeeperException       DOCUMENT ME!
     * @throws  InterruptedException  DOCUMENT ME!
     */
    public boolean enter() throws KeeperException, InterruptedException {
        zooKeeper.create(rootPath + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        while (true) {
            synchronized (mutex) {
                List<String> list = zooKeeper.getChildren(rootPath, true);

                if (list.size() < size) {
                    mutex.wait();
                } else {
                    return true;
                }
            }
        }
    }

    /**
     * Wait until all nodes leave barrier.
     *
     * @return  true when required all nodes have left barrier, else wait.
     *
     * @throws  KeeperException       DOCUMENT ME!
     * @throws  InterruptedException  DOCUMENT ME!
     */
    public boolean leave() throws KeeperException, InterruptedException {
        zooKeeper.delete(rootPath + "/" + name, 0);
        while (true) {
            synchronized (mutex) {
                List<String> list = zooKeeper.getChildren(rootPath, true);

                if (list.size() > 0) {
                    mutex.wait();
                } else {
                    return true;
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  event  DOCUMENT ME!
     */
    public synchronized void process(WatchedEvent event) {
        synchronized (mutex) {
            _logger.debug("Process:{}", event.getType());
            mutex.notify();
        }
    }
}
/* Copyright (c) 2015-2016, Salesforce.com, Inc.  All rights reserved. */
