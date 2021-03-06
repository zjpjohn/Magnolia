/**
 * This file Copyright (c) 2007-2012 Magnolia International
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

import info.magnolia.cms.util.WorkspaceXmlUtil;

import java.util.List;

/**
 * A utility class for workspace.xml related conditions, which will add
 * conditions to a given list of tasks based on some conditions.
 *
 * @version $Id$
 */
public class WorkspaceXmlConditionsUtil {
    private final List<Condition> conditions;

    public WorkspaceXmlConditionsUtil(List<Condition> conditions) {
        this.conditions = conditions;
    }

    /**
     * @deprecated since 4.5 - use {@link #textFilterClassesAreNotSet()} instead.
     */
    public void workspaceHasOldIndexer() {
        textFilterClassesAreNotSet();
    }

    public void textFilterClassesAreNotSet() {
        List<String> names =  WorkspaceXmlUtil.getWorkspaceNamesMatching("/Workspace/SearchIndex/param[@name='textFilterClasses']/@value");

        if (names.size() > 0) {
            for (int i = 0; i < names.size(); i++) {
                conditions.add(new FalseCondition("workspace.xml updates",
                        "Workspace definition in workspace " + names.get(i) +
                                " references indexer which has changed; please remove the parameter 'textFilterClasses'."));
            }
        }
    }

    /**
     * Until https://issues.apache.org/jira/browse/JCR-3236 is fixed the param "analyzer" should not be around.
     */
    public void paramAnalyzerIsNotSet() {
        List<String> names = WorkspaceXmlUtil.getWorkspaceNamesMatching("/Workspace/SearchIndex/param[@name='analyzer']/@value");
        if (names.size() > 0) {
            for (int i = 0; i < names.size(); i++) {
                conditions.add(new WarnCondition("workspace.xml updates",
                        "Workspace definition in workspace " + names.get(i) +
                                " Should not have an analyzer set - this will lead to error-logs when strarting up your server."));
            }
        }
    }

    public void accessControlProviderIsSet() {
        List<String> names = WorkspaceXmlUtil.getWorkspaceNames("/Workspace/WorkspaceSecurity/AccessControlProvider/@class", null);
        if (names.size() > 0) {
            for (int i = 0; i < names.size(); i++) {
                conditions.add(new FalseCondition("workspace.xml updates",
                        "Workspace definition in workspace " + names.get(i) +
                                " must have an AccessControlProvider set."));
            }
        }
    }
}
