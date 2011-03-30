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
package info.magnolia.ui.admincentral.column;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.JCRUtil;
import info.magnolia.ui.framework.editor.Editor;

/**
 * UI component that displays a label and on double click opens it for editing by switching the label to save text field.
 *
 * @author tmattsson
 */
public abstract class AbstractEditable extends CustomComponent {

    /**
     * Presenter for AbstractEditable.
     */
    public interface Presenter {

        void edit(Item item, Editor editor) throws RepositoryException;

        boolean save(Item item) throws RepositoryException;

        void onClick(Item item) throws RepositoryException;
    }

    private final String workspace;
    private final String nodeIdentifier;
    private final String propertyName;
    private HorizontalLayout layout;
    private Presenter presenter;

    protected AbstractEditable(Item item, Presenter presenter) throws RepositoryException {

        this.presenter = presenter;

        this.workspace = item.getSession().getWorkspace().getName();
        this.nodeIdentifier = item instanceof Node ? ((Node) item).getIdentifier() : item.getParent().getIdentifier();
        this.propertyName = item instanceof Property ? (item).getName() : null;

        this.layout = new HorizontalLayout();

        // TODO the double click event should be removed when the text field is visible, otherwise its not possible to double click to mark words
        layout.addListener(new LayoutEvents.LayoutClickListener() {

            public void layoutClick(final LayoutEvents.LayoutClickEvent event) {
                if (event.isDoubleClick()) {
                    try {
                        Item item = getItem();
                        Editor editor = getComponentAndEditor(item);
                        AbstractEditable.this.presenter.edit(item, editor);
                        layout.removeAllComponents();
                        layout.addComponent((Component) editor);
                    } catch (RepositoryException e) {
                        throw new RuntimeRepositoryException(e);
                    }
                } else {
                    try {
                        AbstractEditable.this.presenter.onClick(getItem());
                    } catch (RepositoryException e) {
                        throw new RuntimeRepositoryException(e);
                    }
                }
            }
        });
        layout.addComponent(new Label(getLabelText(item)));
        layout.setSizeUndefined();
        setCompositionRoot(layout);
        setSizeUndefined();
    }

    protected void onCancel() {
        try {
            layout.removeAllComponents();
            layout.addComponent(new Label(getLabelText(getItem())));
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    protected void onSave() {
        try {
            Item item = getItem();
            if (presenter.save(item)) {
                layout.removeAllComponents();
                layout.addComponent(new Label(getLabelText(getItem())));
            }
        } catch (RepositoryException e) {
            throw new RuntimeRepositoryException(e);
        }
    }

    protected abstract String getLabelText(Item item) throws RepositoryException;

    protected abstract Editor getComponentAndEditor(Item item) throws RepositoryException;

    private Item getItem() throws RepositoryException {
        Node node = JCRUtil.getSession(this.workspace).getNodeByIdentifier(this.nodeIdentifier);
        if (propertyName != null)
            return node.getProperty(propertyName);
        return node;
    }
}