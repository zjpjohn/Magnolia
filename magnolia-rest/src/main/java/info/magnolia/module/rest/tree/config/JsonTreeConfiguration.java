/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.module.rest.tree.config;

import info.magnolia.cms.i18n.Messages;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a tree view.
 */
@XmlRootElement
public class JsonTreeConfiguration extends TreeConfigurationItem {

    private List<JsonTreeColumn> columns = new ArrayList<JsonTreeColumn>();
    private List<JsonMenuItem> contextMenuItems = new ArrayList<JsonMenuItem>();
    private List<JsonMenuItem> functionMenuItems = new ArrayList<JsonMenuItem>();
    private boolean flatMode;

    public int getColumnsWidth() {
        int n = 0;
        for (JsonTreeColumn column : columns) {
            n += column.getWidth();
        }
        return n;
    }

    public void addColumn(JsonTreeColumn column) {
        this.columns.add(column);
    }

    public List<JsonTreeColumn> getColumns() {
        return columns;
    }

    public void setFlatMode(boolean flatMode) {
        this.flatMode = flatMode;
    }

    public boolean isFlatMode() {
        return flatMode;
    }

    public List<JsonMenuItem> getContextMenuItems() {
        return contextMenuItems;
    }

    public List<JsonMenuItem> getFunctionMenuItems() {
        return functionMenuItems;
    }

    public void addContextMenuItem(JsonMenuItem menuItem) {
        this.contextMenuItems.add(menuItem);
    }

    public void addFunctionMenuItem(JsonMenuItem menuItem) {
        this.functionMenuItems.add(menuItem);
    }

    public void initMessages(Messages parentMessages) {
        super.initMessages(parentMessages);
        for (JsonTreeColumn column : columns) {
            column.initMessages(getMessages());
        }
        for (JsonMenuItem menuItem : contextMenuItems) {
            menuItem.initMessages(getMessages());
        }
        for (JsonMenuItem menuItem : functionMenuItems) {
            menuItem.initMessages(getMessages());
        }
    }
}