/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.repository.definition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO this isn't a repository mapping definition, its the total set of configuration we have to setup the JCR layer

/**
 * Represents the configuration defined in repositories.xml.
 *
 * @version $Id$
 */
public class RepositoryMappingDefinition {

    // TODO thread safety issues here, make immutable?

    private final Map<String, WorkspaceMappingDefinition> workspaceMappings = new LinkedHashMap<String, WorkspaceMappingDefinition>();
    private final Map<String, RepositoryDefinition> repositories = new LinkedHashMap<String, RepositoryDefinition>();

    public void setMappings(Map<String, WorkspaceMappingDefinition> workspaceMappings) {
        this.workspaceMappings.putAll(workspaceMappings);
    }

    public void addMapping(String logicalWorkspaceName, String repositoryName, String workspaceName) {
        workspaceMappings.put(logicalWorkspaceName, new WorkspaceMappingDefinition(logicalWorkspaceName, repositoryName, workspaceName));
    }

    public void setRepositories(Collection<RepositoryDefinition> repositories) {
        for (RepositoryDefinition def : repositories) {
            this.addRepository(def);
        }
    }

    public void addRepository(RepositoryDefinition repositoryDefinition) {
        this.repositories.put(repositoryDefinition.getName(), repositoryDefinition);
    }

    public Collection<WorkspaceMappingDefinition> getWorkspaceMappings() {
        return workspaceMappings.values();
    }

    public Collection<RepositoryDefinition> getRepositories() {
        return repositories.values();
    }
}
