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
package info.magnolia.module.admincentral.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Definition of a tree.
 */
public class TreeDefinition implements Serializable {

    private static final long serialVersionUID = -4220598066437699852L;

    private String name;
    private String repository;
    private String path;

    /**
     * When in flat mode the tree behaves like a simple table and nodes cannot be expanded to show their children. Used
     * in the security trees.
     */
    private boolean flatMode;

    private List<TreeColumn> columns = new ArrayList<TreeColumn>();
    private List<MenuItem> functionMenu = new ArrayList<MenuItem>();
    private List<MenuItem> contextMenu = new ArrayList<MenuItem>();
    private List<TreeItemType> itemTypes = new ArrayList<TreeItemType>();

    public boolean isFlatMode() {
        return flatMode;
    }

    public List<TreeItemType> getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(List<TreeItemType> itemTypes) {
        this.itemTypes = itemTypes;
    }

    public boolean addItemType(TreeItemType treeItemType) {
        return itemTypes.add(treeItemType);
    }

    public void setFlatMode(boolean flatMode) {
        this.flatMode = flatMode;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TreeColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<TreeColumn> columns) {
        this.columns = columns;
    }

    public boolean addColumn(TreeColumn treeColumn) {
        return columns.add(treeColumn);
    }

    public List<MenuItem> getFunctionMenu() {
        return functionMenu;
    }

    public void setFunctionMenu(List<MenuItem> functionMenu) {
        this.functionMenu = functionMenu;
    }

    public List<MenuItem> getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(List<MenuItem> contextMenu) {
        this.contextMenu = contextMenu;
    }
}