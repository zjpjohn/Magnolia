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

import java.io.IOException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;

/**
 * Renders a paragraph and outputs the associated edit bar.
 *
 * @version $Id$
 */
public class ParagraphMarker extends AbstractContentComponent {

    private boolean editable;

    public ParagraphMarker(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    @Override
    protected void doRender(Appendable out) throws IOException, RepositoryException {
        Node content = getTargetContent();
        out.append("<!-- cms:begin cms:content=\"" + getNodePath(content) + "\" -->").append(LINEBREAK);

        // TODO there's no way of configuring bar vs button here
        out.append("<cms:edit content=\"" + getNodePath(content) + "\"").append(">").append(LINEBREAK);

        // TODO render the target content
        // TODO not sure how to pass editable
    }

    @Override
    public void postRender(Appendable out) throws IOException, RepositoryException {
        Node content = getTargetContent();
        out.append("<!-- cms:end cms:content=\"" + getNodePath(content) + "\" -->").append(LINEBREAK);
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}