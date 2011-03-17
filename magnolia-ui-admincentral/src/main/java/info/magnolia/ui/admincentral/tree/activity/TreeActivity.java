/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.admincentral.tree.activity;

import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.admincentral.editworkspace.event.ContentChangedEvent;
import info.magnolia.ui.admincentral.editworkspace.event.ContentChangedEvent.Handler;
import info.magnolia.ui.admincentral.editworkspace.place.ItemSelectedPlace;
import info.magnolia.ui.admincentral.tree.action.EditWorkspaceActionFactory;
import info.magnolia.ui.admincentral.tree.builder.TreeBuilder;
import info.magnolia.ui.admincentral.tree.view.TreeView;
import info.magnolia.ui.admincentral.tree.view.TreeViewImpl;
import info.magnolia.ui.framework.activity.AbstractActivity;
import info.magnolia.ui.framework.event.EventBus;
import info.magnolia.ui.framework.place.PlaceController;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.ViewPort;
import info.magnolia.ui.model.UIModel;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

/**
 * Activity for displaying dialogs.
 *
 * @author tmattsson
 */
public class TreeActivity extends AbstractActivity implements TreeView.Presenter, Handler {

    private final String treeName;
    private PlaceController placeController;
    private TreeView treeView;
    private UIModel uiModel;
    private String path;
    private TreeBuilder builder;
    private ComponentProvider componentProvider;
    private Shell shell;

    public TreeActivity(String treeName, String path, PlaceController placeController, UIModel uiModel, TreeBuilder builder, ComponentProvider componentProvider, Shell shell) {
        this.uiModel = uiModel;
        this.treeName = treeName;
        this.path = path;
        this.placeController = placeController;
        this.builder = builder;
        this.componentProvider = componentProvider;
        this.shell = shell;
    }

    // TODO is this good practice?
    public void update(String path){
        if(!this.path.equals(path)){
            this.path = path;
            treeView.select(path);
        }
    }

    public void start(ViewPort viewPort, EventBus eventBus) {
        try {
            this.treeView = new TreeViewImpl(treeName, this, uiModel, builder, new EditWorkspaceActionFactory(componentProvider), shell);
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
        treeView.select(path);
        eventBus.addHandler(ContentChangedEvent.class, this);
        viewPort.setView(treeView);
    }

    public UIModel getUIModel() {
        return uiModel;
    }

    public void onItemSelection(Item jcrItem) {
        try {
            this.path = uiModel.getPathInTree(treeName, jcrItem);
            placeController.goTo(new ItemSelectedPlace(treeName, this.path));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    public void onContentChanged(ContentChangedEvent event) {
        // FIXME only if we are not the source!
        treeView.refresh();
    }

}
