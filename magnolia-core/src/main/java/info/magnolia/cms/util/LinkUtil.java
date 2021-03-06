/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.cms.util;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * Utility methods for links.
 */
public class LinkUtil {

    private static final FastDateFormat FINGERPRINT_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd-HH-mm-ss");

    /**
     * Insert a finger-print into a link that is based on the last modified date.
     * This way we can use far future cache headers on images/assets, and simply change the filename for the
     * asset when the asset has changed and we want browsers & proxies to take up the new asset from the server.
     * Appends the date as a string directly before the file extension.
     * 
     * @param lastModified
     * @return The original link with the date based finger-print inserted.
     */
    public static String addFingerprintToLink(String link, Calendar lastModified) {
        String fingerprintedLink = "";
        if (lastModified == null) {
            return link;
        }

        String fingerprint = FINGERPRINT_FORMAT.format(lastModified.getTime());

        // Determine where to place the fingerprint.
        int lastDot = link.lastIndexOf('.');
        int lastSlash = link.lastIndexOf('/');

        if (lastDot > lastSlash && lastDot != -1) {
            fingerprintedLink = link.substring(0, lastDot) + "." + fingerprint + link.substring(lastDot);
        } else {
            // No file extension - just add fingerprint at end.
            fingerprintedLink = link + "." + fingerprint;
        }

        return fingerprintedLink;
    }

    /**
     * Remove the extension and fingerPrint if present.
     * Example: (print-logo.2012-11-20-12-15-20.pdf --> print-logo)
     */
    public static String removeFingerprintAndExtensionFromLink(String originalPath) {

        String subPath = StringUtils.substringBeforeLast(originalPath, ".");
        // Get Finger print
        String fingerPrint = StringUtils.substringAfterLast(subPath, ".");
        if (fingerPrint != null && fingerPrint.matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}")) {
            return StringUtils.substringBeforeLast(subPath, ".");
        } else {
            return subPath;
        }
    }

}
