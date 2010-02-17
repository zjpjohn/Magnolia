/**
 * This file Copyright (c) 2010 Magnolia International
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
package info.magnolia.templatinguicomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.security.Permission;
import info.magnolia.templatinguicomponents.AuthoringUiComponent;

import java.io.IOException;

/**
 * If no target node is set explicitly, it is deduced using {@link #defaultTarget()}.
 * Implementions should expose setter for their specific parameters. No getters needed.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractAuthoringUiComponent implements AuthoringUiComponent {
    private final ServerConfiguration server;
    private final AggregationState aggregationState;

    /** Currently no need for generic label/description
     private String label;
     private String description;
     */
    private Content target;

    protected AbstractAuthoringUiComponent(final ServerConfiguration server, final AggregationState aggregationState) {
        this.server = server;
        this.aggregationState = aggregationState;
        this.target = defaultTarget();
    }

    /** Currently no need for generic label/description
     protected String getLabel() {
     return label;
     }

     public void setLabel(String label) {
     this.label = label;
     }

     protected String getDescription() {
     return description;
     }

     public void setDescription(String description) {
     this.description = description;
     }
     */

    protected Content getTarget() {
        return target;
    }

    public void setTarget(Content target) {
        this.target = target;
    }

    public void render(Appendable out) throws IOException {
        if (!shouldRender()) {
            return;
        }
        doRender(out);
    }

    protected abstract void doRender(Appendable out) throws IOException;

    /**
     * Returns the "current content" from the aggregation state.
     * Override this method if your component needs a different target node.
     */
    protected Content defaultTarget() {
        final Content currentContent = aggregationState.getCurrentContent();
        if (currentContent == null) {
            throw new IllegalStateException("Could not determine defaultTarget from AggregationState, currentContent is null");
        }
        return currentContent;
    }

    /**
     * Override this method if the component needs to be rendered under different conditions.
     */
    protected boolean shouldRender() {

//   TODO     if ((!adminOnly || ? with jsp tag EditBar, you can do adminOnly="false" and get the button/bar on public instance !?
        return (server.isAdmin() && aggregationState.getMainContent().isGranted(Permission.SET));
    }
}