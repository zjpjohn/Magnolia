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
package info.magnolia.module.delta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import info.magnolia.cms.filters.FilterManager;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;

/**
 * Orders a filter <strong>before</strong> a given set of other filters. The filter is placed directly before the first
 * of the other filters. The other filters can be required or optional, if a required filter isn't present this task
 * will do nothing and report a warning. If all other filters are optional and none are present this task does nothing.
 * Does not take nested filter into account.
 *
 * @version $Id$
 * @see FilterOrderingTask
 */
public class OrderFilterBeforeTask extends AbstractRepositoryTask {

    private final String filterToBeOrderedName;
    private final List<String> requiredFilters;
    private final List<String> optionalFilters;

    public OrderFilterBeforeTask(String filterName, String[] filtersAfter) {
        this(filterName, filtersAfter, new String[] {});
    }

    public OrderFilterBeforeTask(String filterName, String[] filtersAfter, String[] optionalFiltersAfter) {
        this(filterName, "Orders the " + filterName + " filter in the filter chain.", filtersAfter, optionalFiltersAfter);
    }

    public OrderFilterBeforeTask(String filterName, String description, String[] requiredFiltersAfter) {
        this(filterName, description, requiredFiltersAfter, new String[] {});
    }

    public OrderFilterBeforeTask(String filterName, String description, String[] requiredFiltersAfter, String[] optionalFiltersAfter) {
        super("Setup " + filterName + " filter", description);
        this.filterToBeOrderedName = filterName;
        this.requiredFilters = new ArrayList<String>(Arrays.asList(requiredFiltersAfter));
        this.optionalFilters = new ArrayList<String>(Arrays.asList(optionalFiltersAfter));
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Node filtersParent = ctx.getConfigJCRSession().getNode(FilterManager.SERVER_FILTERS);

        if (!filtersParent.hasNode(filterToBeOrderedName)) {
            throw new TaskExecutionException("Filter with name " + filterToBeOrderedName + " can't be found.");
        }

        if (!requiredFilters.isEmpty() && !hasNodes(filtersParent, requiredFilters)) {
            ctx.warn("Could not sort filter " + filterToBeOrderedName + ". It should be positioned before " + requiredFilters);
            return;
        }

        Set<String> combinedFilterNames = new HashSet<String>();
        combinedFilterNames.addAll(requiredFilters);
        combinedFilterNames.addAll(optionalFilters);

        orderBeforeSiblings(filtersParent.getNode(filterToBeOrderedName), combinedFilterNames);
    }

    private void orderBeforeSiblings(Node node, Set<String> siblingNames) throws RepositoryException {
        Node firstMatch = getFirstChild(node.getParent(), siblingNames);
        if (firstMatch != null) {
            NodeUtil.orderBefore(node, firstMatch.getName());
        }
    }

    private Node getFirstChild(Node parentNode, Collection<String> childNames) throws RepositoryException {
        NodeIterator nodes = parentNode.getNodes();
        while (nodes.hasNext()) {
            Node child = nodes.nextNode();
            if (childNames.contains(child.getName())) {
                return child;
            }
        }
        return null;
    }

    private boolean hasNodes(Node node, Collection<String> childNames) throws RepositoryException {
        for (String childName : childNames) {
            if (!node.hasNode(childName)) {
                return false;
            }
        }
        return true;
    }
}
