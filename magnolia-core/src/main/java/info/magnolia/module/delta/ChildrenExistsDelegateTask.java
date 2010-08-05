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
package info.magnolia.module.delta;

import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

/**
 * A task that delegates to another depending on existence of children.
 * 
 * @author ochytil
 * @version $Revision: $ ($Author: $)
 */
public class ChildrenExistsDelegateTask extends ConditionalDelegateTask {
    private final String workspaceName;
    private final String pathToCheck;
    private final String contentType;

    public ChildrenExistsDelegateTask(String name, String description,
            String workspaceName, String pathToCheck, String contentType,
            Task ifTrue) {
        this(name, description, workspaceName, pathToCheck, contentType,
                ifTrue, null);
    }

    public ChildrenExistsDelegateTask(String name, String description,
            String workspaceName, String pathToCheck, String contentType,
            Task ifTrue, Task ifFalse) {
        super(name, description, ifTrue, ifFalse);
        this.pathToCheck = pathToCheck;
        this.workspaceName = workspaceName;
        this.contentType = contentType;
    }

    protected boolean condition(InstallContext ctx) {
        try {
            if (contentType != null) {
                return ctx.getHierarchyManager(workspaceName).getContent(
                        pathToCheck).hasChildren(contentType);
            } else {
                return ctx.getHierarchyManager(workspaceName).getContent(
                        pathToCheck).hasChildren();
            }
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}