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
package info.magnolia.ui.admincentral.column;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.JCRMetadataUtil;
import info.magnolia.ui.admincentral.util.UIUtil;
import info.magnolia.ui.model.column.definition.StatusColumnDefinition;

import java.io.Serializable;
import java.security.AccessControlException;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;

/**
 * A column that displays icons for permissions and activation status.
 *
 * @author dlipp
 * @author tmattsson
 */
public class StatusColumn extends AbstractColumn<StatusColumnDefinition> implements Serializable {

    private boolean activation = true;

    private boolean permissions = false;

    public StatusColumn(StatusColumnDefinition def) {
        super(def);
    }

    public boolean isActivation() {
        return activation;
    }

    public void setActivation(boolean activation) {
        this.activation = activation;
    }

    public boolean isPermissions() {
        return permissions;
    }

    public void setPermissions(boolean permissions) {
        this.permissions = permissions;
    }

    @Override
    public Component getComponent(Item item) throws RepositoryException {
        if (item instanceof Node) {
            Node node = (Node) item;
            return new ActivationStatus(node, activation, permissions);
        }
        return null;
    }

    /**
     * A comparable component which embeds an icon.
     * TODO: extract it as a top-level class?
     * @author fgrilli
     *
     */
    protected class ActivationStatus extends CustomComponent implements Comparable<ActivationStatus>{

        private int status;
        private HorizontalLayout root = new HorizontalLayout();

        public ActivationStatus(final Node node, boolean activation, boolean permissions) throws RepositoryException {
            setCompositionRoot(root);
            if(activation) {
                this.status = JCRMetadataUtil.getMetaData(node).getActivationStatus();
                Embedded activationStatus = new Embedded();
                activationStatus.setType(Embedded.TYPE_IMAGE);
                activationStatus.setSource(new ExternalResource(UIUtil.getActivationStatusIconURL(node)));
                activationStatus.setWidth(16, Sizeable.UNITS_PIXELS);
                activationStatus.setHeight(16, Sizeable.UNITS_PIXELS);
                root.addComponent(activationStatus);
            }
            if (permissions) {
                try {
                    // TODO dlipp: verify, this shows the same behavior as old Content-API based
                    // implementation:
                    // if (permissions && !node.isGranted(info.magnolia.cms.security.Permission.WRITE))
                    node.getSession().checkPermission(node.getPath(), Session.ACTION_SET_PROPERTY);

                } catch (AccessControlException e) {
                    // does not have permission to set properties - in that case will return two Icons
                    // in a layout for being displayed...
                    Embedded permissionStatus = new Embedded();
                    permissionStatus.setType(Embedded.TYPE_IMAGE);
                    permissionStatus.setSource(new ExternalResource(MgnlContext.getContextPath() + UIUtil.RESOURCES_ICONS_16_PATH + "pen_blue_canceled.gif"));
                    permissionStatus.setWidth(16, Sizeable.UNITS_PIXELS);
                    permissionStatus.setHeight(16, Sizeable.UNITS_PIXELS);
                    root.addComponent(permissionStatus);
                }
            }
        }

        public int getStatus() {
            return status;
        }

        public int compareTo(ActivationStatus o) {
            return Integer.valueOf(status).compareTo(Integer.valueOf(o.getStatus()));
        }
    }
}
