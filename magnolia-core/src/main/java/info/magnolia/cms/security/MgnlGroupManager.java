/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;


/**
 * @author Sameer Charles $Id$
 */
public class MgnlGroupManager implements GroupManager {
    private static final Logger log = LoggerFactory.getLogger(MgnlGroupManager.class);

    /**
     * Create a group
     * @param name
     * @return newly created group
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if logged in repository user does not sufficient rights
     */
    public Group createGroup(String name) throws UnsupportedOperationException, AccessDeniedException {
        try {
            Content node = getHierarchyManager().createContent("/", name, ItemType.GROUP.getSystemName());
            getHierarchyManager().save();
            return new MgnlGroup(node);
        }
        catch (Exception e) {
            log.error("can't create group [" + name + "]", e);
            return null;
        }
    }

    /**
     * Get group by the given name
     * @param name
     * @return group
     * @throws UnsupportedOperationException if the implementation does not support writing
     * @throws AccessDeniedException if logged in repository user does not sufficient rights
     */
    public Group getGroup(String name) throws UnsupportedOperationException, AccessDeniedException {
        try {
            return new MgnlGroup(getHierarchyManager().getContent(name));
        } catch (PathNotFoundException e) {
            // this is not an error, once we have MAGNOLIA-1757 implemented we can change this.
            log.warn("can't find group [" + name + "] in magnolia");
            log.debug("can't find group [" + name + "] in magnolia", e);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Throwable e) {
            log.error("Exception while retrieving group", e);
        }
        return null;
    }

    /**
     * All groups
     */
    public Collection getAllGroups() {
        Collection groups = new ArrayList();
        try {
            Collection nodes = getHierarchyManager().getRoot().getChildren(ItemType.GROUP);
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                groups.add(new MgnlGroup((Content) iter.next()));
            }
        }
        catch (Exception e) {
            log.error("can't find user");
        }
        return groups;
    }

    /**
     * return the groups HierarchyManager (through the system context)
     */
    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getSystemContext().getHierarchyManager(ContentRepository.USER_GROUPS);
    }
}
