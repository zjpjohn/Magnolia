/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.templatingcomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract base class for components that operate on a specified piece of content.
 * 
 * @version $Id$
 */
public abstract class AbstractContentComponent extends AbstractAuthoringUiComponent {

    protected static final String LINEBREAK = "\r\n";

    // TODO should also support a JSP ContentMap
    private Node content;
    private String workspace;
    private String uuid;
    private String path;

    public AbstractContentComponent(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    protected String getNodePath(Node content) throws RepositoryException {
        return content.getSession().getWorkspace().getName() + ":" + content.getPath();
    }

    protected Node getTargetContent() throws RepositoryException {

        // TODO should we default workspace to 'website' ?
        // TODO should we be strict and fail on invalid combinations ?

        // TODO we can safely keep the node around after we've resolved it

        if (content != null) {
            return content;
        }
        if (StringUtils.isNotEmpty(workspace)) {
            if (StringUtils.isNotEmpty(uuid)) {
                return MgnlContext.getJCRSession(workspace).getNodeByIdentifier(uuid);
            }
            if (StringUtils.isNotEmpty(path)) {
                return MgnlContext.getJCRSession(workspace).getNodeByIdentifier(path);
            }
            throw new IllegalArgumentException("Need to specify either uuid or path in combination with workspace");
        }

        // TODO this default might not be suitable for render and paragraph, why would they render the current content again by default?

        return currentContent();
    }

    public Node getContent() {
        return content;
    }

    public void setContent(Node content) {
        this.content = content;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
