/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */




package info.magnolia.cms.beans.config;

import org.apache.log4j.Logger;
import org.apache.slide.jcr.core.AccessManagerImpl;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.InvalidConfigException;
import info.magnolia.cms.util.regex.RegexWildcardPattern;
import info.magnolia.cms.beans.runtime.Permission;

import javax.jcr.*;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 * User: Sameer Charles
 * Date: Mar 29, 2004
 * Time: 11:39:00 AM
 */


/**
 * Initialise all configured modules
 *
 *
 * */



public class ModuleLoader {



    private static Logger log = Logger.getLogger(ModuleLoader.class);


    /**
     * magnolia module specific keywords
     * */
    private static final String CONFIG_PAGE = "modules";
    private static final String CONFIG_NODE_REGISTER = "Register";
    private static final String CONFIG_NODE_VIRTUAL_MAPPING = "VirtualURIMapping";
    private static final String CONFIG_NODE_LOCAL_STORE = "Config";






    /**
     * todo fix this with proper JCR implementation
     * */
    private static SimpleCredentials simpleCredentials;




    protected static void init()
            throws ClassNotFoundException,
            InvalidConfigException {
        log.info("Loading modules");
        setSudoCredentials();
        try {
            HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
            Content startPage = hm.getPage(CONFIG_PAGE);
            init(startPage);
            log.info("Finished loading modules");
        } catch (RepositoryException re) {
            log.fatal("Failed to initialize module loader");
            log.fatal(re.getMessage(), re);
        }
    }



    /**
     *
     * @param startPage
     * */
    private static void init(Content startPage)
            throws ClassNotFoundException,
            InvalidConfigException {
        Iterator modules = startPage.getChildren().iterator();
        while (modules.hasNext()) {
            Content module = (Content)modules.next();
            try {
                log.info("Initializing module - "+module.getName());
                load(module);
                VirtualMap.getInstance().update(CONFIG_PAGE+"/"+module.getName()+"/"+CONFIG_NODE_VIRTUAL_MAPPING);
                log.info("Module : "+module.getName() +" initialized");
            } catch (RepositoryException re) {
                log.error("Failed to initialize module - "+module.getName());
                log.error(re.getMessage(), re);
            }
        }
    }



    private static void load(Content module)
            throws RepositoryException,
            ClassNotFoundException,
            InvalidConfigException {
        ContentNode moduleConfig = module.getContentNode(CONFIG_NODE_REGISTER);
        ModuleConfig thisModule = new ModuleConfig();
        thisModule.setModuleName(moduleConfig.getNodeData("moduleName").getString());
        thisModule.setModuleDescription(moduleConfig.getNodeData("moduleDescription").getString());
        thisModule.setModuleRepository(getModuleRepository(moduleConfig.getNodeData("repository").getString()));
        try {
            ContentNode sharedRepositories =  moduleConfig.getContentNode("sharedRepositories");
            thisModule.setSharedRepositories(getSharedRepositories(sharedRepositories));
        } catch (PathNotFoundException e) {
            log.info("Module : no shared repository definition found for - "+module.getName());
        }
        thisModule.setInitParameters(getInitParameters(moduleConfig.getContentNode("initParams")));
        /* add local store */
        LocalStore store = LocalStore.getInstance(CONFIG_PAGE+"/"+module.getName()+"/"+CONFIG_NODE_LOCAL_STORE);
        thisModule.setLocalStore(store.getStore());
        try {
            Module moduleClass = (Module) Class.forName(moduleConfig.getNodeData("class").getString()).newInstance();
            moduleClass.init(thisModule);
        } catch(InstantiationException ie) {
            log.fatal("Module : [ "+moduleConfig.getNodeData("moduleName").getString()+" ] failed to load");
            log.fatal(ie.getMessage());
        } catch (IllegalAccessException ae) {
            log.fatal(ae.getMessage());
        }
    }




    private static Hashtable getInitParameters(ContentNode paramList) {
        Hashtable initParams = new Hashtable();
        Iterator initParameters = paramList.getChildren(ItemType.MAGNOLIA_NODE_DATA).iterator();
        while (initParameters.hasNext()) {
            NodeData param = (NodeData) initParameters.next();
            initParams.put(param.getName(), param.getString());
        }
        return initParams;
    }



    private static Session getModuleRepository(String repositoryName)
            throws RepositoryException {
        if (repositoryName==null || (repositoryName.equals("")))
            return null;
        Session moduleRepositoryTicket =
                ContentRepository.getRepository(repositoryName).login(simpleCredentials,null);
        ArrayList acl = new ArrayList();
        Pattern p = Pattern.compile(RegexWildcardPattern.getMultipleCharPattern());
        Permission permission = new Permission();
        permission.setPattern(p);
        permission.setPermissions(Permission.ALL_PERMISSIONS);
        acl.add(permission);
        ((AccessManagerImpl)moduleRepositoryTicket.getWorkspace().getAccessManager()).setUserPermissions(acl);
        return moduleRepositoryTicket;
    }




    private static Hashtable getSharedRepositories(ContentNode sharedRepositoriesNode)
            throws RepositoryException {
        Hashtable sharedRepositories = new Hashtable();
        Iterator repositories = sharedRepositoriesNode.getChildren().iterator();
        ArrayList acl = new ArrayList();
        Pattern p = Pattern.compile(RegexWildcardPattern.getMultipleCharPattern());
        while (repositories.hasNext()) {
            ContentNode repositoryConfig = (ContentNode) repositories.next();
            String id = repositoryConfig.getNodeData("id").getString();
            String repositoryName = repositoryConfig.getNodeData("repository").getString();
            Session ticket =
                    ContentRepository.getRepository(repositoryName).login(simpleCredentials,null);

            Permission permission = new Permission();
            permission.setPattern(p);
            permission.setPermissions(repositoryConfig.getNodeData("permissions").getLong());
            acl.add(permission);
            ((AccessManagerImpl)ticket.getWorkspace().getAccessManager()).setUserPermissions(acl);
            sharedRepositories.put(id,ticket);
        }
        return sharedRepositories;
    }



    private static void setSudoCredentials() {
        simpleCredentials = new SimpleCredentials("ModuleLoader", "".toCharArray());
    }


}
