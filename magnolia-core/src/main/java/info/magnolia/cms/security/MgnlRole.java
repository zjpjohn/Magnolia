/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.security;

import info.magnolia.cms.security.auth.ACL;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wraps a role jcr-node.
 * @version $Revision$ ($Author$)
 */
public class MgnlRole implements Role {
    private static final Logger log = LoggerFactory.getLogger(MgnlRole.class);

    public static long PERMISSION_ANY = -1;

    private final String roleName;

    private final String roleId;

    private final Collection<ACL> acls;

    protected MgnlRole(String name ,String roleId , Collection<ACL> acls) {
        this.roleName = name;
        this.roleId = roleId;
        this.acls = acls;
    }

    @Override
    public String getName() {
        return roleName;
    }

    @Override
    public void addPermission(String repository, String path, long permission) {
        throw new UnsupportedOperationException("Use RoleManager.addPermission() instead.");
    }

    @Override
    public void removePermission(String repository, String path) {
        this.removePermission(repository, path, MgnlRole.PERMISSION_ANY);
    }

    @Override
    public void removePermission(String repository, String path, long permission) {
        throw new UnsupportedOperationException("Use RoleManager.removePermission() instead.");
    }

    @Override
    public String getId() {
        return roleId;
    }
}
