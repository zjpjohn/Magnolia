/**
 * This file Copyright (c) 2009 Magnolia International
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
package info.magnolia.cms.filters;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.UnicodeNormalizer;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;


/**
 * @author Luca Boati
 * @version $Id: $
 */
public class UnicodeNormalizationFilter extends AbstractMgnlFilter
{

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        final AggregationState aggregationState = MgnlContext.getAggregationState();

        HttpServletRequest unicodeRequest = request;

        MgnlContext.getAggregationState().setOriginalURI(
            UnicodeNormalizer.normalizeNFC(MgnlContext.getAggregationState().getOriginalURI()));
        MgnlContext.getAggregationState().setOriginalURL(
            UnicodeNormalizer.normalizeNFC(MgnlContext.getAggregationState().getOriginalURL()));

        // check if it is not a multipart form
        if (MgnlContext.getPostedForm() == null)
        {
            unicodeRequest = new UnicodeNormalizerRequestWrapper(request);
            MgnlContext.push(unicodeRequest, response);
        }
        else
        {
            // if it is a multipart form, request is already wrapped and parameters are read from multipartform object;
            // parameters are sometimes read by form.getParameter (deprecated) so we have to convert values in
            // multipartform.paramters map
            for (Object key : MgnlContext.getPostedForm().getParameters().keySet())
            {
                String[] value = transform((String[]) MgnlContext.getPostedForm().getParameters().get(key));
                MgnlContext.getPostedForm().getParameters().put((String) key, value);
            }
        }

        chain.doFilter(unicodeRequest, response);

        if (MgnlContext.getPostedForm() == null)
        {
            MgnlContext.pop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled()
    {
        //@todo this filter is enabled only if utf8 support is enabled. remove it when the check of this property is not needed anymore.
        return super.isEnabled() && SystemProperty.getBooleanProperty(SystemProperty.MAGNOLIA_UTF8_ENABLED);
    }

    private static String[] transform(String[] input)
    {
        String[] toNormalize = input;
        if (toNormalize != null && toNormalize.length > 0)
        {
            for (int i = 0; i < toNormalize.length; i++)
            {
                toNormalize[i] = UnicodeNormalizer.normalizeNFC(toNormalize[i]);
            }
        }
        return toNormalize;
    }

    public class UnicodeNormalizerRequestWrapper extends HttpServletRequestWrapper
    {

        private HttpServletRequest original;

        private Map parameters;

        /**
         * @param request
         */
        public UnicodeNormalizerRequestWrapper(HttpServletRequest request)
        {
            super(request);
            original = request;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getParameter(String name)
        {
            String[] values = getParameterValues(name);
            if (values != null && values.length > 0)
            {
                return values[0];
            }
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map getParameterMap()
        {
            if (parameters == null)
            {
                parameters = new HashMap<String, String[]>();
                for (Object key : original.getParameterMap().keySet())
                {
                    String[] value = transform((String[]) original.getParameterMap().get(key));
                    parameters.put(key, value);
                }
            }
            return parameters;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getParameterValues(String name)
        {
            return (String[]) getParameterMap().get(name);
        }

    }

}
