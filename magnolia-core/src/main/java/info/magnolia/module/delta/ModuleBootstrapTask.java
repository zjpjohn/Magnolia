/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.delta;

import info.magnolia.module.InstallContext;

/**
 * A task to bootstrap a module.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleBootstrapTask extends BootstrapResourcesTask {
    public ModuleBootstrapTask() {
        super("Bootstrap", "Bootstraps the necessary module repository content.");
    }

    protected boolean acceptResource(InstallContext ctx, String name) {
        final String moduleName = ctx.getCurrentModuleDefinition().getName();
        return name.startsWith("/mgnl-bootstrap/" + moduleName + "/") && name.endsWith(".xml");
    }
}
