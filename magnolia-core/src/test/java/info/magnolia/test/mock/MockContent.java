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
package info.magnolia.test.mock;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.DefaultContent;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.security.AccessDeniedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;


/**
 * @author philipp
 * @version $Id$
 */
public class MockContent extends DefaultContent {

    private String uuid;

    private Content parent;

    private HierarchyManager hierarchyManager;

    private String name;

    private OrderedMap nodeDatas = new ListOrderedMap();

    private OrderedMap children = new ListOrderedMap();

    private String nodeTypeName = ItemType.CONTENTNODE.getSystemName();

    public MockContent(String name) {
        this.name = name;
    }

    public MockContent(String name, ItemType contentType) {
        this(name);
        this.setNodeTypeName(contentType.getSystemName());
    }

    public MockContent(String name, OrderedMap nodeDatas, OrderedMap children) {
        this(name);
        for (Iterator iter = children.values().iterator(); iter.hasNext();) {
            MockContent c = (MockContent) iter.next();
            addContent(c);
        }
        for (Iterator iter = nodeDatas.values().iterator(); iter.hasNext();) {
            MockNodeData nd = (MockNodeData) iter.next();
            addNodeData(nd);
        }
    }

    public void addNodeData(MockNodeData nd) {
        nd.setParent(this);
        nodeDatas.put(nd.getName(), nd);
    }

    public MockMetaData createMetaData() {
        addContent(new MockContent("MetaData"));//, ItemType."mgnl:metaData"));
        return getMetaData();
    }

    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        return createContent(name, new ItemType(contentType));
    }

    public Content createContent(String name, ItemType contentType) {
        MockContent c = new MockContent(name, contentType);
        addContent(c);
        return c;
    }

    public void addContent(MockContent child) {
        child.setParent(this);
        children.put(child.getName(), child);
    }

    public Content getContent(String name) throws RepositoryException {
        Content c;
        if (name.contains("/")) {
            c = getContent(StringUtils.substringBefore(name, "/"));
            if (c != null) {
                return c.getContent(StringUtils.substringAfter(name, "/"));
            }
        }
        else {
            c = (Content) children.get(name);
        }
        if (c == null) {
            throw new PathNotFoundException(name);
        }
        return c;
    }

    public boolean hasContent(String name) throws RepositoryException {
        return children.containsKey(name);
    }

    public String getHandle() {
        if (this.getParent() != null && !this.getParent().getName().equals("jcr:root")) {
            return getParent().getHandle() + "/" + this.getName();
        }
        return "/" + this.getName();
    }

    public int getLevel() throws PathNotFoundException, RepositoryException {
        if (this.getParent() == null) {
            return 0;
        }
        return getParent().getLevel() + 1;
    }

    public Collection getNodeDataCollection() {
        return this.nodeDatas.values();
    }

    public NodeData getNodeData(String name) {
        final MockNodeData nodeData = (MockNodeData) this.nodeDatas.get(name);
        return nodeData !=null ? nodeData : new MockNodeData(name, null);
    }

    public boolean hasNodeData(String name) throws RepositoryException {
        return nodeDatas.containsKey(name);
    }

    // TODO : use the given Comparator
    public Collection getChildren(final ContentFilter filter, Comparator orderCriteria) {
        // copy
        List children = new ArrayList(this.children.values());

        CollectionUtils.filter(children, new Predicate() {

            public boolean evaluate(Object object) {
                return filter.accept((Content) object);
            }
        });

        return children;
    }

    public Collection getChildren(final String contentType, String namePattern) {
        if (!"*".equals(namePattern)) {
            throw new IllegalStateException("Only the \"*\" name pattern is currently supported in MockContent.");
        }
        return getChildren(new ContentFilter() {
            public boolean accept(Content content) {
                return contentType == null || content.isNodeType(contentType);
            }
        });

    }

    public Content getChildByName(String namePattern) {
        return (Content) children.get(namePattern);
    }

    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        Content movedNode = (Content)children.get(srcName);
        List tmp = new ArrayList(children.values());
        tmp.remove(movedNode);
        tmp.add(tmp.indexOf(children.get(beforeName)), movedNode);
        children.clear();
        for (Iterator iter = tmp.iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            children.put(child.getName(), child);
        }
    }

    public void save() throws RepositoryException {
        // nothing to do
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNodeTypeName() throws RepositoryException {
        return this.nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    public Content getParent() {
        return this.parent;
    }

    public void setParent(Content parent) {
        this.parent = parent;
    }

    public HierarchyManager getHierarchyManager() {
        if (this.hierarchyManager == null && getParent() != null) {
            return ((MockContent) getParent()).getHierarchyManager();
        }
        return this.hierarchyManager;
    }

    /**
     * @param hm the hm to set
     */
    public void setHierarchyManager(HierarchyManager hm) {
        this.hierarchyManager = hm;
    }

    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public MockMetaData getMetaData() {
        try {
            return new MockMetaData((MockContent) getContent("MetaData"));
        } catch (RepositoryException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public String toString() {
        return super.toString() + ": " + this.getHandle();
    }
}