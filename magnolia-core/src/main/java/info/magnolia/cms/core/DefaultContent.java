/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.core;

import info.magnolia.cms.core.version.ContentVersion;
import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Rule;
import info.magnolia.logging.AuditLoggingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.derby.impl.sql.compile.HasNodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default, JCR-based, implementation of {@link Content}.
 * 
 * @author Sameer Charles
 * @version $Revision:2719 $ ($Author:scharles $)
 */
public class DefaultContent extends AbstractContent {
    private static final Logger log = LoggerFactory.getLogger(DefaultContent.class);

    /**
     * Wrapped jcr node.
     */
    protected Node node;

    /**
     * Path for the jcr node.
     */
    private String path;

    /**
     * root node.
     */
    private Node rootNode;

    /**
     * node metadata.
     */
    private MetaData metaData;

    /**
     * Empty constructor. Should NEVER be used for standard use, test only.
     */
    protected DefaultContent() {
    }

    /**
     * Constructor to get existing node.
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @param hierarchyManager HierarchyManager instance
     * @throws PathNotFoundException if the node at <code>path</code> does not exist
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    DefaultContent(Node rootNode, String path, HierarchyManager hierarchyManager) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        this.setHierarchyManager(hierarchyManager);
        Access.isGranted(hierarchyManager.getAccessManager(), Path.getAbsolutePath(rootNode.getPath(), path), Permission.READ);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.setNode(this.rootNode.getNode(this.path));
    }

    /**
     * Constructor to get existing node.
     * @param elem initialized node object
     * @param hierarchyManager HierarchyManager instance
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     * @throws RepositoryException if an error occurs
     */
    public DefaultContent(Item elem,HierarchyManager hierarchyManager) throws RepositoryException, AccessDeniedException {
        this.setHierarchyManager(hierarchyManager);
        Access.isGranted(hierarchyManager.getAccessManager(), Path.getAbsolutePath(elem.getPath()), Permission.READ);
        this.setNode((Node) elem);
        this.setPath(this.getHandle());
    }

    /**
     * creates contentNode of type <b>contentType </b> contentType must be defined in item type definition of magnolia
     * as well as JCR implementation
     * @param rootNode node to start with
     * @param path absolute (primary) path to this <code>Node</code>
     * @param contentType JCR node type as configured
     * @param hierarchyManager HierarchyManager instance
     * @throws PathNotFoundException if the node at <code>path</code> does not exist
     * @throws RepositoryException if an error occurs
     * @throws AccessDeniedException if the current session does not have sufficient access rights to complete the
     * operation
     */
    DefaultContent(Node rootNode, String path, String contentType, HierarchyManager hierarchyManager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        this.setHierarchyManager(hierarchyManager);
        Access.isGranted(hierarchyManager.getAccessManager(), Path.getAbsolutePath(rootNode.getPath(), path), Permission.WRITE);
        this.setPath(path);
        this.setRootNode(rootNode);
        this.node = this.rootNode.addNode(this.path, contentType);
        // add mix:lockable as default for all nodes created using this manager
        // for version 3.5 we cannot change node type definitions because of compatibility reasons
        // MAGNOLIA-1518
        this.addMixin(ItemType.MIX_LOCKABLE);
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_CREATE, hierarchyManager.getName(), this.getItemType(), Path.getAbsolutePath(node.getPath()));
    }

    /**
     * @param node
     */
    protected void setNode(Node node) {
        this.node = node;
    }

    /**
     * @param node
     */
    protected void setRootNode(Node node) {
        this.rootNode = node;
    }

    /**
     * @param path
     */
    protected void setPath(String path) {
        this.path = path;
    }

    public Content getContent(String name) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new DefaultContent(this.node, name, this.hierarchyManager));
    }

    public Content createContent(String name, String contentType) throws PathNotFoundException, RepositoryException,
        AccessDeniedException {
        Content content = new DefaultContent(this.node, name, contentType, this.hierarchyManager);
        MetaData metaData = content.getMetaData();
        metaData.setCreationDate();
        return content;
    }

    @Override
    public boolean hasNodeData(String name) throws RepositoryException {
        if (this.node.hasProperty(name)) {
            return true;
        }
        else { // check for mgnl:resource node
            if (this.node.hasNode(name) && this.node.getNode(name).isNodeType(ItemType.NT_RESOURCE)) {
                return true;
            }
        }
        return false;
    }
    
    public NodeData newNodeDataInstance(String name, int type, boolean createIfNotExisting) throws AccessDeniedException, RepositoryException {
        try {
            Access.isGranted(getAccessManager(), Path.getAbsolutePath(getHandle(), name), Permission.READ);
        }
        // FIXME: should be thrown but return a dummy node data
        catch (AccessDeniedException e) {
            return new NonExistingNodeData(this, name);
        }

        // create an empty dummy
        if(!hasNodeData(name) && !createIfNotExisting){
            return new NonExistingNodeData(this, name);
        }
        
        if(type == PropertyType.UNDEFINED){
            type = determineNodeDataType(name);
        }

        if(type == PropertyType.BINARY){
            return new BinaryNodeData(this, name);
        }
        else{
            return new DefaultNodeData(this, name);
        }
    }

    protected int determineNodeDataType(String name) {
        // FIXME: maybe delegate to NodeDataImplementations?
        try {
            if (this.node.hasProperty(name)) {
                return this.node.getProperty(name).getType();
            }
            else { // check for mgnl:resource node
                if (this.node.hasNode(name) && this.node.getNode(name).isNodeType(ItemType.NT_RESOURCE)) {
                    return PropertyType.BINARY;
                }
            }
        }
        catch (RepositoryException e) {
            throw new IllegalStateException("Can't determine property type of [" + getHandle() + "/" + name + "]", e);
        }
        return PropertyType.UNDEFINED;
    }
    

    public MetaData getMetaData() {
        if (this.metaData == null) {
            this.metaData = new MetaData(this.node, this.hierarchyManager.getAccessManager());
        }
        return this.metaData;
    }

    public String getName() {
        try {
            return this.node.getName();
        }
        catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    public Collection<Content> getChildren(ContentFilter filter, String namePattern, Comparator<Content> orderCriteria) {
        Collection<Content> children;
        if (orderCriteria == null) {
            children = new ArrayList<Content>();
        }
        else {
            children = new TreeSet<Content>(orderCriteria);
        }

        try {
            final NodeIterator nodeIterator;
            if (namePattern == null) {
                nodeIterator = this.node.getNodes();
            } else {
                nodeIterator = this.node.getNodes(namePattern);
            }

            while (nodeIterator.hasNext()) {
                Node subNode = (Node) nodeIterator.next();
                try {
                    Content content = new DefaultContent(subNode, this.hierarchyManager);
                    if (filter.accept(content)) {
                        children.add(content);
                    }
                }
                catch (PathNotFoundException e) {
                    log.error("Exception caught", e);
                }
                catch (AccessDeniedException e) {
                    // ignore, simply wont add content in a list
                }
            }
        }
        catch (RepositoryException re) {
            log.error("Exception caught", re);
        }

        return children;
    }
    
    public Collection<NodeData> getNodeDataCollection(String namePattern) {
        final ArrayList<NodeData> all = new ArrayList<NodeData>();
        try {
            all.addAll(getPrimitiveNodeDatas(namePattern));
            all.addAll(getBinaryNodeDatas(namePattern));
        }
        catch (RepositoryException e) {
            throw new IllegalStateException("Can't read node datas of " + toString(), e);
        }
        return all;
    }

    protected Collection<NodeData> getPrimitiveNodeDatas(String namePattern) throws RepositoryException {
        final Collection<NodeData> nodeDatas = new ArrayList<NodeData>();
        final PropertyIterator propertyIterator;
        if (namePattern == null) {
            propertyIterator = this.node.getProperties();
        } else {
            propertyIterator = this.node.getProperties(namePattern);
        }
        while (propertyIterator.hasNext()) {
            Property property = (Property) propertyIterator.next();
            try {
                if (!property.getName().startsWith("jcr:") && !property.getName().startsWith("mgnl:")) { //$NON-NLS-1$ //$NON-NLS-2$
                    nodeDatas.add(getNodeData(property.getName()));
                }
            }
            catch (PathNotFoundException e) {
                log.error("Exception caught", e);
            }
            catch (AccessDeniedException e) {
                // ignore, simply wont add content in a list
            }
        }
        return nodeDatas;
    }


    public boolean hasContent(String name) throws RepositoryException {
        return this.node.hasNode(name);
    }

    public String getHandle() {
        try {
            return this.node.getPath();
        }
        catch (RepositoryException e) {
            log.error("Failed to get handle: " + e.getMessage(), e); //$NON-NLS-1$
            return StringUtils.EMPTY;
        }
    }

    public Content getParent() throws PathNotFoundException, RepositoryException, AccessDeniedException {
        return (new DefaultContent(this.node.getParent(), this.hierarchyManager));
    }

    public Content getAncestor(int level) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        if (level > this.getLevel()) {
            throw new PathNotFoundException();
        }
        return (new DefaultContent(this.node.getAncestor(level), this.hierarchyManager));
    }

    public Collection<Content> getAncestors() throws PathNotFoundException, RepositoryException {
        List<Content> allAncestors = new ArrayList<Content>();
        int level = this.getLevel();
        while (level != 0) {
            try {
                allAncestors.add(getAncestor(--level));
            }
            catch (AccessDeniedException e) {
                // valid
            }
        }
        return allAncestors;
    }

    public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.node.getDepth(); //$NON-NLS-1$
    }

    public void orderBefore(String srcName, String beforeName) throws RepositoryException {
        this.node.orderBefore(srcName, beforeName);
    }

    public int getIndex() throws RepositoryException {
        return this.node.getIndex();
    }

    public Node getJCRNode() {
        return this.node;
    }

    public boolean isNodeType(String type) {
        return isNodeType(this.node, type);
    }

    /**
     * private Helper method to evaluate primary node type of the given node.
     * @param node
     * @param type
     */
    protected boolean isNodeType(Node node, String type) {
        try {
            final String actualType = node.getProperty(ItemType.JCR_PRIMARY_TYPE).getString();
            // if the node is frozen, and we're not looking specifically for frozen nodes, then we compare with the original node type
            if (ItemType.NT_FROZENNODE.equals(actualType) && !(ItemType.NT_FROZENNODE.equals(type))) {
                final Property p = node.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE);
                final String s = p.getString();
                return s.equalsIgnoreCase(type);
            } else {
                return node.isNodeType(type);
            }
        }
        catch (RepositoryException re) {
            log.error(re.getMessage());
            log.debug(re.getMessage(), re);
            return false;
        }
    }

    public NodeType getNodeType() throws RepositoryException {
        return this.node.getPrimaryNodeType();
    }

    public String getNodeTypeName() throws RepositoryException {

        if (this.node.hasProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE)) {
            return this.node.getProperty(ItemType.JCR_FROZEN_PRIMARY_TYPE).getString();
        }
        return this.node.getProperty(ItemType.JCR_PRIMARY_TYPE).getString();
    }

    public ItemType getItemType() throws RepositoryException {
        return new ItemType(getNodeTypeName());
    }

    public void restore(String versionName, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), this.getHandle(), Permission.WRITE);
        Version version = this.getVersionHistory().getVersion(versionName);
        this.restore(version, removeExisting);
    }

    public void restore(Version version, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), this.getHandle(), Permission.WRITE);
        VersionManager.getInstance().restore(this, version, removeExisting);
    }

    public void restore(Version version, String relPath, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Not implemented in 3.0 Beta");
    }

    public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException, UnsupportedRepositoryOperationException, RepositoryException {
        this.node.restoreByLabel(versionLabel, removeExisting);
        throw new UnsupportedRepositoryOperationException("Not implemented in 3.0 Beta");
    }

    public Version addVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().addVersion(this);
    }

    public Version addVersion(Rule rule) throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().addVersion(this, rule);
    }

    /**
     * Returns true if this node is either
     * <ul>
     * <li/>versionable and currently checked-out, <li/>non-versionable and its nearest versionable ancestor is
     * checked-out or <li/>non-versionable and it has no versionable ancestor.
     * </ul>
     * Returns false if this node is either
     * <ul>
     * <li/>versionable and currently checked-in or <li/>non-versionable and its nearest versionable ancestor is
     * checked-in.
     * </ul>
     * @return true if the node is checked out
     * @throws RepositoryException
     */
    protected boolean isCheckedOut() throws RepositoryException {
        return this.node.isCheckedOut();
    }

    public boolean isModified() {
        return this.node.isModified();
    }

    public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().getVersionHistory(this);
    }

    public VersionIterator getAllVersions() throws UnsupportedRepositoryOperationException, RepositoryException {
        return VersionManager.getInstance().getAllVersions(this);
    }

    public ContentVersion getBaseVersion() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new ContentVersion(VersionManager.getInstance().getBaseVersion(this), this);
    }

    public ContentVersion getVersionedContent(Version version) throws RepositoryException {
        return new ContentVersion(version, this);
    }

    public ContentVersion getVersionedContent(String versionName) throws RepositoryException {
        return new ContentVersion(VersionManager.getInstance().getVersion(this, versionName), this);
    }

    public void removeVersionHistory() throws AccessDeniedException, RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(node.getPath()), Permission.WRITE);
        VersionManager.getInstance().removeVersionHistory(this.node.getUUID());
    }

    public void save() throws RepositoryException {
        this.node.save();
    }

    public void delete() throws RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(this.node.getPath()), Permission.REMOVE);
        String nodePath = Path.getAbsolutePath(this.node.getPath());
        ItemType nodeType = this.getItemType();
        this.node.remove();
        AuditLoggingUtil.log( AuditLoggingUtil.ACTION_DELETE, hierarchyManager.getName(), nodeType, nodePath);
    }


    public void refresh(boolean keepChanges) throws RepositoryException {
        this.node.refresh(keepChanges);
    }

    public String getUUID() {
        try {
            return this.node.getUUID();
        }
        catch (UnsupportedOperationException e) {
            log.error(e.getMessage());
        }
        catch (RepositoryException re) {
            log.error("Exception caught", re);
        }
        return StringUtils.EMPTY;
    }

    public void addMixin(String type) throws RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(this.node.getPath()), Permission.SET);
        if (this.node.canAddMixin(type)) {
            this.node.addMixin(type);
        }
        else {
            log.error("Node - " + this.node.getPath() + " does not allow mixin type - " + type); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public void removeMixin(String type) throws RepositoryException {
        Access.isGranted(this.hierarchyManager.getAccessManager(), Path.getAbsolutePath(this.node.getPath()), Permission.SET);
        this.node.removeMixin(type);
    }

    public NodeType[] getMixinNodeTypes() throws RepositoryException {
        return this.node.getMixinNodeTypes();
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped) throws LockException, RepositoryException {
        return this.node.lock(isDeep, isSessionScoped);
    }

    public Lock lock(boolean isDeep, boolean isSessionScoped, long yieldFor) throws LockException, RepositoryException {
        long finalTime = System.currentTimeMillis() + yieldFor;
        LockException lockException = null;
        while (System.currentTimeMillis() <= finalTime) {
            try {
                return this.node.lock(isDeep, isSessionScoped);
            }
            catch (LockException e) {
                // its not an exception yet, still got time
                lockException = e;
            }
            Thread.yield();
        }
        // could not get lock
        throw lockException;
    }

    public Lock getLock() throws LockException, RepositoryException {
        return this.node.getLock();
    }

    public void unlock() throws LockException, RepositoryException {
        this.node.unlock();
    }

    public boolean holdsLock() throws RepositoryException {
        return this.node.holdsLock();
    }

    public boolean isLocked() throws RepositoryException {
        return this.node.isLocked();
    }

    public boolean hasMetaData() {
        try {
            return this.node.hasNode("MetaData");
        }
        catch (RepositoryException re) {
            log.debug(re.getMessage(), re);
        }
        return false;
    }

}
