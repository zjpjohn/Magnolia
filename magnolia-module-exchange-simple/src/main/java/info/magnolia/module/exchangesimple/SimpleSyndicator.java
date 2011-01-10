/**
 * This file Copyright (c) 2003-2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.ActivationManagerFactory;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.exchange.Subscriber;
import info.magnolia.cms.exchange.Subscription;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import EDU.oswego.cs.dl.util.concurrent.CountDown;
import EDU.oswego.cs.dl.util.concurrent.Sync;


/**
 * Implementation of syndicator that simply sends all activated content over http connection specified in the subscriber.
 * @author Sameer Charles $Id$
 */
public class SimpleSyndicator extends BaseSyndicatorImpl {
    private static final Logger log = LoggerFactory.getLogger(SimpleSyndicator.class);

    public SimpleSyndicator() {
    }

    @Override
    public void activate(final ActivationContent activationContent, String nodePath) throws ExchangeException {
        String nodeUUID = activationContent.getproperty(NODE_UUID);
        Collection<Subscriber> subscribers = ActivationManagerFactory.getActivationManager().getSubscribers();
        Iterator<Subscriber> subscriberIterator = subscribers.iterator();
        final Sync done = new CountDown(subscribers.size());
        final Map<Subscriber, Exception> errors = new ConcurrentHashMap<Subscriber, Exception>(subscribers.size());
        while (subscriberIterator.hasNext()) {
            final Subscriber subscriber = subscriberIterator.next();
            if (subscriber.isActive()) {
                // Create runnable task for each subscriber execute
                if (Boolean.parseBoolean(activationContent.getproperty(ItemType.DELETED_NODE_MIXIN))) {
                    executeInPool(getDeactivateTask(done, errors, subscriber, nodeUUID, nodePath));
                } else {
                    executeInPool(getActivateTask(activationContent, done, errors, subscriber, nodePath));
                }
            } else {
                // count down directly
                done.release();
            }
        } //end of subscriber loop

        // wait until all tasks are executed before returning back to user to make sure errors can be propagated back to the user.
        acquireIgnoringInterruption(done);

        // collect all the errors and send them back.
        if (!errors.isEmpty()) {
            Exception e = null;
            StringBuffer msg = new StringBuffer(errors.size() + " error").append(errors.size() > 1 ? "s" : "").append(" detected: ");
            Iterator<Map.Entry<Subscriber, Exception>> iter = errors.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Subscriber, Exception> entry = iter.next();
                e = entry.getValue();
                Subscriber subscriber = entry.getKey();
                msg.append("\n").append(e.getMessage()).append(" on ").append(subscriber.getName());
                log.error(e.getMessage(), e);
            }

            throw new ExchangeException(msg.toString(), e);
        }

        executeInPool(new Runnable() {
            public void run() {
                cleanTemporaryStore(activationContent);
            }
        });
    }

    private Runnable getActivateTask(final ActivationContent activationContent, final Sync done, final Map<Subscriber, Exception> errors, final Subscriber subscriber, final String nodePath) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    activate(subscriber, activationContent, nodePath);
                } catch (Exception e) {
                    log.error("Failed to activate content.", e);
                    errors.put(subscriber,e);
                } finally {
                    done.release();
                }
            }
        };
        return r;
    }

    @Override
    public void doDeactivate(String nodeUUID, String nodePath) throws ExchangeException {
        Collection<Subscriber> subscribers = ActivationManagerFactory.getActivationManager().getSubscribers();
        Iterator<Subscriber> subscriberIterator = subscribers.iterator();
        final Sync done = new CountDown(subscribers.size());
        final Map<Subscriber, Exception> errors = new ConcurrentHashMap<Subscriber, Exception>();
        while (subscriberIterator.hasNext()) {
            final Subscriber subscriber = subscriberIterator.next();
            if (subscriber.isActive()) {
                // Create runnable task for each subscriber.
                executeInPool(getDeactivateTask(done, errors, subscriber, nodeUUID, nodePath));
            } else {
                // count down directly
                done.release();
            }
        } //end of subscriber loop

        // wait until all tasks are executed before returning back to user to make sure errors can be propagated back to the user.
        acquireIgnoringInterruption(done);

        // collect all the errors and send them back.
        if (!errors.isEmpty()) {
            Exception e = null;
            StringBuffer msg = new StringBuffer(errors.size() + " error").append(
                    errors.size() > 1 ? "s" : "").append(" detected: ");
            Iterator<Entry<Subscriber, Exception>> iter = errors.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Subscriber, Exception> entry = iter.next();
                e = entry.getValue();
                Subscriber subscriber = entry.getKey();
                msg.append("\n").append(e.getMessage()).append(" on ").append(subscriber.getName());
                log.error(e.getMessage(), e);
            }

            throw new ExchangeException(msg.toString(), e);
        }
    }

    private Runnable getDeactivateTask(final Sync done, final Map<Subscriber, Exception> errors, final Subscriber subscriber, final String nodeUUID, final String nodePath) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    doDeactivate(subscriber, nodeUUID, nodePath);
                } catch (Exception e) {
                    log.error("Failed to deactivate content.", e);
                    errors.put(subscriber,e);
                } finally {
                    done.release();
                }
            }
        };
        return r;
    }

    /**
     * Deactivate from a specified subscriber.
     * @param subscriber
     * @throws ExchangeException
     */
    @Override
    public String doDeactivate(Subscriber subscriber, String nodeUUID, String path) throws ExchangeException {
        Subscription subscription = subscriber.getMatchedSubscription(path, this.repositoryName);
        if (null != subscription) {
            String urlString = getDeactivationURL(subscriber);
            try {
                URLConnection urlConnection = prepareConnection(subscriber, urlString);

                this.addDeactivationHeaders(urlConnection, nodeUUID);
                String status = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_STATUS);

                // check if the activation failed
                if (StringUtils.equals(status, ACTIVATION_FAILED)) {
                    String message = urlConnection.getHeaderField(ACTIVATION_ATTRIBUTE_MESSAGE);
                    throw new ExchangeException("Message received from subscriber: " + message);
                }

                urlConnection.getContent();

            }
            catch (MalformedURLException e) {
                throw new ExchangeException("Incorrect URL for subscriber " + subscriber + "[" + urlString + "]");
            }
            catch (IOException e) {
                throw new ExchangeException("Not able to send the deactivation request [" + urlString + "]: " + e.getMessage());
            }
            catch (Exception e) {
                throw new ExchangeException(e);
            }
        }
        return null;
    }

}
