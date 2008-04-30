/**
 * This file Copyright (c) 2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.cache.filter;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicyResult;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class CacheFilter extends AbstractMgnlFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CacheFilter.class);

    private static final String MODULE_NAME = "cache";

    private String cacheConfigurationName = "default";
    private CacheConfiguration cacheConfig;
    private Cache cache;

    /**
     *
     * cachePolicy:shouldCache ?
     *   store:
     *     doFilter
     *     store in cache
     *   useCache:
     *     get from cache
     *   bypass:
     *     doFilter
     *
     */


    public String getCacheConfiguration() {
        return cacheConfigurationName;
    }

    public void setCacheConfiguration(String cacheConfiguration) {
        this.cacheConfigurationName = cacheConfiguration;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        final CacheModule cacheModule = getModule();
        this.cacheConfig = cacheModule.getConfiguration(cacheConfigurationName);
        this.cache = cacheModule.getCacheFactory().getCache(cacheConfigurationName);
    }

    // TODO : maybe this method could be generalized ...
    protected CacheModule getModule() {
        return (CacheModule) ModuleRegistry.Factory.getInstance().getModuleInstance(MODULE_NAME);
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        final AggregationState aggregationState = MgnlContext.getAggregationState();
        final CachePolicyResult cachePolicy = cacheConfig.getCachePolicy().shouldCache(cache, aggregationState);

        final Object cacheKey = cachePolicy.getCacheKey();
        final CachePolicyResult.CachePolicyBehaviour behaviour = cachePolicy.getBehaviour();
        if (behaviour.equals(CachePolicyResult.useCache)) {
//TODO
//            if (!this.ifModifiedSince(request, cacheManager.getCreationTime(cacheKey))) {
//                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

//            } else {

            final CachedPage cached = (CachedPage) cachePolicy.getCachedEntry();
            writeResponse(request, response, cached);
            response.flushBuffer();
//            }


        } else if (behaviour.equals(CachePolicyResult.store)) {
            // will write to both the response stream and an internal byte array for caching
            final ByteArrayOutputStream cachingStream = new ByteArrayOutputStream();
            final TeeOutputStream teeOutputStream = new TeeOutputStream(response.getOutputStream(), cachingStream);
            final SimpleServletOutputStream out = new SimpleServletOutputStream(teeOutputStream);
            final CacheResponseWrapper responseWrapper = new CacheResponseWrapper(response, out);
            chain.doFilter(request, responseWrapper);

            try {
                responseWrapper.flushBuffer();
            } catch (IOException e) {
                //TODO better handling ?
                // ignore and don't cache, should be a ClientAbortException
                return;
            }

            final CachedPage cachedEntry = makeCachedEntry(responseWrapper, cachingStream);
            if (cachedEntry != null && cachedEntry.getOut().length > 0) {
                cache.put(cacheKey, cachedEntry);
            }

        } else if (behaviour.equals(CachePolicyResult.bypass)) {
            chain.doFilter(request, response);
        } else {
            throw new IllegalStateException("Unexpected cache policy result: " + cachePolicy);
        }
    }

    public CachedPage makeCachedEntry(CacheResponseWrapper cacheResponse, ByteArrayOutputStream cachingStream) {
        if (cachingStream == null) {
            return null;
        }
        final byte[] aboutToBeCached = cachingStream.toByteArray();
        return new CachedPage(aboutToBeCached,
                cacheResponse.getContentType(),
                cacheResponse.getCharacterEncoding(),
                cacheResponse.getStatus(),
                cacheResponse.getHeaders());
    }

    /**
     * Check if server cache is newer then the client cache
     * @param request The servlet request we are processing
     * @return boolean true if the server resource is newer
     */
    protected boolean ifModifiedSince(HttpServletRequest request, long lastModified) {
        try {
            long headerValue = request.getDateHeader("If-Modified-Since");
            if (headerValue != -1) {
                // If an If-None-Match header has been specified, if modified since
                // is ignored.
                if ((request.getHeader("If-None-Match") == null)
                        && (lastModified > 0 && lastModified <= headerValue + 1000)) {
                    return false;
                }
            }
        }
        catch (IllegalArgumentException illegalArgument) {
            return true;
        }
        return true;
    }

    public boolean clientAcceptsGzip(HttpServletRequest request) {
        return StringUtils.contains(request.getHeader("Accept-Encoding"), "gzip");
    }

    protected void writeResponse(final HttpServletRequest request, final HttpServletResponse response, final CachedPage cachedEntry) throws IOException {
//        boolean requestAcceptsGzipEncoding = acceptsGzipEncoding(request);

        response.setStatus(cachedEntry.getStatusCode());
        //requestAcceptsGzipEncoding,
        addHeaders(cachedEntry, response);
        // TODO : cookies ?
        response.setContentType(cachedEntry.getContentType());
        // response.setCharacterEncoding();
        writeContent(request, response, cachedEntry);
    }



    /**
     * Set the headers in the response object TODO, excluding the Gzip header ?
     */
    protected void addHeaders(final CachedPage cachedEntry, final HttpServletResponse response) {
        final MultiMap headers = cachedEntry.getHeaders();

        final Iterator it = headers.keySet().iterator();
        while (it.hasNext()) {
            final String header = (String) it.next();
            final Collection values = (Collection) headers.get(header);
            final Iterator valIt = values.iterator();
            while (valIt.hasNext()) {
                final Object val = valIt.next();
                if (val instanceof Long) {
                    response.addDateHeader(header, ((Long) val).longValue());
                } else if (val instanceof Integer) {
                    response.addIntHeader(header, ((Integer) val).intValue());
                } else if (val instanceof String) {
                    response.addHeader(header, (String) val);
                } else {
                    throw new IllegalStateException("Unrecognized type for header [" + header + "], value is: " + val);
                }

            }
        }
    }

    protected void writeContent(final HttpServletRequest request, final HttpServletResponse response, final CachedPage cachedEntry) throws IOException {
       final byte[] body = cachedEntry.getOut();
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }

}
