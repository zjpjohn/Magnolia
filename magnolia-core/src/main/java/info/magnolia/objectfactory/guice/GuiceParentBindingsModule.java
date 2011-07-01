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
package info.magnolia.objectfactory.guice;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang.UnhandledException;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.Stage;
import info.magnolia.objectfactory.ComponentProvider;


/**
 * Bridges a Guice Injector to another Injector by adding bindings for all explicit bindings in the injector bridging to.
 *
 * @version $Id$
 */
public class GuiceParentBindingsModule extends AbstractModule {

    // FIXME hardcoded exclusions should be changed

    private Injector parentInjector;
    private Set<Key> excluded = new HashSet<Key>();

    public GuiceParentBindingsModule(Injector parentInjector) {
        this.parentInjector = parentInjector;
        excluded.add(Key.get(ComponentProvider.class));
        excluded.add(Key.get(Injector.class));
        excluded.add(Key.get(Stage.class));
        excluded.add(Key.get(Logger.class));
        excluded.add(Key.get(classForName("info.magnolia.ui.framework.shell.Shell")));
        excluded.add(Key.get(classForName("info.magnolia.ui.framework.place.PlaceController")));
        excluded.add(Key.get(classForName("info.magnolia.ui.framework.event.EventBus")));
    }

    private Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new UnhandledException(e);
        }
    }

    @Override
    protected void configure() {

        Injector targetInjector = parentInjector;
        do {
            Map<Key<?>, Binding<?>> bindings = targetInjector.getBindings();
            for (Map.Entry<Key<?>, Binding<?>> bindingEntry : bindings.entrySet()) {
                Key<?> key = bindingEntry.getKey();
                if (!excluded.contains(key)) {
                    final Provider<?> provider = bindingEntry.getValue().getProvider();
                    bind(key).toProvider(new Provider() {
                        @Override
                        public Object get() {
                            return provider.get();
                        }
                    }).in(Scopes.NO_SCOPE);
                }
            }
            targetInjector = targetInjector.getParent();
        } while (targetInjector != null);
    }
}