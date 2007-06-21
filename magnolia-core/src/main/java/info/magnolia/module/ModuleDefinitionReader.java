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
package info.magnolia.module;

import info.magnolia.module.model.ModuleDefinition;
import org.apache.commons.betwixt.io.BeanReader;
import org.xml.sax.SAXException;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ModuleDefinitionReader {
    private final BeanReader beanReader;

    public ModuleDefinitionReader() {
        beanReader = new BeanReader();
        try {
            beanReader.registerBeanClass(ModuleDefinition.class);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public ModuleDefinition read(Reader in) {
        try {
            return (ModuleDefinition) beanReader.parse(in);
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        } catch (SAXException e) {
            throw new RuntimeException(e); // TODO
        }

    }
}
