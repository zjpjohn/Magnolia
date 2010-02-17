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
package info.magnolia.templatinguicomponents.jsp;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.templatinguicomponents.AuthoringUiComponent;
import info.magnolia.templatinguicomponents.components.EditParagraphBar;

import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * TODO - parameter type conversions !?
 *
 * @jsp.tag name="edit" body-content="empty"
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $) 
 */
public class EditParagraphBarTag extends AbstractTag {
    private String editButtonLabel;
    private boolean enableMoveButton = true;
    private boolean enableDeleteButton = true;
    private String specificDialogName;
    private Content target;

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setEditLabel(String editLabel) {
        this.editButtonLabel = editLabel;
    }

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setMove(boolean move) {
        this.enableMoveButton = move;
    }

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDelete(boolean delete) {
        this.enableDeleteButton = delete;
    }

    /**
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setDialog(String dialog) {
        this.specificDialogName = dialog;
    }

    public void setTarget(Content target) {
        this.target = target;
    }

    @Override
    protected AuthoringUiComponent prepareUIComponent(ServerConfiguration serverCfg, AggregationState aggState) throws JspException, IOException {
        // TODO - this is copied from EditParagraphBarDirective; can't we do better ?
        final EditParagraphBar bar = new EditParagraphBar(serverCfg, aggState);
        if (target != null) {
            bar.setTarget(target);
        }

        if (specificDialogName != null) {
            bar.setSpecificDialogName(specificDialogName);
        }

        if (editButtonLabel != null) {
            // TODO - where to keep default values? jsp-tag, directives, uzw ? Or the component.. but then wrappers have to invent stuff to work around that
            bar.setEditButtonLabel(editButtonLabel);
        }
        bar.setEnableMoveButton(enableMoveButton);
        bar.setEnableDeleteButton(enableDeleteButton);

        return bar;
    }
}