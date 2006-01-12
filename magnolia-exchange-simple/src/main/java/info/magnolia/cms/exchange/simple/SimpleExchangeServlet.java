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
package info.magnolia.cms.exchange.simple;

import org.apache.log4j.Logger;
import org.apache.commons.io.IOUtils;
import org.jdom.input.SAXBuilder;
import org.jdom.Element;
import org.doomdark.uuid.UUIDGenerator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.jcr.*;
import javax.jcr.lock.LockException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import info.magnolia.cms.beans.config.ConfigLoader;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.security.Listener;
import info.magnolia.cms.security.Authenticator;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.SessionAccessControl;
import info.magnolia.cms.core.*;
import info.magnolia.cms.util.Resource;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.util.Rule;
import info.magnolia.cms.util.RuleBasedContentFilter;

/**
 *
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class SimpleExchangeServlet extends HttpServlet {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger
     * */
    private static Logger log = Logger.getLogger(SimpleExchangeServlet.class);

    /**
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String statusMessage = "";
        String status = "";
        try {
            applyLock(request);
            receive(request);
            // remove cached files if successful
            CacheHandler.flushCache();
            status = SimpleSyndicator.ACTIVATION_SUCCESSFUL;
        } catch (OutOfMemoryError e) {
            Runtime rt = Runtime.getRuntime();
            log.error("---------\nOutOfMemoryError caught during activation. Total memory = " //$NON-NLS-1$
                + rt.totalMemory() + ", free memory = " //$NON-NLS-1$
                + rt.freeMemory() + "\n---------"); //$NON-NLS-1$
            statusMessage = e.getMessage();
            status = SimpleSyndicator.ACTIVATION_FAILED;
        } catch(PathNotFoundException e) {
            log.error(e.getMessage(), e);
            statusMessage = "Parent not found (not yet activated): " + e.getMessage();
            status = SimpleSyndicator.ACTIVATION_FAILED;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            statusMessage = e.getMessage();
            status = SimpleSyndicator.ACTIVATION_FAILED;
        } finally {
            cleanUp(request);
            response.setHeader(SimpleSyndicator.ACTIVATION_ATTRIBUTE_STATUS, status);
            response.setHeader(SimpleSyndicator.ACTIVATION_ATTRIBUTE_MESSAGE, statusMessage);
        }

    }

    /**
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * handle activate or deactivate request
     * @param request
     * @throws Exception if fails to update
     * */
    private synchronized void receive(HttpServletRequest request)
            throws Exception {
        String action = request.getHeader(SimpleSyndicator.ACTION);
        if (action.equalsIgnoreCase(SimpleSyndicator.ACTIVATE)) {
            update(request);
        } else if (action.equalsIgnoreCase(SimpleSyndicator.DE_ACTIVATE)) {
            remove(request);
        } else {
            throw new UnsupportedOperationException("Method not supported : "+action);
        }
        // Everything went well
        log.info("Activation succeeded");
    }

    /**
     * handle update (activate) request
     * @param request
     * @throws Exception if fails to update
     * */
    private synchronized void update(HttpServletRequest request)
            throws Exception {
        validateRequest(request);
        MultipartForm data = Resource.getPostedForm(request);
        if (null != data) {
            String parentPath = request.getHeader(SimpleSyndicator.PARENT_PATH);
            String resourceFileName = request.getHeader(SimpleSyndicator.RESOURCE_MAPPING_FILE);
            HierarchyManager hm = getHierarchyManager(request);
            Document resourceDocument = data.getDocument(resourceFileName);
            SAXBuilder builder = new SAXBuilder();
            InputStream documentInputStream = resourceDocument.getStream();
            org.jdom.Document jdomDocument = builder.build(documentInputStream);
            IOUtils.closeQuietly(documentInputStream);
            Element topContentElement =
                    jdomDocument.getRootElement().getChild(SimpleSyndicator.RESOURCE_MAPPING_FILE_ELEMENT);
            String newPath = "";
            if (parentPath.equals("/"))
                newPath =
                        parentPath+topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE);
            else
                newPath =
                        parentPath+"/"+topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE);
            // lock this hierarchy
            if (hm.isExist(newPath)) {
                String ruleString = request.getHeader(SimpleSyndicator.CONTENT_FILTER_RULE);
                Rule rule = new Rule(ruleString,",");
                RuleBasedContentFilter filter = new RuleBasedContentFilter(rule);
                Content content = hm.getContent(newPath);
                // remove all child nodes
                this.removeChildren(content, filter);
                // import all child nodes
                this.importOnExisting(topContentElement, data, hm, content);
            } else {
                importFresh(topContentElement, data, hm, parentPath);
            }
        }
    }

    /**
     * Copy all properties from source to destination
     * @param source
     * @param destination
     * */
    private synchronized void copyProperties(Content source, Content destination) throws RepositoryException {
        // first remove all existing properties at the destination
        // will be different with incremental activation
        Iterator nodeDataIterator = destination.getNodeDataCollection().iterator();
        while (nodeDataIterator.hasNext()) {
            NodeData nodeData = (NodeData) nodeDataIterator.next();
            nodeData.delete();
        }
        // copy all properties
        nodeDataIterator = source.getNodeDataCollection().iterator();
        while (nodeDataIterator.hasNext()) {
            NodeData nodeData = (NodeData) nodeDataIterator.next();
            destination.createNodeData(nodeData.getName(), nodeData.getValue());
        }
    }

    /**
     * remove children
     * @param content whose children to be deleted
     * @param filter content filter
     * */
    private synchronized void removeChildren(Content content, Content.ContentFilter filter) {
        Iterator children = content.getChildren(filter).iterator();
        // remove sub nodes using the same filter used by the sender to collect
        // this will make sure there is no existing nodes of the same type
        while (children.hasNext()) {
            Content child = (Content) children.next();
            try {
                child.delete();
            } catch (Exception e) {
                log.error("Failed to remove "+child.getHandle()+" | "+e.getMessage());
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
     * */
    private synchronized void importFresh(Element topContentElement,
                                          MultipartForm data,
                                          HierarchyManager hierarchyManager,
                                          String parentPath)
            throws ExchangeException, RepositoryException {
        try {
            importResource(data,
                    topContentElement,
                    hierarchyManager.getWorkspace().getSession(),
                    parentPath);
            hierarchyManager.save();
        } catch (Exception e) {
            hierarchyManager.refresh(false); // revert all transient changes made in this session till now.
            log.error(e);
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
     * */
    private synchronized void importOnExisting(Element topContentElement,
                                               MultipartForm data,
                                               HierarchyManager hierarchyManager,
                                               Content existingContent)
            throws ExchangeException, RepositoryException {
        Iterator fileListIterator =
                topContentElement.getChildren(SimpleSyndicator.RESOURCE_MAPPING_FILE_ELEMENT).iterator();
        String uuid = UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
        String transientStore = "/" + uuid;
        try {
            while (fileListIterator.hasNext()) {
                Element fileElement = (Element) fileListIterator.next();
                importResource(data,
                        fileElement,
                        hierarchyManager.getWorkspace().getSession(),
                        existingContent.getHandle());
            }
            // use temporary transient store to extract top level node and copy properties
            hierarchyManager.createContent("/",uuid,ItemType.CONTENTNODE.toString());
            String fileName = topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_ID_ATTRIBUTE);
            GZIPInputStream inputStream = new GZIPInputStream(data.getDocument(fileName).getStream());
            hierarchyManager.getWorkspace().getSession().importXML(transientStore,
                    inputStream,
                    ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
            IOUtils.closeQuietly(inputStream);
            Content tmpContent = hierarchyManager.getContent(transientStore
                    +"/"
                    +topContentElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE));
            copyProperties(tmpContent, existingContent);
            hierarchyManager.delete(transientStore);
            hierarchyManager.save();
        } catch (Exception e) {
            hierarchyManager.refresh(false); // revert all transient changes made in this session till now.
            log.error(e);
            throw new ExchangeException("Activation failed | " + e.getMessage());
        }
    }
    
    /**
     * import documents
     * @param data as sent
     * @param resourceElement parent file element
     * @param jcrSession
     * @param parentPath
     * @throws Exception
     * */
    private synchronized void importResource(MultipartForm data, 
                                             Element resourceElement, 
                                             Session jcrSession,
                                             String parentPath) throws Exception {

        String name = resourceElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_NAME_ATTRIBUTE);
        String fileName = resourceElement.getAttributeValue(SimpleSyndicator.RESOURCE_MAPPING_ID_ATTRIBUTE);
        // do actual import
        GZIPInputStream inputStream = new GZIPInputStream(data.getDocument(fileName).getStream());
        jcrSession.importXML(parentPath,
                inputStream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        IOUtils.closeQuietly(inputStream);
        Iterator fileListIterator = resourceElement.getChildren(SimpleSyndicator.RESOURCE_MAPPING_FILE_ELEMENT).iterator();
        // parent path
        if (parentPath.equals("/")) {
            parentPath = ""; // remove / if its a root
        }
        parentPath += ("/"+name);
        while (fileListIterator.hasNext()) {
            Element fileElement = (Element) fileListIterator.next();
            importResource(data, fileElement, jcrSession, parentPath);
        }
    }

    /**
     * handle remove (de-activate) request
     * @param request
     * @throws Exception if fails to update
     * */
    private synchronized void remove(HttpServletRequest request)
            throws Exception {
        validateRequest(request);
        String path = request.getHeader(SimpleSyndicator.PATH);
        if (log.isDebugEnabled()) {
            log.debug("Exchange : remove request received for " + path);
        }
        HierarchyManager hm = getHierarchyManager(request);
        try {
            hm.delete(path);
            hm.save();
        } catch (PathNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to delete node " + path + ": " + e.getMessage());
            }
        }
    }

    /**
     * Check if the request is valid
     * @param request
     * @throws AccessDeniedException
     * */
    private void validateRequest(HttpServletRequest request) throws AccessDeniedException {
        if (ConfigLoader.isConfigured() && (!Listener.isAllowed(request) || !Authenticator.authenticate(request))) {
            throw new AccessDeniedException("Either server not configured or user is not valid");
        }
    }

    /**
     * get hierarchy manager
     * @param request
     * */
    private HierarchyManager getHierarchyManager(HttpServletRequest request) {
        String repositoryName = request.getHeader(SimpleSyndicator.REPOSITORY_NAME);
        String workspaceName = request.getHeader(SimpleSyndicator.WORKSPACE_NAME);
        if (ConfigLoader.isConfigured()) {
            return SessionAccessControl.getHierarchyManager(request, repositoryName, workspaceName);
        }
        else {
            return ContentRepository.getHierarchyManager(repositoryName, workspaceName);
        }
    }

    /**
     * cleans temporary store and removes any locks set
     * @param request
     * */
    private void cleanUp(HttpServletRequest request) {
        MultipartForm data = Resource.getPostedForm(request);
        if (null != data) {
            Iterator keys = data.getDocuments().keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                data.getDocument(key).delete();
            }
        }
        String action = request.getHeader(SimpleSyndicator.ACTION);
        String path = "";
        if (action.equalsIgnoreCase(SimpleSyndicator.ACTIVATE)) {
            path = request.getHeader(SimpleSyndicator.PARENT_PATH);
        } else if (action.equalsIgnoreCase(SimpleSyndicator.DE_ACTIVATE)) {
            path = request.getHeader(SimpleSyndicator.PATH);
        }
        try {
            Content content = getHierarchyManager(request).getContent(path);
            if (content.isLocked()) {
                content.unlock();
            }
        } catch (LockException le) {
            // either repository does not support locking OR this node never locked
            log.debug(le.getMessage());
        } catch (RepositoryException re) {
            // should never come here
            log.debug(re);
        }
    }

    /**
     * apply lock
     * @param request
     * */
    private void applyLock(HttpServletRequest request) throws ExchangeException {
        String action = request.getHeader(SimpleSyndicator.ACTION);
        String path = "";
        if (action.equalsIgnoreCase(SimpleSyndicator.ACTIVATE)) {
            path = request.getHeader(SimpleSyndicator.PARENT_PATH);
        } else if (action.equalsIgnoreCase(SimpleSyndicator.DE_ACTIVATE)) {
            path = request.getHeader(SimpleSyndicator.PATH);
        }
        try {
            Content content = getHierarchyManager(request).getContent(path);
            if (content.isLocked()) {
                throw new ExchangeException("Operation not permitted, "+path+" is locked");
            } else {
                // get a new deep lock
                content.lock(true, true);
            }
        } catch (LockException le) {
            // either repository does not support locking OR this node never locked
            log.debug(le.getMessage());
        } catch (RepositoryException re) {
            // should never come here
            log.debug(re);
        }
    }

}
