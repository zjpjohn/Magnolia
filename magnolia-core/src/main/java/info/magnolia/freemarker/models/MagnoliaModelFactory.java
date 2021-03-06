/**
 * This file Copyright (c) 2009-2012 Magnolia International
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
package info.magnolia.freemarker.models;

import freemarker.ext.util.ModelFactory;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModel;

/**
 * A specialization of Freemarker's ModelFactory, which knows which
 * class its handling. Mostly just used so we can simply register
 * other factories in Magnolia's configuration without too many hacks
 * nor billions of subnodes.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface MagnoliaModelFactory extends ModelFactory {
    /**
     * Returns the Class this ModelFactory is able to create Model instances for.
     * MagnoliaObjectWrapper uses this with a isAssignableFrom call to determine
     * which ModelFactory to use.
     */
    Class factoryFor();

    /**
     * If possible, our models should instantiate models which implement AdapterTemplateModel,
     * so they can be unwrapped easily. (by custom directives, for example)
     * @see freemarker.template.AdapterTemplateModel
     */
    @Override
    TemplateModel create(Object object, ObjectWrapper wrapper);
}
