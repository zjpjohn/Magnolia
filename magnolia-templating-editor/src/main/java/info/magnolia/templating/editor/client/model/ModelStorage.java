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
package info.magnolia.templating.editor.client.model;

import info.magnolia.templating.editor.client.AbstractBarWidget;
import info.magnolia.templating.editor.client.AbstractOverlayWidget;
import info.magnolia.templating.editor.client.dom.MgnlElement;
import info.magnolia.templating.editor.client.model.focus.FocusModel;
import info.magnolia.templating.editor.client.model.focus.FocusModelImpl3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;

/**
 * Singleton keeping the model.
 */
public class ModelStorage {

    private static ModelStorage storage = null;

    private FocusModel focusModel = new FocusModelImpl3(this);

    private Map<MgnlElement, AbstractBarWidget> editBars = new HashMap<MgnlElement, AbstractBarWidget>();
    private Map<MgnlElement, AbstractOverlayWidget> overlays = new HashMap<MgnlElement, AbstractOverlayWidget>();
    private Map<MgnlElement, List<Element>> elements = new HashMap<MgnlElement, List<Element>>();
    private Map<Element, MgnlElement> mgnlElements = new HashMap<Element, MgnlElement>();


    public List<MgnlElement> rootElements = new LinkedList<MgnlElement>();


    private MgnlElement selectedMgnlElement = null;

    public static ModelStorage getInstance() {
        if (storage == null) {
            storage = new ModelStorage();
        }
        return storage;
    }

    public void addOverlay(MgnlElement mgnlElement, AbstractOverlayWidget overlayWidget) {
        overlays.put(mgnlElement, overlayWidget);
    }

    public AbstractOverlayWidget getOverlay(MgnlElement mgnlElement) {
        return overlays.get(mgnlElement);
    }

    public void addEditBar(MgnlElement mgnlElement, AbstractBarWidget editBar) {
        editBars.put(mgnlElement, editBar);
    }

    public AbstractBarWidget getEditBar(MgnlElement mgnlElement) {
        return editBars.get(mgnlElement);
    }

    public void addElement(MgnlElement mgnlElement, Element element) {

        mgnlElements.put(element, mgnlElement);

        if (elements.get(mgnlElement) != null) {
            elements.get(mgnlElement).add(element);
        }
        else {
            List<Element> elList = new LinkedList<Element>();
            elList.add(element);
            elements.put(mgnlElement, elList);
        }
    }

    public MgnlElement getMgnlElement(Element element) {
        return mgnlElements.get(element);
    }

    public List<Element> getElements(MgnlElement mgnlElement) {
        return elements.get(mgnlElement);
    }

    public void addRoot(MgnlElement boundary) {
        this.rootElements.add(boundary);
    }

    public List<MgnlElement> getRootElements() {
        return rootElements;
    }

    public void setSelectedMgnlElement(MgnlElement selectedMgnlElement) {
        this.selectedMgnlElement = selectedMgnlElement;
    }

    public MgnlElement getSelectedMgnlElement() {
        return selectedMgnlElement;
    }

    public FocusModel getFocusModel() {
        return focusModel;
    }

}