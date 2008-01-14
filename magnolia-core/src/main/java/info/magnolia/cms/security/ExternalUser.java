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

import info.magnolia.cms.security.auth.Entity;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.cms.i18n.MessagesManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.io.Serializable;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class wraps a user content object to provide some nice methods
 * @author philipp
 * @author Sameer Charles
 * @version $Revision:2558 $ ($Author:scharles $)
 */
public class ExternalUser extends AbstractUser implements Serializable {

    public static Logger log = LoggerFactory.getLogger(ExternalUser.class);

    /**
     * user properties
     */
    private Entity userDetails;

    /**
     * user roles
     */
    private RoleList roleList;

    /**
     * user groups
     */
    private GroupList groupList;

    /**
     * @param subject as created by login module
     */
    protected ExternalUser(Subject subject) {
        Set principalSet = subject.getPrincipals(Entity.class);
        Iterator entityIterator = principalSet.iterator();
        this.userDetails = (Entity) entityIterator.next();
        principalSet = subject.getPrincipals(RoleList.class);
        Iterator roleListIterator = principalSet.iterator();
        this.roleList = (RoleList) roleListIterator.next();
        principalSet = subject.getPrincipals(GroupList.class);
        Iterator groupListIterator = principalSet.iterator();
        this.groupList = (GroupList) groupListIterator.next();
    }

    public boolean hasRole(String roleName) {
        return this.roleList.has(roleName);
    }

    public void removeRole(String roleName) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    public void addRole(String roleName) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    /**
     * Is this user in a specified group?
     * @param groupName
     * @return true if in group
     */
    public boolean inGroup(String groupName) {
        return this.groupList.has(groupName);
    }

    /**
     * Remove a group. Implementation is optional
     * @param groupName
     */
    public void removeGroup(String groupName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    /**
     * Adds this user to a group. Implementation is optional
     * @param groupName
     */
    public void addGroup(String groupName) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    public boolean isEnabled() {
        return true;
    }

    public void setEnabled(boolean enabled) {
    }

    public String getLanguage() {
        String language = (String) this.userDetails.getProperty(Entity.LANGUAGE);
        if (null == language) {
              language = MessagesManager.getDefaultLocale().getLanguage();
        }
        return language;
    }

    public String getName() {
        return (String) this.userDetails.getProperty(Entity.NAME);
    }

    /**
     * get user password
     * @return password string
     */
    public String getPassword() {
        return (String) this.userDetails.getProperty(Entity.PASSWORD);
    }

    public String getProperty(String propertyName) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    public void setProperty(String propertyName, String value) {
        throw new UnsupportedOperationException("not implemented for this ExternalUser");
    }

    public Collection getGroups() {
        return this.groupList.getList();
    }

    public Collection getAllGroups() {
        return this.getGroups();
    }

    public Collection getRoles() {
        return this.roleList.getList();
    }

    public Collection getAllRoles() {
        return this.getRoles();
    }
}
