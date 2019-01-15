package com.evolveum.polygon.connector.openstackkeystone.rest;


import com.evolveum.polygon.common.GuardedStringAccessor;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.identity.v3.Group;
import org.openstack4j.model.identity.v3.User;
import org.openstack4j.openstack.identity.v3.domain.KeystoneUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserProcessing extends ObjectProcessing {

    //optional
    private static final String DEFAULT_PROJECT_ID = "default_project_id";
    private static final String DOMAIN_ID = "domain_id";
    private static final String EMAIL = "email";
    private static final String DESCRIPTION = "description";


    private static final String USERGROUPS = "usergroups";
    private static final String USERROLES = "userroles";

    private static final String LINKS = "links";


    public UserProcessing(OpenStackConnectorConfiguration configuration) {
        super(configuration);
    }

    public void buildUserObjectClass(SchemaBuilder schemaBuilder) {

        ObjectClassInfoBuilder userObjClassBuilder = new ObjectClassInfoBuilder();

        userObjClassBuilder.setType(ObjectClass.ACCOUNT_NAME);


        AttributeInfoBuilder attrDefault_project_id = new AttributeInfoBuilder(DEFAULT_PROJECT_ID);
        attrDefault_project_id.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrDefault_project_id.build());

        AttributeInfoBuilder attrDomain_id = new AttributeInfoBuilder(DOMAIN_ID);
        attrDomain_id.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrDomain_id.build());

        userObjClassBuilder.addAttributeInfo(OperationalAttributeInfos.PASSWORD);

        userObjClassBuilder.addAttributeInfo(OperationalAttributeInfos.ENABLE);

        AttributeInfoBuilder attrEmail = new AttributeInfoBuilder(EMAIL);
        attrEmail.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrEmail.build());

        AttributeInfoBuilder attrDescription = new AttributeInfoBuilder(DESCRIPTION);
        attrDescription.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrDescription.build());

        //read-only && multi-valued
        AttributeInfoBuilder attrLinks = new AttributeInfoBuilder(LINKS);
        attrLinks.setRequired(false).setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true).setMultiValued(true);
        userObjClassBuilder.addAttributeInfo(attrLinks.build());

        AttributeInfoBuilder attrUserInGroups = new AttributeInfoBuilder(USERGROUPS);
        attrUserInGroups.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true).setMultiValued(true);
        userObjClassBuilder.addAttributeInfo(attrUserInGroups.build());

        AttributeInfoBuilder attrUserRoles = new AttributeInfoBuilder(USERROLES);
        attrUserRoles.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true).setMultiValued(true);
        userObjClassBuilder.addAttributeInfo(attrUserRoles.build());

        schemaBuilder.defineObjectClass(userObjClassBuilder.build());


    }


    public Uid createUser(Set<Attribute> attributes) {
        LOG.info("Start createUser, attributes: {0}", attributes);
        if (attributes == null || attributes.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }

        User user = new KeystoneUser();
        boolean set_required_attribute_name = false;
        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(Name.NAME)) {
                String userName = AttributeUtil.getAsStringValue(attribute);
                if (!StringUtil.isBlank(userName)) {
                    user.toBuilder().name(userName);
                    set_required_attribute_name = true;
                }
            }
            if (attribute.getName().equals(DEFAULT_PROJECT_ID)) {
                user.toBuilder().defaultProjectId(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals(DOMAIN_ID)) {
                user.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals(OperationalAttributes.ENABLE_NAME)) {
                user.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute));
            }
            if (attribute.getName().equals(OperationalAttributes.PASSWORD_NAME)) {
                GuardedString guardedString = (GuardedString) AttributeUtil.getSingleValue(attribute);
                GuardedStringAccessor accessor = new GuardedStringAccessor();
                guardedString.access(accessor);
                user.toBuilder().password(accessor.getClearString());
            }
            if (attribute.getName().equals(EMAIL)) {
                user.toBuilder().email(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals(DESCRIPTION)) {
                user.toBuilder().description(AttributeUtil.getAsStringValue(attribute));
            }
        }

        //user name is not set or is empty
        if (!set_required_attribute_name) {
            throw new InvalidAttributeValueException("Missing value of required attribute name in User");
        }

        user.toBuilder().build();
        LOG.info("KeystoneUser: {0} ", user);
        OSClientV3 os = authenticate(getConfiguration());
        User createdUser = os.identity().users().create(user);
        LOG.info("createdKeystoneUser {0}", createdUser);
        Uid uid = new Uid(createdUser.getId());
        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(USERROLES)) {
                grantUserRoles(attribute, uid);
            } else if (attribute.getName().equals(USERGROUPS)) {
                userToGroup(attribute, uid);
            }
        }
        return new Uid(createdUser.getId());
    }

    public void deleteUser(Uid uid) {
        if (uid == null) {
            throw new InvalidAttributeValueException("uid not provided");
        }

        OSClientV3 os = authenticate(getConfiguration());
        LOG.info("Delete user with UID: {0}", uid.getUidValue());
        ActionResponse userDeleteResponse = os.identity().users().delete(uid.getUidValue());
        if (!userDeleteResponse.isSuccess()) {
            LOG.info("deleteUser failed!");
            handleActionResponse(userDeleteResponse);
        } else LOG.info("deleteUser success!");

    }

    public void updateUser(Uid uid, Set<Attribute> attributes) {

        OSClientV3 os = authenticate(getConfiguration());
        User user = os.identity().users().get(uid.getUidValue());
        LOG.info("User is : {0}", user);
        if (user != null) {
            for (Attribute attribute : attributes) {
                if (attribute.getName().equals(DEFAULT_PROJECT_ID)) {
                    user = os.identity().users().update(user.toBuilder().defaultProjectId(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals(DOMAIN_ID)) {
                    user = os.identity().users().update(user.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals(OperationalAttributes.ENABLE_NAME)) {
                    user = os.identity().users().update(user.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute)).build());
                }
                if (attribute.getName().equals(Name.NAME)) {
                    user = os.identity().users().update(user.toBuilder().name(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals(OperationalAttributes.PASSWORD_NAME)) {
                    GuardedString guardedString = (GuardedString) AttributeUtil.getSingleValue(attribute);
                    GuardedStringAccessor accessor = new GuardedStringAccessor();
                    guardedString.access(accessor);
                    user = os.identity().users().update(user.toBuilder().password(accessor.getClearString()).build());
                }
                if (attribute.getName().equals(EMAIL)) {
                    user = os.identity().users().update(user.toBuilder().email(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals(DESCRIPTION)) {
                    user = os.identity().users().update(user.toBuilder().description(AttributeUtil.getAsStringValue(attribute)).build());
                }
            }
        } else throw new UnknownUidException("Returned User object is null");
    }

    public void executeQueryForUser(Filter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("executeQueryForUser()");
        if (query instanceof EqualsFilter) {
            LOG.info("query instanceof EqualsFilter");
            if (((EqualsFilter) query).getAttribute() instanceof Uid) {
                LOG.info("((EqualsFilter) query).getAttribute() instanceof Uid");

                Uid uid = (Uid) ((EqualsFilter) query).getAttribute();
                LOG.info("Uid {0}", uid);
                if (uid.getUidValue() == null) {
                    invalidAttributeValue("Uid", query);
                }

                OSClientV3 os = authenticate(getConfiguration());
                User user = os.identity().users().get(uid.getUidValue());
                List<? extends Group> listUserGroups = os.identity().users().listUserGroups(uid.getUidValue());
                convertUserToConnectorObject(user, handler, listUserGroups);

            } else if (((EqualsFilter) query).getAttribute() instanceof Name) {
                LOG.info("(((EqualsFilter) query).getAttribute() instanceof Name)");

                List<Object> allValues = ((EqualsFilter) query).getAttribute().getValue();
                if (allValues == null || allValues.get(0) == null) {
                    invalidAttributeValue("Name", query);
                }

                String attributeValue = allValues.get(0).toString();
                LOG.info("Attribute value is: {0}", attributeValue);

                OSClientV3 os = authenticate(getConfiguration());
                List<? extends User> users = os.identity().users().getByName(attributeValue);

                for (User user : users) {
                    convertUserToConnectorObject(user, handler, null);
                }
            }
        } else if (query == null) {
            LOG.info("query==null");

            OSClientV3 os = authenticate(getConfiguration());
            List<? extends User> users = os.identity().users().list();
            for (User user : users) {
                List<? extends Group> listUserGroups = os.identity().users().listUserGroups(user.getId());
                convertUserToConnectorObject(user, handler, null);
            }
        }

    }


    protected void invalidAttributeValue(String attrName, Filter query) {
        StringBuilder sb = new StringBuilder();
        sb.append("Value of").append(attrName).append("attribute not provided for query: ").append(query);
        throw new InvalidAttributeValueException(sb.toString());
    }

    public void convertUserToConnectorObject(User user, ResultsHandler handler, List<? extends Group> listUserGroups) {
        LOG.info("convertRoleToConnectorObject, user: {0}, handler {1}", user, handler);
        if (user != null) {
            ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
            builder.setObjectClass(ObjectClass.ACCOUNT);
            if (user.getId() != null) {
                builder.setUid(new Uid(String.valueOf(user.getId())));
            }
            if (user.getName() != null) {
                builder.setName(user.getName());
            }
            if (user.getDefaultProjectId() != null) {
                builder.addAttribute(DEFAULT_PROJECT_ID, user.getDefaultProjectId());
            }
            if (user.getDescription() != null) {
                builder.addAttribute(DESCRIPTION, user.getDescription());
            }
            if (user.getDomainId() != null) {
                builder.addAttribute(DOMAIN_ID, user.getDomainId());
            }
            if (user.getEmail() != null) {
                builder.addAttribute(EMAIL, user.getEmail());
            }

            if (user.isEnabled()) {
                builder.addAttribute(OperationalAttributes.ENABLE_NAME, true);
            } else {
                builder.addAttribute(OperationalAttributes.ENABLE_NAME, false);
            }

            if (user.getLinks() != null) {
                builder.addAttribute(LINKS, user.getLinks());
            }

            if (listUserGroups != null) {
                List<String> groupsList = new ArrayList<>(listUserGroups.size());
                for (Group group : listUserGroups) {
                    groupsList.add(group.getId());
                }
                builder.addAttribute(USERGROUPS, groupsList);
            }

            ConnectorObject connectorObject = builder.build();
            handler.handle(connectorObject);

        } else throw new UnknownUidException("Returned User object is null");
    }

    //Grant a role to a user in a project
    public void grantProjectUserRole(String projectId, String userId, String roleId) {
        OSClient.OSClientV3 os = authenticate(getConfiguration());
        ActionResponse grantProjectUserRoleResponse = os.identity().roles().grantProjectUserRole(projectId, userId, roleId);
        if (!grantProjectUserRoleResponse.isSuccess()) {
            LOG.info("grantProjectUserRole failed!");
            handleActionResponse(grantProjectUserRoleResponse);
        } else LOG.info("grantProjectUserRole success!");
    }

    //Revoke a role from a user in a project
    public void revokeProjectUserRole(String projectId, String userId, String roleUid) {
        OSClient.OSClientV3 os = authenticate(getConfiguration());
        ActionResponse revokeProjectUserRoleResponse = os.identity().roles().revokeProjectUserRole(projectId, userId, roleUid);
        if (!revokeProjectUserRoleResponse.isSuccess()) {
            LOG.info("revokeProjectUserRole failed!");
            handleActionResponse(revokeProjectUserRoleResponse);
        } else LOG.info("revokeProjectUserRole success!");
    }


    //Grant a role to a user in a domain
    public void grantDomainUserRole(String domainId, String userId, String roleId) {
        OSClient.OSClientV3 os = authenticate(getConfiguration());
        ActionResponse grantDomainUserRoleResponse = os.identity().roles().grantDomainUserRole(domainId, userId, roleId);
        if (!grantDomainUserRoleResponse.isSuccess()) {
            LOG.info("grantProjectUserRole failed!");
            handleActionResponse(grantDomainUserRoleResponse);
        } else LOG.info("grantProjectUserRole success!");
    }

    //Revoke a role from a user in a domain
    public void revokeDomainUserRole(String domainId, String userId, String roleId) {
        OSClient.OSClientV3 os = authenticate(getConfiguration());
        ActionResponse revokeDomainUserRoleResponse = os.identity().roles().revokeDomainUserRole(domainId, userId, roleId);
        if (!revokeDomainUserRoleResponse.isSuccess()) {
            LOG.info("revokeDomainUserRole failed!");
            handleActionResponse(revokeDomainUserRoleResponse);
        } else LOG.info("revokeDomainUserRole success!");
    }


    public void grantUserRoles(Attribute attribute, Uid uid) {
        for (Object v : attribute.getValue()) {
            LOG.info("value {0}", v);
            if (!(v instanceof String)) {
                LOG.error("Not string!");
            } else {
                //attribute=projectId:roleId, uid=userId
                if (((String) v).contains(":")) {
                    String[] split = ((String) v).split(":");
                    String projectId = split[0];
                    String roleId = split[1];
                    String userId = uid.getUidValue();
                    LOG.info("projectId: {0}, userId {1}, roleId {2} ", projectId, userId, roleId);
                    grantProjectUserRole(projectId, userId, roleId);
                }
                //attribute=domainId.roleId, uid=userId
                else if (((String) v).contains(".")) {
                    String[] split = ((String) v).split(".");
                    String domainId = split[0];
                    String roleId = split[1];
                    String userId = uid.getUidValue();
                    LOG.info("domainId: {0}, userId {1}, roleId {2} ", domainId, userId, roleId);
                    grantDomainUserRole(domainId, userId, roleId);
                }
            }
        }
    }

    public void revokeUserRoles(Attribute attribute, Uid uid) {
        for (Object v : attribute.getValue()) {
            LOG.info("value {0}", v);
            if (!(v instanceof String)) {
                LOG.error("Not string!");
            } else {
                //attribute=projectId:roleId, uid=userId
                if (((String) v).contains(":")) {
                    String[] split = ((String) v).split(":");
                    String projectId = split[0];
                    String roleId = split[1];
                    String userId = uid.getUidValue();
                    LOG.info("projectId: {0}, userId {1}, roleId {2} ", projectId, userId, roleId);
                    revokeProjectUserRole(projectId, userId, roleId);
                }
                //attribute=domainId.roleId, uid=userId
                else if (((String) v).contains(".")) {
                    String[] split = ((String) v).split(".");
                    String domainId = split[0];
                    String roleId = split[1];
                    String userId = uid.getUidValue();
                    LOG.info("domainId: {0}, userId {1}, roleId {2} ", domainId, userId, roleId);
                    revokeDomainUserRole(domainId, userId, roleId);
                }
            }
        }
    }

    public void userToGroup(Attribute attribute, Uid uid) {
        String userId = uid.getUidValue();
        String groupId;
        for (Object v : attribute.getValue()) {
            LOG.info("value {0}", v);
            if (!(v instanceof String)) {
                LOG.error("Not string!");
            } else {
                groupId = (String) v;
                addUserToGroup(groupId, userId);
            }
        }
    }

    public void userRemoveFromGroup(Attribute attribute, Uid uid) {
        String userId = uid.getUidValue();
        String groupId;
        for (Object v : attribute.getValue()) {
            LOG.info("value {0}", v);
            if (!(v instanceof String)) {
                LOG.error("Not string!");
            } else {
                groupId = (String) v;
                removeUserFromGroup(groupId, userId);
            }
        }
    }

    public void addUserToGroup(String groupId, String userId) {
        OSClientV3 os = authenticate(getConfiguration());
        //addUserToGroup("groupId", "userId");
        ActionResponse addUserToGroupResponse = os.identity().groups().addUserToGroup(groupId, userId);
        if (!addUserToGroupResponse.isSuccess()) {
            LOG.info("addUserToGroup failed!");
            handleActionResponse(addUserToGroupResponse);
        } else LOG.info("addUserToGroup success!");
    }


    public void removeUserFromGroup(String groupId, String userId) {
        OSClientV3 os = authenticate(getConfiguration());
        //removeUserFromGroup("groupId", "userId");
        ActionResponse removeUserFromGroupResponse = os.identity().groups().removeUserFromGroup(groupId, userId);
        if (!removeUserFromGroupResponse.isSuccess()) {
            LOG.info("removeUserFromGroup failed!");
            handleActionResponse(removeUserFromGroupResponse);
        } else LOG.info("removeUserFromGroup success!");
    }

}
