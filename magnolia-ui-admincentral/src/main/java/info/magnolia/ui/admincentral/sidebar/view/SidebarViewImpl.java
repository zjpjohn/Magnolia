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
package info.magnolia.ui.admincentral.sidebar.view;

import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.lazyloadwrapper.LazyLoadWrapper;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalSplitPanel;

/**
 * The sidebar view showing the list of available actions and a preview of the selected item.
 *
 * @author fgrilli
 * @author tmattsson
 */
public class SidebarViewImpl implements IsVaadinComponent, SidebarView {

    private static final Logger log = LoggerFactory.getLogger(SidebarViewImpl.class);
    private VerticalSplitPanel panel;
    private ActionListView actionListView;
    private PreviewView previewView;
    private Presenter presenter;

    public SidebarViewImpl(ActionListView actionListView, PreviewView previewView) {
        this.actionListView = actionListView;
        this.previewView = previewView;
        panel = new VerticalSplitPanel();
        panel.setSizeFull();

        panel.setFirstComponent(actionListView.asVaadinComponent());
        LazyLoadWrapper lazyPreview = new LazyLoadWrapper(previewView.asVaadinComponent());
        panel.setSecondComponent(lazyPreview);
    }

    public ActionListView getActionList() {
        return actionListView;
    }

    public PreviewView getPreviewView() {
        return previewView;
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
        actionListView.setPresenter(this.presenter);
    }

    public Component asVaadinComponent() {
        return panel;
    }
}
