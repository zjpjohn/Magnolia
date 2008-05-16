/**
 * This file Copyright (c) 2003-2008 Magnolia International
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
package info.magnolia.module.exchangesimple;

import org.apache.commons.codec.binary.Base64;
import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Access;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.filters.AbstractMgnlFilter;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.util.Resource;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.safehaus.uuid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oreilly.servlet.Base64Decoder;

/**
 * @author Sameer Charles
 * $Id$
 */
public class ReceiveFilter extends AbstractMgnlFilter {

    private static final Logger log = LoggerFactory.getLogger(ReceiveFilter.class);

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String statusMessage = "";
        String status = "";
        try {
            applyLock(request);
            receive(request);
            status = SimpleSyndicator.ACTIVATION_SUCCESSFUL;
        }
        catch (OutOfMemoryError e) {
            Runtime rt = Runtime.getRuntime();
            log.error("---------\nOutOfMemoryError caught during activation. Total memory = " //$NON-NLS-1$
                + rt.totalMemory()
                + ", free memory = " //$NON-NLS-1$
                + rt.freeMemory()
                + "\n---------"); //$NON-NLS-1$
            statusMessage = e.getMessage();
            status = SimpleSyndicator.ACTIVATION_FAILED;
        }
        catch (PathNotFoundException e) {
            log.error(e.getMessage(), e);
            statusMessage = "Parent not found (not yet activated): " + e.getMessage();
            status = SimpleSyndicator.ACTIVATION_FAILED;
        }
        catch (Throwable e) {
            log.error(e.getMessage(), e);
            statusMessage = e.getMessage();
            status = SimpleSyndicator.ACTIVATION_FAILED;
        }
        finally {
            cleanUp(request);
            response.setHeader(SimpleSyndicator.ACTIVATION_ATTRIBUTE_STATUS, status);
            response.setHeader(SimpleSyndicator.ACTIVATION_ATTRIBUTE_MESSAGE, statusMessage);
        }
    }

    /**
      * handle activate or deactivate request
      * @param request
      * @throws Exception if fails to update
      */
     protected synchronized void receive(HttpServletRequest request) throws Exception {
         String action = request.getHeader(SimpleSyndicator.ACTION);

         // get the user who authorized this request.
         String authorization = request.getHeader(SimpleSyndicator.AUTHORIZATION);
         if (StringUtils.isEmpty(authorization)) {
             authorization = request.getParameter(SimpleSyndicator.AUTH_USER);
         } else {
             log.error("AUTH:" +authorization);
            authorization = new String(Base64.decodeBase64(authorization.substring(6).getBytes())); //Basic uname:pwd
            authorization = authorization.substring(0, authorization.indexOf(":"));
         }

         String webapp = SystemProperty.getProperty(SystemProperty.MAGNOLIA_WEBAPP);
         if (action.equalsIgnoreCase(SimpleSyndicator.ACTIVATE)) {
             String name = update(request);
             // Everything went well
             log.info("User {} successfuly activated {} on {}.", new Object[]{authorization, name, webapp});
         }
         else if (action.equalsIgnoreCase(SimpleSyndicator.DEACTIVATE)) {
             String name = remove(request);
             // Everything went well
             log.info("User {} succeessfuly deactivated {} on {}.", new Object[] {authorization, name, webapp});
         }
         else {
             throw new UnsupportedOperationException("Method not supported : " + action);
         }
     }

     /**
      * handle update (activate) request
      * @param request
      * @throws Exception if fails to update
      */
     protected synchronized String update(HttpServletRequest request) throws Exception {
         MultipartForm data = Resource.getPostedForm();
         String name = null;
         if (null != data) {
             String parentPath = this.getParentPath(request);
             String resourceFileName = request.getHeader(SimpleSyndicator.RESOURCE_MAPPING_FILE);
             HierarchyManager hm = getHierarchyManager(request);
             Document resourceDocument = data.getDocument(resourceFileName);
             SAXBuilder builder = new SAXBuilder();
             InputStream documentInputStream = resourceDocument.getStream();
             org.jdom.Document jdomDocument = builder.build(documentInputStream);
             IOUtils.closeQuietly(documentInputStream);
             Element rootElement = jdomDocument.getRootElement();
             Element topContentElement = rootElement.getChild(SimpleSyndicator.RESOURCE_MAPPING_FILE_ELEMENT);
             try {
                 String uuid = topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_UUID_ATTRIBUTE);
                 Content content = hm.getContentByUUID(uuid);
                 String ruleString = request.getHeader(SimpleSyndicator.CONTENT_FILTER_RULE);
                 Rule rule = new Rule(ruleString, ",");
                 RuleBasedContentFilter filter = new RuleBasedContentFilter(rule);
                 // remove all child nodes
                 this.removeChildren(content, filter);
                 // import all child nodes
                 this.importOnExisting(topContentElement, data, hm, content);
             }
             catch (ItemNotFoundException e) {
                 importFresh(topContentElement, data, hm, parentPath);
             }

             // order imported node
             name = topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE);
             Content parent = hm.getContent(parentPath);
             List siblings = rootElement.getChild(SimpleSyndicator.SIBLINGS_ROOT_ELEMENT)
                     .getChildren(SimpleSyndicator.SIBLINGS_ELEMENT);
             Iterator siblingsIterator = siblings.iterator();
             while (siblingsIterator.hasNext()) {
                 Element sibling = (Element) siblingsIterator.next();
                 // check for existence and order
                 try {
                     String siblingUUID = sibling.getAttributeValue(SimpleSyndicator.SIBLING_UUID);
                     // be compatible with 3.0 (MAGNOLIA-2016)
                     siblingUUID = StringUtils.defaultIfEmpty(siblingUUID, sibling.getAttributeValue(SimpleSyndicator.DEPRECATED_SIBLING_UUID));
                     Content beforeContent = hm.getContentByUUID(siblingUUID);
                     parent.orderBefore(name, beforeContent.getName());
                     parent.save();
                     break;
                 } catch (ItemNotFoundException e) {
                     // ignore
                 } catch (RepositoryException re) {
                     log.warn("Failed to order node");
                     log.debug("Failed to order node", re);
                 }
             }
         }
         return name;
     }

     /**
      * Copy all properties from source to destination (by cleaning the old properties).
      * @param source the content node to be copied
      * @param destination the destination node
      */
     protected synchronized void copyProperties(Content source, Content destination) throws RepositoryException {
         // first remove all existing properties at the destination
         // will be different with incremental activation
         Iterator nodeDataIterator = destination.getNodeDataCollection().iterator();
         while (nodeDataIterator.hasNext()) {
             NodeData nodeData = (NodeData) nodeDataIterator.next();
             // Ignore binary types, since these are sub nodes and already taken care of while
             // importing sub resources
             if (nodeData.getType() != PropertyType.BINARY) {
                 nodeData.delete();
             }
         }

         // copy all properties
         Node destinationNode = destination.getJCRNode();
         nodeDataIterator = source.getNodeDataCollection().iterator();
         while (nodeDataIterator.hasNext()) {
             NodeData nodeData = (NodeData) nodeDataIterator.next();
             Property property = nodeData.getJCRProperty();
             if (property.getDefinition().isMultiple()) {
                 if (destination.isGranted(Permission.WRITE)) {
                     destinationNode.setProperty(nodeData.getName(), property.getValues());
                 }
                 else {
                     throw new AccessDeniedException(
                         "User not allowed to " + Permission.PERMISSION_NAME_WRITE + " at [" + nodeData.getHandle() + "]"); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
                 }
             }
             else {
                 destination.createNodeData(nodeData.getName(), nodeData.getValue());
             }
         }
     }

     /**
      * remove children
      * @param content whose children to be deleted
      * @param filter content filter
      */
     protected synchronized void removeChildren(Content content, Content.ContentFilter filter) {
         Iterator children = content.getChildren(filter).iterator();
         // remove sub nodes using the same filter used by the sender to collect
         // this will make sure there is no existing nodes of the same type
         while (children.hasNext()) {
             Content child = (Content) children.next();
             try {
                 child.delete();
             }
             catch (Exception e) {
                 log.error("Failed to remove " + child.getHandle() + " | " + e.getMessage());
             }
         }
     }

     /**
      * import on non existing tree
      * @param topContentElement
      * @param data
      * @param hierarchyManager
      * @param parentPath
      * @throws ExchangeException
      * @throws RepositoryException
      */
     protected synchronized void importFresh(Element topContentElement, MultipartForm data,
         HierarchyManager hierarchyManager, String parentPath) throws ExchangeException, RepositoryException {
         try {
             importResource(data, topContentElement, hierarchyManager, parentPath);
             hierarchyManager.save();
         }
         catch (Exception e) {
             hierarchyManager.refresh(false); // revert all transient changes made in this session till now.
             log.error("Exception caught", e);
             throw new ExchangeException("Activation failed | " + e.getMessage());
         }
     }

     /**
      * import on existing content, making sure that content which is not sent stays as is
      * @param topContentElement
      * @param data
      * @param hierarchyManager
      * @param existingContent
      * @throws ExchangeException
      * @throws RepositoryException
      */
     protected synchronized void importOnExisting(Element topContentElement, MultipartForm data,
         HierarchyManager hierarchyManager, Content existingContent) throws ExchangeException, RepositoryException {
         Iterator fileListIterator = topContentElement
             .getChildren(SimpleSyndicator.RESOURCE_MAPPING_FILE_ELEMENT)
             .iterator();
         String uuid = UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
         String transientStore = existingContent.getHandle() + "/" + uuid;
         try {
             while (fileListIterator.hasNext()) {
                 Element fileElement = (Element) fileListIterator.next();
                 importResource(data, fileElement, hierarchyManager, existingContent.getHandle());
             }
             // use temporary transient store to extract top level node and copy properties
             existingContent.createContent(uuid, ItemType.CONTENTNODE.toString());
             //hierarchyManager.createContent("/", uuid, ItemType.CONTENTNODE.toString());
             String fileName = topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_ID_ATTRIBUTE);
             GZIPInputStream inputStream = new GZIPInputStream(data.getDocument(fileName).getStream());
             hierarchyManager.getWorkspace().getSession().importXML(
                 transientStore,
                 inputStream,
                 ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
             IOUtils.closeQuietly(inputStream);
             StringBuffer newPath = new StringBuffer();
             newPath.append(transientStore);
             newPath.append("/");
             newPath.append(topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE));
             Content tmpContent = hierarchyManager.getContent(newPath.toString());
             copyProperties(tmpContent, existingContent);
             hierarchyManager.delete(transientStore);
             hierarchyManager.save();
         }
         catch (Exception e) {
             hierarchyManager.refresh(false); // revert all transient changes made in this session till now.
             log.error("Exception caught", e);
             throw new ExchangeException("Activation failed | " + e.getMessage());
         }
     }

     /**
      * import documents
      * @param data as sent
      * @param resourceElement parent file element
      * @param hm
      * @param parentPath
      * @throws Exception
      */
     protected synchronized void importResource(MultipartForm data, Element resourceElement, HierarchyManager hm,
         String parentPath) throws Exception {

         // throws an excpetion in case you don't have the permission
         Access.isGranted(hm.getAccessManager(), parentPath, Permission.WRITE);

         String name = resourceElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE);
         String fileName = resourceElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_ID_ATTRIBUTE);
         // do actual import
         GZIPInputStream inputStream = new GZIPInputStream(data.getDocument(fileName).getStream());
         hm.getWorkspace().getSession().importXML(
             parentPath,
             inputStream,
             ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
         IOUtils.closeQuietly(inputStream);
         Iterator fileListIterator = resourceElement
             .getChildren(SimpleSyndicator.RESOURCE_MAPPING_FILE_ELEMENT)
             .iterator();
         // parent path
         if (parentPath.equals("/")) {
             parentPath = ""; // remove / if its a root
         }
         parentPath += ("/" + name);
         while (fileListIterator.hasNext()) {
             Element fileElement = (Element) fileListIterator.next();
             importResource(data, fileElement, hm, parentPath);
         }
     }

     /**
      * handle remove (de-activate) request
      * @param request
      * @throws Exception if fails to update
      */
     protected synchronized String remove(HttpServletRequest request) throws Exception {
         HierarchyManager hm = getHierarchyManager(request);
         String handle = null;
         try {
             Content node = this.getNode(request);
             handle = node.getHandle();
             hm.delete(handle);
             hm.save();
         }
         catch (ItemNotFoundException e) {
             if (log.isDebugEnabled()) {
                 log.debug("Unable to delete node", e);
             }
         }
         return handle;
     }

     /**
      * get hierarchy manager
      * @param request
      * @throws ExchangeException
      */
     protected HierarchyManager getHierarchyManager(HttpServletRequest request) throws ExchangeException {
         String repositoryName = request.getHeader(SimpleSyndicator.REPOSITORY_NAME);
         String workspaceName = request.getHeader(SimpleSyndicator.WORKSPACE_NAME);

         if (StringUtils.isEmpty(repositoryName) || StringUtils.isEmpty(workspaceName)) {
             throw new ExchangeException("Repository or workspace name not sent, unable to activate.");
         }

         if (ConfigLoader.isConfigured()) {
             return MgnlContext.getHierarchyManager(repositoryName, workspaceName);
         }

         return ContentRepository.getHierarchyManager(repositoryName, workspaceName);

     }

     /**
      * cleans temporary store and removes any locks set
      * @param request
      */
     protected void cleanUp(HttpServletRequest request) {
         if (SimpleSyndicator.ACTIVATE.equalsIgnoreCase(request.getHeader(SimpleSyndicator.ACTION))) {
             MultipartForm data = Resource.getPostedForm();
             if (null != data) {
                 Iterator keys = data.getDocuments().keySet().iterator();
                 while (keys.hasNext()) {
                     String key = (String) keys.next();
                     data.getDocument(key).delete();
                 }
             }
             try {
                 Content content = this.getNode(request);
                 if (content.isLocked()) {
                     content.unlock();
                 }
             }
             catch (LockException le) {
                 // either repository does not support locking OR this node never locked
                 if (log.isDebugEnabled()) {
                     log.debug(le.getMessage());
                 }
             }
             catch (RepositoryException re) {
                 // should never come here
                 log.warn("Exception caught", re);
             }
             catch (ExchangeException e) {
                 // should never come here
                 log.warn("Exception caught", e);
             }
         }

         try {
             getHierarchyManager(request).getWorkspace().getSession().logout();
             HttpSession httpSession = request.getSession(false);
             if (httpSession != null) httpSession.invalidate();
         } catch (Throwable t) {
             // its only a test so just dump
             log.error("failed to invalidate session", t);
         }
     }

     /**
      * apply lock
      * @param request
      */
     protected void applyLock(HttpServletRequest request) throws ExchangeException {
         try {
             Content content = this.getNode(request);
             if (content.isLocked()) {
                 throw new ExchangeException("Operation not permitted, " + content.getHandle() + " is locked");
             }
             // get a new deep lock
             content.lock(true, true);
         } catch (LockException le) {
             // either repository does not support locking OR this node never locked
             if (log.isDebugEnabled()) {
                 log.debug(le.getMessage());
             }
         } catch (RepositoryException re) {
             // should never come here
             log.warn("Exception caught", re);
         }
     }

     protected Content getNode(HttpServletRequest request)
             throws ExchangeException, RepositoryException {

         String action = request.getHeader(SimpleSyndicator.ACTION);
         if (SimpleSyndicator.ACTIVATE.equalsIgnoreCase(action)) {
             return this.getHierarchyManager(request).getContent(this.getParentPath(request));
         }
         else if (SimpleSyndicator.DEACTIVATE.equalsIgnoreCase(action)) {
             if(request.getHeader(SimpleSyndicator.NODE_UUID) != null){
                 return this.getHierarchyManager(request).getContentByUUID(request.getHeader(SimpleSyndicator.NODE_UUID));
             }
             // 3.0 protocol
             else {
                 return this.getHierarchyManager(request).getContent(request.getHeader(SimpleSyndicator.PATH));
             }
         }
         throw new ExchangeException("Node not found");
     }

     protected String getParentPath(HttpServletRequest request) {
         String parentPath = request.getHeader(SimpleSyndicator.PARENT_PATH);
         if (StringUtils.isNotEmpty(parentPath)) {
             return parentPath;
         }
         return "";
     }



}
