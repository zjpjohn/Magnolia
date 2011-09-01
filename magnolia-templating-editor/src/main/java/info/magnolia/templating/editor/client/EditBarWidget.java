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
package info.magnolia.templating.editor.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

/**
 * Edit bar.
 */
public class EditBarWidget extends AbstractBarWidget {

    private PageEditor pageEditor;

    private String workspace;
    private String path;

    private String componentId; // name of the component, needed for drag n drop
    private String label;
    private String dialog;
    private String format; // bar or button (its likely too late to make a decision here)

    public EditBarWidget(AbstractBarWidget parentBar, final PageEditor pageEditor, Element element) {
        super(parentBar, "rgb(116, 173, 59)");
        this.pageEditor = pageEditor;

        String content = element.getAttribute("content");
        int i = content.indexOf(':');
        this.workspace = content.substring(0, i);
        this.path = content.substring(i + 1);

        this.componentId = element.getAttribute("template");
        this.label = element.getAttribute("label");
        this.dialog = element.getAttribute("dialog");
        this.format = element.getAttribute("format");

        setLabelText(label + "(" + componentId + ")");
        Button button = new Button("Edit&nbsp;component");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pageEditor.openDialog(dialog, workspace, path, null, null);
            }
        });
        addButton(button);
    }

    @Override
    protected void onSelect() {
        super.onSelect();
        pageEditor.updateSelection(this, PageEditor.SELECTION_TYPE_COMPONENT_IN_LIST, workspace, path, null, null, getComponentsAvailableInThisArea(), dialog);
    }

    private String getComponentsAvailableInThisArea() {
        AbstractBarWidget parentBar = getParentBar();
        if (parentBar instanceof AreaBarWidget) {
            return ((AreaBarWidget) parentBar).getAvailableComponents();
        }
        return "";
    }

    @Override
    public void attach(Element element) {
        element.appendChild(getElement());
        onAttach();
    }
}