/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.admincentral.tree;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.module.admincentral.jcr.JCRMetadataUtil;

/**
 * Column that displays a property for a nodes MetaData. Used to display the modification date of
 * content nodes.
 */
public class MetaDataColumn extends TreeColumn<String> implements Serializable {

    private static final long serialVersionUID = -2788490588550009503L;

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public Object getValue(Item item) throws RepositoryException {
        if (item instanceof Node) {
            Node node = (Node) item;
            Calendar date = JCRMetadataUtil.getMetaData(node).getCreationDate();
            return date != null ? new SimpleDateFormat("yy-MM-dd, HH:mm").format(date.getTime()) : "";
        }
        return "";
    }
}
