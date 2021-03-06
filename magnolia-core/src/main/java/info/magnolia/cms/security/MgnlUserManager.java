/**
 * This file Copyright (c) 2003-2012 Magnolia International
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
package info.magnolia.cms.security;

import static info.magnolia.cms.security.SecurityConstants.*;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.security.auth.ACL;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.iterator.FilteringPropertyIterator;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.Query;
import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages the users stored in Magnolia itself.
 */
public class MgnlUserManager extends RepositoryBackedSecurityManager implements UserManager {

    private static final Logger log = LoggerFactory.getLogger(MgnlUserManager.class);

    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_LANGUAGE = "language";
    public static final String PROPERTY_LASTACCESS = "lastaccess";
    public static final String PROPERTY_PASSWORD = "pswd";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_ENABLED = "enabled";

    public static final String NODE_ACLUSERS = "acl_users";

    private String realmName;

    private boolean allowCrossRealmDuplicateNames = false;

    private int maxFailedLoginAttempts;

    private int lockTimePeriod;

    /**
     * There should be no need to instantiate this class except maybe for testing. Manual instantiation might cause manager not to be initialized properly.
     */
    public MgnlUserManager() {
    }

    @Override
    public void setMaxFailedLoginAttempts(int maxFailedLoginAttempts){
        this.maxFailedLoginAttempts = maxFailedLoginAttempts;
    }

    @Override
    public int getMaxFailedLoginAttempts(){
        return maxFailedLoginAttempts;
    }

    @Override
    public int getLockTimePeriod() {
        return lockTimePeriod;
    }

    @Override
    public void setLockTimePeriod(int lockTimePeriod) {
        this.lockTimePeriod = lockTimePeriod;
    }

    @Override
    public User setProperty(final User user, final String propertyName, final Value propertyValue) {
        return MgnlContext.doInSystemContext(new SilentSessionOp<User>(getRepositoryName()) {

            @Override
            public User doExec(Session session) throws RepositoryException {
                String path = ((MgnlUser) user).getPath();
                Node userNode;
                try {
                    userNode = session.getNode(path);
                    // setting value to null would remove existing properties anyway, so no need to create a
                    // not-yet-existing-one first and then set it to null.
                    if(propertyValue != null || PropertyUtil.getPropertyOrNull(userNode, propertyName) != null){
                        if(StringUtils.equals(propertyName, PROPERTY_PASSWORD)){
                            setPasswordProperty(userNode, propertyValue.getString());
                        }
                        else{
                            userNode.setProperty(propertyName, propertyValue);
                            session.save();
                       }
                    }
                }

                catch (RepositoryException e) {
                    session.refresh(false);
                    log.error("Property {} can't be changed. " + e.getMessage(), propertyName);
                    return user;
                }
                return newUserInstance(userNode);
            }
        });
    }

    @Override
    public User setProperty(final User user, final String propertyName, final String propertyValue) {
        return MgnlContext.doInSystemContext(new SilentSessionOp<User>(getRepositoryName()) {

            @Override
            public User doExec(Session session) throws RepositoryException {
                String path = ((MgnlUser) user).getPath();
                Node userNode;
                try {
                    userNode = session.getNode(path);
                    // setting value to null would remove existing properties anyway, so no need to create a
                    // not-yet-existing-one first and then set it to null.
                    if (propertyName != null) {
                        if(StringUtils.equals(propertyName, PROPERTY_PASSWORD)){
                            setPasswordProperty(userNode, propertyValue);
                        }
                        else{
                            userNode.setProperty(propertyName, propertyValue);
                            session.save();
                        }
                    }
                } catch (RepositoryException e) {
                    session.refresh(false);
                    log.error("Property {} can't be changed. " + e.getMessage(), propertyName);
                    return user;
                }
                return newUserInstance(userNode);
            }
        });
    }


    /**
     * TODO : rename to getRealmName and setRealmName (and make sure Content2Bean still sets realmName using the parent's node name).
     * @deprecated since 4.5 use realmName instead
     */
    @Deprecated
    public String getName() {
        return getRealmName();
    }

    /**
     * @deprecated since 4.5 use realmName instead
     */
    @Deprecated
    public void setName(String name) {
        setRealmName(name);
    }

    public void setRealmName(String name) {
        this.realmName = name;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setAllowCrossRealmDuplicateNames(boolean allowCrossRealmDuplicateNames) {
        this.allowCrossRealmDuplicateNames = allowCrossRealmDuplicateNames;
    }

    public boolean isAllowCrossRealmDuplicateNames() {
        return allowCrossRealmDuplicateNames;
    }

    /**
     * Get the user object. Uses a search
     * @param name
     * @return the user object
     */
    @Override
    public User getUser(final String name) {
        try {
            return MgnlContext.doInSystemContext(new JCRSessionOp<User>(getRepositoryName()) {
                @Override
                public User exec(Session session) throws RepositoryException {
                    Node priviledgedUserNode = findPrincipalNode(name, session);
                    return newUserInstance(priviledgedUserNode);
                }
                @Override
                public String toString() {
                    return "retrieve user " + name;
                }
            });
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the user object. Uses a search
     * @param id user identifier
     * @return the user object
     */
    @Override
    public User getUserById(final String id){
        try {
            return MgnlContext.doInSystemContext(new JCRSessionOp<User>(getRepositoryName()) {
                @Override
                public User exec(Session session) throws RepositoryException {
                    Node priviledgedUserNode = session.getNodeByIdentifier(id);
                    return newUserInstance(priviledgedUserNode);
                }
                @Override
                public String toString() {
                    return "retrieve user with id " + id;
                }
            });
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User getUser(Subject subject) throws UnsupportedOperationException {
        // this could be the case if no one is logged in yet
        if (subject == null) {
            log.debug("subject not set.");
            return new DummyUser();
        }

        Set<User> principalSet = subject.getPrincipals(User.class);
        Iterator<User> entityIterator = principalSet.iterator();
        if (!entityIterator.hasNext()) {
            // happens when JCR authentication module set to optional and user doesn't exist in magnolia
            log.debug("user name not contained in principal set.");
            return new DummyUser();
        }
        return entityIterator.next();
    }

    /**
     * Helper method to find a user in a certain realm. Uses JCR Query.
     * @deprecated since 4.5 use findPrincipalNode(java.lang.String, javax.jcr.Session) instead
     */
    @Deprecated
    protected Content findUserNode(String realm, String name) throws RepositoryException {
        // while we could call the other findUserNode method and wrap the output it would be inappropriate as session is not valid outside of the call
        throw new UnsupportedOperationException("Admin session is no longer kept open for unlimited duration of the time, therefore it is not possible to expose node outside of admin session.");
    }

    /**
     * Helper method to find a user in a certain realm. Uses JCR Query.
     */
    @Override
    protected Node findPrincipalNode(String name, Session session) throws RepositoryException {
        String realmName = getRealmName();
        final String where;
        // the all realm searches the repository
        if (Realm.REALM_ALL.getName().equals(realmName)) {
            where = "where name() = '" + name + "'";
        } else {
            // FIXME: DOUBLE CHECK THE QUERY FOR REALMS ... ISDESCENDANTNODE and NAME ....
            where = "where name() = '" + name + "' and isdescendantnode(['/" + realmName + "'])";
            //            where = "where [jcr:path] = '/" + realm + "/" + name + "'"
            //            + " or [jcr:path] like '/" + realm + "/%/" + name + "'";
        }

        final String statement = "select * from [" + NodeTypes.User.NAME + "] " + where;

        Query query = session.getWorkspace().getQueryManager().createQuery(statement, Query.JCR_SQL2);
        NodeIterator iter = query.execute().getNodes();
        Node user = null;
        while (iter.hasNext()) {
            Node node = iter.nextNode();
            if (node.isNodeType(NodeTypes.User.NAME)) {
                user = node;
                break;
            }
        }
        if (iter.hasNext()) {
            log.error("More than one user found with name [{}] in realm [{}]", name, realmName);
        }
        return user;
    }

    protected User getFromRepository(String name) throws RepositoryException {
        final Content node = findUserNode(this.realmName, name);
        if (node == null) {
            log.debug("User not found: [{}]", name);
            return null;
        }

        return newUserInstance(node);
    }

    /**
     * SystemUserManager does this.
     */
    @Override
    public User getSystemUser() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * SystemUserManager does this.
     */
    @Override
    public User getAnonymousUser() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Get all users managed by this user manager.
     */
    @Override
    public Collection<User> getAllUsers() {
        return MgnlContext.doInSystemContext(new SilentSessionOp<Collection<User>>(getRepositoryName()) {

            @Override
            public Collection<User> doExec(Session session) throws RepositoryException {
                List<User> users = new ArrayList<User>();
                Node node = session.getNode("/" + realmName);
                updateUserListWithAllChildren(node, users);
                return users;
            }

            @Override
            public String toString() {
                return "get all users";
            }

        });
    }

    /**
     * Updates collection with all users located under provided node.
     * @throws RepositoryException
     */
    public void updateUserListWithAllChildren(Node node, Collection<User> users) throws RepositoryException{
        NodeIterator nodesIter = node.getNodes();
        Collection<Node> nodes = new HashSet<Node>();
        Collection<Node> folders = new HashSet<Node>();
        while(nodesIter.hasNext()){
            Node newNode = (Node) nodesIter.next();
            if(newNode.isNodeType(NodeTypes.User.NAME)){
                nodes.add(newNode);
            }else if(newNode.isNodeType(NodeTypes.Folder.NAME)){
                folders.add(newNode);
            }
        }

        if(!nodes.isEmpty()){
            for (Node userNode : nodes) {
                users.add(newUserInstance(userNode));
            }
        }
        if(!folders.isEmpty()){
            Iterator<Node> it = folders.iterator();
            while(it.hasNext()){
                updateUserListWithAllChildren(it.next(), users);
            }
        }
    }

    @Override
    public User createUser(final String name, final String pw) {
        return this.createUser(null, name, pw);
    }

    @Override
    public User createUser(final String path, final String name, final String pw) throws UnsupportedOperationException {
        validateUsername(name);
        return MgnlContext.doInSystemContext(new SilentSessionOp<MgnlUser>(getRepositoryName()) {

            @Override
            public MgnlUser doExec(Session session) throws RepositoryException {
                String uPath = path == null ? "/" + getRealmName() : path;
                Node userNode = session.getNode(uPath).addNode(name, NodeTypes.User.NAME);
                userNode.addMixin(JcrConstants.MIX_LOCKABLE);
                userNode.setProperty("name", name);
                setPasswordProperty(userNode, pw);
                userNode.setProperty("language", "en");

                final String handle = userNode.getPath();
                final Node acls = userNode.addNode(NODE_ACLUSERS, NodeTypes.ContentNode.NAME);
                // read only access to the node itself
                Node acl = acls.addNode(Path.getUniqueLabel(session, acls.getPath(), "0"), NodeTypes.ContentNode.NAME);
                acl.setProperty("path", handle);
                acl.setProperty("permissions", Permission.READ);
                // those who had access to their nodes should get access to their own props
                addWrite(handle, PROPERTY_EMAIL, acls);
                addWrite(handle, PROPERTY_LANGUAGE, acls);
                addWrite(handle, PROPERTY_LASTACCESS, acls);
                addWrite(handle, PROPERTY_PASSWORD, acls);
                addWrite(handle, PROPERTY_TITLE, acls);
                session.save();
                return new MgnlUser(userNode.getName(), getRealmName(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_MAP,  NodeUtil.getPathIfPossible(userNode), NodeUtil.getNodeIdentifierIfPossible(userNode));
            }

            @Override
            public String toString() {
                return "create user " + name;
            }
        });
    }

    @Override
    public User changePassword(final User user, final String newPassword) {
        return MgnlContext.doInSystemContext(new SilentSessionOp<User>(getRepositoryName()) {

            @Override
            public User doExec(Session session) throws RepositoryException {
                Node userNode = session.getNode("/" + getRealmName() + "/" + user.getName());
                setPasswordProperty(userNode, newPassword);

                session.save();
                return newUserInstance(userNode);
            }

            @Override
            public String toString() {
                return "change password of user " + user.getName();
            }
        });
    }

    /**
     * @deprecated since 4.5 use {@link #setPasswordProperty(Node, String)} instead
     */
    @Deprecated
    protected void setPasswordProperty(Content userNode, String clearPassword) throws RepositoryException {
        setPasswordProperty(userNode.getJCRNode(), clearPassword);
    }


    protected void setPasswordProperty(Node userNode, String clearPassword) throws RepositoryException {
        userNode.setProperty(PROPERTY_PASSWORD, encodePassword(clearPassword));
    }

    protected String encodePassword(String clearPassword) {
        return SecurityUtil.getBCrypt(clearPassword);
    }

    protected void validateUsername(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException(name + " is not a valid username.");
        }

        User user;
        if (isAllowCrossRealmDuplicateNames()) {
            user = this.getUser(name);
        } else {
            user = Security.getUserManager().getUser(name);
        }
        if (user != null) {
            throw new IllegalArgumentException("User with name " + name + " already exists.");
        }
    }

    protected Content createUserNode(String name) throws RepositoryException {
        final String path = "/" + getRealmName();
        return getHierarchyManager().createContent(path, name, NodeTypes.User.NAME);
    }

    /**
     * Return the HierarchyManager for the user workspace (through the system context).
     */
    protected HierarchyManager getHierarchyManager() {
        return MgnlContext.getSystemContext().getHierarchyManager(RepositoryConstants.USERS);
    }

    /**
     * @deprecated since 4.3.1 - use {@link #newUserInstance(javax.jcr.Node)}
     */
    @Deprecated
    protected MgnlUser userInstance(Content node) {
        try {
            return (MgnlUser) newUserInstance(node.getJCRNode());
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates a {@link MgnlUser} out of a jcr node. Can be overridden in order to provide a different implementation.
     * @since 4.3.1
     * @deprecated since 4.5 use newUSerInstance(javax.jcr.Node) instead
     */
    @Deprecated
    protected User newUserInstance(Content node) {
        try {
            return newUserInstance(node.getJCRNode());
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private Node addWrite(String parentPath, String property, Node acls) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Node acl = acls.addNode(Path.getUniqueLabel(acls.getSession(), acls.getPath(), "0"), NodeTypes.ContentNode.NAME);
        acl.setProperty("path", parentPath + "/" + property);
        acl.setProperty("permissions", Permission.ALL);
        return acl;
    }

    @Override
    public void updateLastAccessTimestamp(final User user) throws UnsupportedOperationException {
        MgnlContext.doInSystemContext(new SilentSessionOp<Void>(getRepositoryName()) {

            @Override
            public Void doExec(Session session) throws RepositoryException {
                String path = ((MgnlUser) user).getPath();
                log.debug("update access timestamp for {}", user.getName());
                try {
                    Node userNode = session.getNode(path);
                    PropertyUtil.updateOrCreate(userNode, "lastaccess", new GregorianCalendar());
                    session.save();
                }
                catch (RepositoryException e) {
                    session.refresh(false);
                }
                return null;
            }
            @Override
            public String toString() {
                return "update user "+user.getName()+" last access time stamp";
            }
        });
    }

    protected User newUserInstance(Node privilegedUserNode) throws ValueFormatException, PathNotFoundException, RepositoryException {
        if (privilegedUserNode == null) {
            return null;
        }
        Set<String> roles = collectUniquePropertyNames(privilegedUserNode, "roles", RepositoryConstants.USER_ROLES, false);
        Set<String> groups = collectUniquePropertyNames(privilegedUserNode, "groups", RepositoryConstants.USER_GROUPS, false);

        Map<String, String> properties = new HashMap<String, String>();
        for (PropertyIterator iter = new FilteringPropertyIterator(privilegedUserNode.getProperties(), NodeUtil.ALL_PROPERTIES_EXCEPT_JCR_AND_MGNL_FILTER); iter.hasNext();) {
            Property prop = iter.nextProperty();
            //TODO: should we check and skip binary props in case someone adds image to the user?
            properties.put(prop.getName(), prop.getString());
        }

        MgnlUser user = new MgnlUser(privilegedUserNode.getName(), getRealmName(), groups, roles, properties, NodeUtil.getPathIfPossible(privilegedUserNode), NodeUtil.getNodeIdentifierIfPossible(privilegedUserNode));
        return user;
    }

    @Override
    protected String getRepositoryName() {
        return RepositoryConstants.USERS;
    }

    /**
     * Sets access control list from a list of roles under the provided content object.
     */
    @Override
    public Map<String, ACL> getACLs(final User user) {
        if (!(user instanceof MgnlUser)) {
            return null;
        }
        return super.getACLs(user.getName());
    }

    @Override
    public User addRole(User user, String roleName) {
        try {
            super.add(user.getName(), roleName, NODE_ROLES);
        } catch (PrincipalNotFoundException e) {
            // user doesn't exist in this UM
            return null;
        }
        return getUser(user.getName());
    }

    /**
     * Collects all property names of given type, sorting them (case insensitive) and removing duplicates in the process.
     */
    private Set<String> collectUniquePropertyNames(Node rootNode, String subnodeName, String repositoryName, boolean isDeep) {
        final SortedSet<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        String path = null;
        try {
            path = rootNode.getPath();
            final Node node = rootNode.getNode(subnodeName);
            collectUniquePropertyNames(node, repositoryName, subnodeName, set, isDeep);
            collectUniquePropertyNames(rootNode.getNode(subnodeName), repositoryName, subnodeName, set, isDeep);
        } catch (PathNotFoundException e) {
            log.debug("{} does not have any {}", path, repositoryName);
        } catch (Throwable t) {
            log.error("Failed to read " + path + " or sub node " + subnodeName + " in repository " + repositoryName, t);
        }
        return set;
    }

    private void collectUniquePropertyNames(final Node node, final String repositoryName, final String subnodeName, final Collection<String> set, final boolean isDeep) throws RepositoryException {
        MgnlContext.doInSystemContext(new JCRSessionOp<Void>(repositoryName) {

            @Override
            public Void exec(Session session) throws RepositoryException {
                for (PropertyIterator iter = new FilteringPropertyIterator(node.getProperties(), NodeUtil.ALL_PROPERTIES_EXCEPT_JCR_AND_MGNL_FILTER); iter.hasNext();) {
                    Property property = iter.nextProperty();
                    final String uuid = property.getString();
                    try {
                        final Node targetNode = session.getNodeByIdentifier(uuid);
                        set.add(targetNode.getName());
                        if (isDeep && targetNode.hasNode(subnodeName)) {
                            collectUniquePropertyNames(targetNode.getNode(subnodeName), repositoryName, subnodeName, set, true);
                        }
                    } catch (ItemNotFoundException t) {
                        final String path = property.getPath();
                        // TODO: why we are using UUIDs here? shouldn't be better to use group names, since uuids can change???
                        log.warn("Can't find {} node by UUID {} referred by node {}", new Object[]{repositoryName, t.getMessage(), path});
                        log.debug("Failed while reading node by UUID", t);
                        // we continue since it can happen that target node is removed
                        // - UUID's are kept as simple strings thus have no referential integrity
                    }
                }
                return null;
            }
        });
    }

    @Override
    public User addGroup(User user, String groupName) {
        try {
            super.add(user.getName(), groupName, NODE_GROUPS);
        } catch (PrincipalNotFoundException e) {
            // user doesn't exist in this UM
            return null;
        }
        return getUser(user.getName());
    }

    @Override
    public User removeGroup(User user, String groupName) {
        try {
            super.remove(user.getName(), groupName, NODE_GROUPS);
        } catch (PrincipalNotFoundException e) {
            // user doesn't exist in this UM
            return null;
        }
        return getUser(user.getName());
    }

    @Override
    public User removeRole(User user, String roleName) {
        try {
            super.remove(user.getName(), roleName, NODE_ROLES);
        } catch (PrincipalNotFoundException e) {
            // user doesn't exist in this UM
            return null;
        }
        return getUser(user.getName());
    }
}
