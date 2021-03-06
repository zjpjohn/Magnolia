/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.cms.i18n;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract implementation of {@link I18nContentSupport} which stores the
 * locale specific content in node data having a local suffix:
 * &lt;name&gt;_&lt;locale&gt;.
 *
 * The detection of the current locale, based on the URI for instance, is left to the concrete implementation.
 * @author philipp
 *
 */
public abstract class AbstractI18nContentSupport implements I18nContentSupport {

    private static final Logger log = LoggerFactory.getLogger(AbstractI18nContentSupport.class);

    /**
     * The content is served for this locale if the the content is not available for the current locale.
     */
    private Locale fallbackLocale = new Locale("en");

    /**
     * If no locale can be determined the default locale will be set. If no default locale is defined the fallback locale is used.
     */
    protected Locale defaultLocale;

    private boolean enabled = false;

    /**
     * The active locales.
     */
    private final Map<String, Locale> locales = new LinkedHashMap<String, Locale>();

    @Override
    public Locale getLocale() {
        Locale locale = null;
        if(MgnlContext.getWebContextOrNull() != null){
            locale = MgnlContext.getAggregationState().getLocale();
        }
        if (locale == null) {
            return fallbackLocale;
        }
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        MgnlContext.getAggregationState().setLocale(locale);
    }

    @Override
    public Locale getFallbackLocale() {
        return this.fallbackLocale;
    }

    @Override
    public void setFallbackLocale(Locale fallbackLocale) {
        this.fallbackLocale = fallbackLocale;
    }

    /**
     * Returns the closest locale for which {@link #isLocaleSupported(Locale)} is true.
     * <ul>
     * <li>If the locale has a country specified (fr_CH) the locale without country (fr) will be returned.</li>
     * <li>If the locale has no country specified (fr) the first locale with the same language but specific country (fr_CH) will be returned.</li>
     * <li>If this fails the fall-back locale is returned</li>
     * </ul>
     * Warning: if you have configured both (fr and fr_CH) this method will jiter between this two values.
     */
    protected Locale getNextLocale(Locale locale) {
        // if this locale defines a country
        if(StringUtils.isNotEmpty(locale.getCountry())){
            // try to use the language only
            Locale langOnlyLocale = new Locale(locale.getLanguage());
            if(isLocaleSupported(langOnlyLocale)){
                return langOnlyLocale;
            }
        }
        // try to find a locale with the same language (ignore the country)
        for (Iterator<Locale> iter = getLocales().iterator(); iter.hasNext();) {
            Locale otherCountryLocale = iter.next();
            // same lang, but not the same country as well or we end up in the loop
            if(locale.getLanguage().equals(otherCountryLocale.getLanguage()) && !locale.equals(otherCountryLocale)){
                return otherCountryLocale;
            }
        }
        return getFallbackLocale();
    }

    /**
     * Extracts the language from the uri.
     */
    @Override
    public Locale determineLocale() {
        Locale locale;

        locale = onDetermineLocale();

        // depending on the implementation the returned local can be null (not defined)
        if(locale == null){
            locale = getDefaultLocale();
        }
        // if we have a locale but it is not supported we try to get the closest locale
        if(!isLocaleSupported(locale)){
            locale = getNextLocale(locale);
        }
        // instead of returning the content fallback language
        // we are going to return the default locale which might differ
        if(locale.equals(getFallbackLocale())){
            locale = getDefaultLocale();
        }
        return locale;
    }

    protected abstract Locale onDetermineLocale();

    protected static Locale determineLocalFromString(String localeStr) {
        if(StringUtils.isNotEmpty(localeStr)){
            String[] localeArr = StringUtils.split(localeStr, "_");
            if(localeArr.length ==1){
                return new Locale(localeArr[0]);
            }
            else if(localeArr.length == 2){
                return new Locale(localeArr[0],localeArr[1]);
            }
        }
        return null;
    }

    @Override
    public String toI18NURI(String uri) {
        if (!isEnabled()) {
            return uri;
        }
        Locale locale = getLocale();
        if (isLocaleSupported(locale)) {
            return toI18NURI(uri, locale);
        }
        return uri;
    }

    protected abstract String toI18NURI(String uri, Locale locale);

    /**
     * Removes the prefix.
     */
    @Override
    public String toRawURI(String i18nURI) {
        if (!isEnabled()) {
            return i18nURI;
        }

        Locale locale = getLocale();
        if (isLocaleSupported(locale)) {
            return toRawURI(i18nURI, locale);
        }
        return i18nURI;
    }

    protected abstract String toRawURI(String i18nURI, Locale locale);

    @Override
    public NodeData getNodeData(Content node, String name, Locale locale) throws RepositoryException {
        String nodeDataName = name + "_" + locale;
        if (node.hasNodeData(nodeDataName)) {
            return node.getNodeData(nodeDataName);
        }
        return null;
    }

    /**
     * Returns the nodedata with the name &lt;name&gt;_&lt;current language&gt; or &lt;name&gt;_&lt;fallback language&gt
     * otherwise returns &lt;name&gt;.
     */
    @Override
    public NodeData getNodeData(Content node, String name) {
        NodeData nd = null;

        if (isEnabled()) {
            try {
                // test for the current language
                Locale locale = getLocale();
                Set<Locale> checkedLocales = new HashSet<Locale>();

                // getNextContentLocale() returns null once the end of the locale chain is reached
                while(locale != null){
                    nd = getNodeData(node, name, locale);
                    if (!isEmpty(nd)) {
                        return nd;
                    }
                    checkedLocales.add(locale);
                    locale = getNextContentLocale(locale, checkedLocales);
                }
            }
            catch (RepositoryException e) {
                log.error("can't read i18n nodeData " + name + " from node " + node, e);
            }
        }

        // return the node data
        return node.getNodeData(name);
    }

    @Override
    public Node getNode(Node node, String name) throws RepositoryException {
        if (isEnabled()) {

            try {
                // test for the current language
                Locale locale = getLocale();
                Set<Locale> checkedLocales = new HashSet<Locale>();

                // getNextContentLocale() returns null once the end of the locale chain is reached
                while(locale != null){
                    String localeSpecificChildName = name + "_" + locale;
                    if (node.hasNode(localeSpecificChildName)) {
                        return node.getNode(localeSpecificChildName);
                    }
                    checkedLocales.add(locale);
                    locale = getNextContentLocale(locale, checkedLocales);
                }
            }
            catch (RepositoryException e) {
                log.error("can't read i18n node " + name + " from node " + node, e);
            }
        }

        return node.getNode(name);
    }

    @Override
    public boolean hasProperty(Node node, String name) throws RepositoryException {
        if (!isEnabled()) {
            return node.hasProperty(name);
        }
        try {
            // get property using all the rules in getProperty method. If not found, then it doesn't exist.
            getProperty(node, name);
        } catch (RepositoryException e) {
            return false;
        }
        return true;
    }

    @Override
    public Property getProperty(Node node, String name) throws RepositoryException {
        if (!isEnabled()) {
            return node.getProperty(name);
        }
        try {
            // test for the current language
            Locale locale = getLocale();
            Set<Locale> checkedLocales = new HashSet<Locale>();

            // getNextContentLocale() returns null once the end of the locale chain is reached
            while (locale != null) {
                Property property = getProperty(node, name, locale);
                if (!isEmpty(property)) {
                    return property;
                }
                checkedLocales.add(locale);
                locale = getNextContentLocale(locale, checkedLocales);
            }
        } catch (RepositoryException e) {
            log.error("can't read i18n nodeData " + name + " from node " + node, e);
        }

        // return the node data
        return node.getProperty(name);
    }

    @Override
    public Property getProperty(Node node, String name, Locale locale) throws RepositoryException {
        String propName = name + "_" + locale;
        if (node.hasProperty(propName)) {
            return node.getProperty(propName);
        }
        return null;
    }

    /**
     * Uses {@link #getNextLocale(Locale)} to find the next locale. If the returned locale is in the
     * checkedLocales set it falls back to the fall-back locale. If the fall-back locale itself is
     * passed to the method, the method returns null to signal the end of the chain.
     */
    protected Locale getNextContentLocale(Locale locale, Set<Locale> checkedLocales) {
        if(locale.equals(getFallbackLocale())){
            return null;
        }
        Locale candidate = getNextLocale(locale);
        if(!checkedLocales.contains(candidate)){
            return candidate;
        }
        return getFallbackLocale();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Collection<Locale> getLocales() {
        return this.locales.values();
    }

    public void setLocales(Map<String, Locale> locales) {
        this.locales.putAll(locales);
    }

    public void addLocale(LocaleDefinition ld) {
        if (ld.isEnabled()) {
            this.locales.put(ld.getId(), ld.getLocale());
        }
    }

    protected boolean isLocaleSupported(Locale locale) {
        return locale != null && locales.containsKey(locale.toString());
    }

    /**
     * Checks if the nodedata field is empty.
     *
     * @deprecated since 4.5.4. Use {@link #isEmpty(Property)} instead.
     */
    @Deprecated
    protected boolean isEmpty(NodeData nd) {
        if (nd != null && nd.isExist()) {
            // TODO use a better way to find out if it is empty
            return StringUtils.isEmpty(NodeDataUtil.getValueString(nd));
        }
        return true;
    }

    /**
     * Checks if the property field is empty.
     */
    protected boolean isEmpty(Property nd) {
        if (nd != null) {
            return StringUtils.isEmpty(PropertyUtil.getValueString(nd));
        }
        return true;
    }

    @Override
    public Locale getDefaultLocale() {
        if(this.defaultLocale == null){
            return getFallbackLocale();
        }
        return this.defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }
}
