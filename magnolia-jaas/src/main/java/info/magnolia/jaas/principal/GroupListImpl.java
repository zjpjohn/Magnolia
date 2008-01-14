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
package info.magnolia.jaas.principal;

import info.magnolia.cms.security.auth.GroupList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * @author Sameer Charles $Id$
 */
public class GroupListImpl implements GroupList {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * default name for this principal
     */
    private static final String DEFAULT_NAME = "groups";

    private String name;

    /**
     * list of names
     */
    protected Collection list;

    public GroupListImpl() {
        this.list = new ArrayList();
    }

    /**
     * Get name given to this principal
     * @return name
     */
    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            return getDefaultName();
        }
        return this.name;
    }

    protected String getDefaultName() {
        return DEFAULT_NAME;
    }

    /**
     * Set principal name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Add a name to the list
     * @param name
     */
    public void add(String name) {
        this.list.add(name);
    }

    /**
     * Gets list of roles as string
     * @return roles
     */
    public Collection getList() {
        return this.list;
    }

    /**
     * Checks if the name exist in this list
     * @param name
     */
    public boolean has(String name) {
        Iterator listIterator = this.list.iterator();
        while (listIterator.hasNext()) {
            String roleName = (String) listIterator.next();
            if (StringUtils.equalsIgnoreCase(name, roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", getName()).append("list", this.list).toString();
    }
}
