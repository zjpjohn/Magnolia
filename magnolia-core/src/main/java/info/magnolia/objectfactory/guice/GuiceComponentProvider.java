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
package info.magnolia.objectfactory.guice;

import info.magnolia.objectfactory.CandidateParameterResolver;
import info.magnolia.objectfactory.ComponentFactory;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.NoSuchComponentException;
import info.magnolia.objectfactory.ObjectManufacturer;
import info.magnolia.objectfactory.ParameterResolver;

import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;


/**
 * ComponentProvider implementation based on Guice.
 *
 * @version $Id$
 * @see ComponentProvider
 * @see GuiceComponentProviderBuilder
 */
public class GuiceComponentProvider implements ComponentProvider {

    private static Logger logger = LoggerFactory.getLogger(GuiceComponentProvider.class);

    @Inject
    private Injector injector;
    private ObjectManufacturer manufacturer;
    private final Map<Class<?>, Class<?>> typeMappings;
    private final GuiceComponentProvider parentComponentProvider;

    public GuiceComponentProvider(Map<Class<?>, Class<?>> typeMappings, GuiceComponentProvider parentComponentProvider) {
        this.parentComponentProvider = parentComponentProvider;
        this.typeMappings = typeMappings;
    }

    @Override
    public <T> Class<? extends T> getImplementation(Class<T> type) {
        Class<?> implementation = typeMappings.get(type);
        if (implementation == null) {
            if (parentComponentProvider != null) {
                return parentComponentProvider.getImplementation(type);
            }
            return type;
        }
        if (ComponentFactory.class.isAssignableFrom(implementation)) {
            return type;
        }
        return (Class<? extends T>) implementation;
    }

    @Override
    @Deprecated
    public <T> T getSingleton(Class<T> type) {
        return getComponent(type);
    }

    @Override
    public <T> T getComponent(Class<T> type) throws NoSuchComponentException{
        if (!GuiceUtils.hasExplicitBindingFor(injector, type)) {
            throw new NoSuchComponentException("No component configuration for type [" + type.getName() + "] found. Please add a configuration to your module descriptor.");
        }
        return injector.getInstance(type);
    }

    @Override
    public <T> T newInstance(Class<T> type, Object... parameters) {
        return newInstanceWithParameterResolvers(type, new CandidateParameterResolver(parameters));
    }

    @Override
    public <T> T newInstanceWithParameterResolvers(Class<T> type, ParameterResolver... parameterResolvers) {
        if (this.manufacturer == null) {
            this.manufacturer = new ObjectManufacturer();
        }
        Class<? extends T> implementation = getImplementation(type);
        parameterResolvers = concat(parameterResolvers, new GuiceParameterResolver(injector));
        T instance = (T) manufacturer.newInstance(implementation, parameterResolvers);
        injectMembers(instance);
        return instance;
    }

    private ParameterResolver[] concat(ParameterResolver[] array, ParameterResolver extra) {
        ParameterResolver[] newArray = Arrays.copyOf(array, array.length + 1);
        newArray[array.length] = extra;
        return newArray;
    }

    public Injector getInjector() {
        return injector;
    }

    public <T> Provider<T> getProvider(Class<T> type) {
        if (!GuiceUtils.hasExplicitBindingFor(injector, type)) {
            return null;
        }
        return injector.getProvider(type);
    }

    public void injectMembers(Object instance) {
        injector.injectMembers(instance);
    }

    public void destroy() {
        /*

       Destroy using @PreDestroy is disabled because the implementation acquires instances for lazy-init singletons
       only to destroy them. It also tries to acquire instances that have non-existing scopes leading to exceptions
       being thrown. This usually results in shutdown of the application being interrupted before having a chance to
       properly close down JackRabbit. With (at least) the derby persistence manager this results in threads not being
       closed down properly and therefore Tomcat stalls at shutdown.

        */
    }

    @Override
    public GuiceComponentProvider getParent() {
        return parentComponentProvider;
    }
}
