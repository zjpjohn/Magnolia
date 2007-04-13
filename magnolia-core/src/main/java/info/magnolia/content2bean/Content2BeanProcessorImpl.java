/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.content2bean;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ClassUtil;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains the logic for traversing the hierarchy and do the calls to the transformer
 * @author philipp
 * @version $Id$
 *
 */
public class Content2BeanProcessorImpl implements Content2BeanProcessor {

    /**
     * Logger.
     */
    static Logger log = LoggerFactory.getLogger(Content2BeanProcessorImpl.class);

    protected Content2BeanTransformerImpl defaultTransformer = new Content2BeanTransformerImpl();

    /**
     * Initialize the default mappings defined in properies files
     */
   public Content2BeanProcessorImpl() throws Content2BeanException {
        String[] fileNames = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter(){
            public boolean accept(String name) {
                return name.endsWith(".content2bean");
            }
        });

        for (int i = 0; i < fileNames.length; i++) {
            String fileName = fileNames[i];
            Properties props = new Properties();
            InputStream stream = null;
            try {
                stream = ClasspathResourcesUtil.getStream(fileName);
                props.load(stream);

            }
            catch (IOException e) {
                log.error("can't read collection to bean information " + fileName,  e);
            }
            IOUtils.closeQuietly(stream);

            for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
                String key = (String) iter.next();
                Class type;
                try {
                    type = ClassUtil.classForName(StringUtils.substringBeforeLast(key, "."));
                    Class mappedType = ClassUtil.classForName(props.getProperty(key));
                    getDefaultContentToBeanTransformer().addCollectionPropertyClass(type, StringUtils.substringAfterLast(key, "."), mappedType);
                }
                catch (ClassNotFoundException e) {
                    log.error("can't read collection to bean information file: " + fileName + " key: " + key,  e);
                }

            }
        }
    }

   /**
     * Transforms the nodes data into a map containting the names and values. In case recursive is true the subnodes are
     * transformed to maps as well
     * @param node
     * @return a flat map
     */
    public Map toMap(Content node, boolean recursive) {
        Map map = new HashMap();
        for (Iterator iter = node.getNodeDataCollection().iterator(); iter.hasNext();) {
            NodeData nd = (NodeData) iter.next();
            Object val = NodeDataUtil.getValueObject(nd);
            if (val != null) {
                map.put(nd.getName(), val);
            }
        }

        if (recursive) {
            Collection children = ContentUtil.getAllChildren(node);
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                Content childNode = (Content) iter.next();
                Map childMap = toMap(childNode, true);
                map.put(node.getName(), childMap);
            }
        }
        return map;
    }

    public Object toBean(Content node, boolean recursive, final Content2BeanTransformer transformer) throws Content2BeanException{
        Class klass = null;
        try {
            klass = transformer.resolveClass(node);
        }
        catch (ClassNotFoundException e) {
            new Content2BeanException("can't resolve class for node " +  node.getHandle(), e);
        }

        if(klass != null){
            // supports default implementations for interfaces
            Object bean = transformer.newBeanInstance(node, klass);
            transformer.pushBean(bean);
            setProperties(bean, node, recursive, transformer);
            transformer.popBean();
            return bean;
        }
        else{
            // not a bean, make a map
            Map map = toMap(node, false);
            if (recursive) {
                Collection children = node.getChildren(transformer);
                for (Iterator iter = children.iterator(); iter.hasNext();) {
                    Content childNode = (Content) iter.next();
                    transformer.pushBean(map);
                    Object childBean = toBean(childNode, true, transformer);
                    transformer.popBean();
                    map.put(childNode.getName(), childBean);
                }
            }
            return map;
        }
    }

    public Object setProperties(final Object bean, Content node, boolean recursive, final Content2BeanTransformer transformer) throws Content2BeanException {
        Map properties = toMap(node, false);
        if(recursive){
            Collection children = node.getChildren(transformer);
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                final Content childNode = (Content) iter.next();
                // in case the the class can not get resolved we can use now
                // the parent bean to resolve the class

                Object childBean = toBean(childNode, true, transformer);
                properties.put(childNode.getName(), childBean);
            }
        }
        for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
            String propertyName = (String) iter.next();
            transformer.setProperty(bean, propertyName, properties.get(propertyName));
        }

        return bean;
    }

    public Content2BeanTransformerImpl getDefaultContentToBeanTransformer() {
        return defaultTransformer;
    }
}
