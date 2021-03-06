/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.cms.i18n;

import javax.jcr.RepositoryException;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentWrapper;


/**
 * A ContentWrapper implementation which knows about I18nContentSupport.
 * @see I18nContentSupport
 * @see info.magnolia.cms.util.ContentWrapper
 *
 * @author pbracher
 * @version $Id$
 */
public class I18nContentWrapper extends ContentWrapper {

    public I18nContentWrapper(Content node) {
        super(node);
    }

    @Override
    public NodeData newNodeDataInstance(String name, int type, boolean createIfNotExisting) throws AccessDeniedException, RepositoryException {
        final I18nContentSupport i18nSupport = I18nContentSupportFactory.getI18nSupport();
        NodeData nodeData = i18nSupport.getNodeData(getWrappedContent(), name);
        if(nodeData.isExist()){
            return nodeData;
        }
        // nothing we can do
        return super.newNodeDataInstance(name, type, createIfNotExisting);
    }

    @Override
    protected Content wrap(Content node) {
        // be sure we don't wrap nulls
        if (node == null) {
            return null;
        }
        return new I18nContentWrapper(node);
    }

}
