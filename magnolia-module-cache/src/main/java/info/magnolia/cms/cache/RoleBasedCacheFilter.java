/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.cache;

import info.magnolia.context.MgnlContext;
import info.magnolia.cms.security.Digester;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author Sameer Charles
 * $Id$
 */
public class RoleBasedCacheFilter extends CacheFilter {

    /**
     * This will:
     * - Consolidate all roles sorted by node name
     * - Generate MD5 of this consolidated string
     * - Adds this MD5 to the key generated by cache manager
     * */
    public CacheKey getCacheKey(HttpServletRequest request) {
        return this.generateMD5Key(MgnlContext.getUser().getAllRoles(), super.getCacheKey(request));
    }

    /**
     * generate an MD5 of all roles assigned to this user
     * @param assignedRoles
     * @param key as created by configured CacheManager
     * */
    private CacheKey generateMD5Key(Collection assignedRoles, CacheKey key) {
        Collections.sort((List) assignedRoles);
        String asString = ArrayUtils.toString(assignedRoles);
        StringBuffer buffer = new StringBuffer(key.toString()).append(".").append(Digester.getMD5Hex(asString));
        key.setValue(new String(buffer));
        return key;
    }

}
