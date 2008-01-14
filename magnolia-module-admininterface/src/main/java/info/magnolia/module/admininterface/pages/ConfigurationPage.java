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
package info.magnolia.module.admininterface.pages;

import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.ShutdownManager;
import info.magnolia.cms.beans.config.VirtualURIManager;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.module.admininterface.TemplatedMVCHandler;

import java.util.List;
import java.util.Map;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class ConfigurationPage extends TemplatedMVCHandler {

    /**
     * Required constructor.
     * @param name page name
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     */
    public ConfigurationPage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
    }

    public Server getServer() {
        return Server.getInstance();
    }

    public String getDefaultMailServer() {
        return Server.getDefaultMailServer();
    }

    public String getServerId() {
        return Server.getServerId();
    }

    public Collection getVirtualUriMappings() {
        return VirtualURIManager.getInstance().getURIMappings();
    }

    public List getShutdownTasks() {
        return ShutdownManager.listShutdownTasks();
    }

    public Map getSystemProperties() {
        return SystemProperty.getProperties();
    }

}
