package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.common.StringUtil;
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
import org.openstack4j.openstack.identity.v3.domain.KeystoneGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GroupProcessing extends ObjectProcessing {

    //optional
    private static final String DESCRIPTION = "description";
    private static final String DOMAIN_ID = "domain_id";
    private static final String LINKS = "links";

    private static final String GROUP_MEMBERS = "group_members";


    public GroupProcessing(OpenStackConnectorConfiguration configuration) {
        super(configuration);
    }

    public void buildGroupObjectClass(SchemaBuilder schemaBuilder) {

        ObjectClassInfoBuilder groupObjClassBuilder = new ObjectClassInfoBuilder();

        groupObjClassBuilder.setType(ObjectClass.GROUP_NAME);

        AttributeInfoBuilder attrDomain_id = new AttributeInfoBuilder(DOMAIN_ID);
        attrDomain_id.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        groupObjClassBuilder.addAttributeInfo(attrDomain_id.build());

        AttributeInfoBuilder attrDescription = new AttributeInfoBuilder(DESCRIPTION);
        attrDescription.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        groupObjClassBuilder.addAttributeInfo(attrDescription.build());

        AttributeInfoBuilder attrMembers = new AttributeInfoBuilder(GROUP_MEMBERS);
        attrMembers.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true).setMultiValued(true);
        groupObjClassBuilder.addAttributeInfo(attrMembers.build());

        //read-only && multi-valued
        AttributeInfoBuilder attrLinks = new AttributeInfoBuilder(LINKS);
        attrLinks.setRequired(false).setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true).setMultiValued(true);
        groupObjClassBuilder.addAttributeInfo(attrLinks.build());


        schemaBuilder.defineObjectClass(groupObjClassBuilder.build());


    }

    public Uid createGroup(Set<Attribute> attributes) {
        LOG.info("Start createGroup, attributes: {0}", attributes);

        if (attributes == null || attributes.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }

        Group group = new KeystoneGroup();
        boolean set_required_attribute_name = false;

        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(Name.NAME)) {
                String groupName = AttributeUtil.getAsStringValue(attribute);
                if (!StringUtil.isBlank(groupName)) {
                    group.toBuilder().name(groupName);
                    set_required_attribute_name = true;
                }
            }
            if (attribute.getName().equals(DOMAIN_ID)) {
                group.toBuilder().name(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals(DESCRIPTION)) {
                group.toBuilder().description(AttributeUtil.getAsStringValue(attribute));
            }
        }

        //group name is not set or is empty
        if (!set_required_attribute_name) {
            throw new InvalidAttributeValueException("Missing value of required attribute name in Group");
        }

        group.toBuilder().build();
        LOG.info("KeystoneGroup: {0} ", group);

        OSClientV3 os = authenticate(getConfiguration());

        Group createdGroup = os.identity().groups().create(group);
        LOG.info("createdKeystoneGroup {0}", createdGroup);
        return new Uid(createdGroup.getId());
    }

    public void deleteGroup(Uid uid) {
        if (uid == null) {
            throw new InvalidAttributeValueException("uid not provided");
        }

        OSClientV3 os = authenticate(getConfiguration());
        LOG.info("Delete group with UID: {0}", uid.getUidValue());
        ActionResponse deleteGroupResponse = os.identity().groups().delete(uid.getUidValue());
        if (!deleteGroupResponse.isSuccess()) {
            LOG.info("deleteGroup failed!");
            handleActionResponse(deleteGroupResponse);
        } else LOG.info("deleteGroup success!");

    }

    public void updateGroup(Uid uid, Set<Attribute> attributes) {
        OSClientV3 os = authenticate(getConfiguration());
        Group group = os.identity().groups().get(uid.getUidValue());
        LOG.info("Group is : {0}", group);
        if (group != null) {
            for (Attribute attribute : attributes) {
                if (attribute.getName().equals(DOMAIN_ID)) {
                    group = os.identity().groups().update(group.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute)).build());
                }

                if (attribute.getName().equals(Name.NAME)) {
                    group = os.identity().groups().update(group.toBuilder().name(AttributeUtil.getAsStringValue(attribute)).build());
                }

                if (attribute.getName().equals(DESCRIPTION)) {
                    group = os.identity().groups().update(group.toBuilder().description(AttributeUtil.getAsStringValue(attribute)).build());
                }
            }
        } else throw new UnknownUidException("Returned Group object is null");
    }

    public void executeQueryForGroup(Filter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("executeQueryForGroup()");
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
                Group group = os.identity().groups().get(uid.getUidValue());
                List<? extends User> listGroupUsers = os.identity().groups().listGroupUsers(uid.getUidValue());
                convertGroupToConnectorObject(group, handler, listGroupUsers);

            } else if (((EqualsFilter) query).getAttribute() instanceof Name) {
                LOG.info("((EqualsFilter) query).getAttribute().equals(\"name\")");

                List<Object> allValues = ((EqualsFilter) query).getAttribute().getValue();
                if (allValues == null || allValues.get(0) == null) {
                    invalidAttributeValue("Name", query);
                }

                String attributeValue = allValues.get(0).toString();
                LOG.info("Attribute value is: {0}", attributeValue);
                OSClientV3 os = authenticate(getConfiguration());
                List<? extends Group> groups = os.identity().groups().getByName(attributeValue);
                for (Group group : groups) {
                    List<? extends User> listGroupUsers = os.identity().groups().listGroupUsers(group.getId());
                    convertGroupToConnectorObject(group, handler, listGroupUsers);
                }
            }
        } else if (query == null) {
            LOG.info("query==null");

            OSClientV3 os = authenticate(getConfiguration());
            List<? extends Group> groups = os.identity().groups().list();
            for (Group group : groups) {
                List<? extends User> listGroupUsers = os.identity().groups().listGroupUsers(group.getId());
                convertGroupToConnectorObject(group, handler, listGroupUsers);
            }
        }
    }

    protected void invalidAttributeValue(String attrName, Filter query) {
        StringBuilder sb = new StringBuilder();
        sb.append("Value of").append(attrName).append("attribute not provided for query: ").append(query);
        throw new InvalidAttributeValueException(sb.toString());
    }

    public void convertGroupToConnectorObject(Group group, ResultsHandler handler, List<? extends User> listGroupUsers) {
        LOG.info("convertGroupToConnectorObject, group: {0}, handler {1}, listGroupUser {2} ", group, handler, listGroupUsers);
        if (group != null) {
            ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
            builder.setObjectClass(ObjectClass.GROUP);
            if (group.getId() != null) {
                builder.setUid(new Uid(String.valueOf(group.getId())));
            }
            if (group.getName() != null) {
                builder.setName(group.getName());
            }
            if (group.getDescription() != null) {
                builder.addAttribute(DESCRIPTION, group.getDescription());
            }
            if (group.getDomainId() != null) {
                builder.addAttribute(DOMAIN_ID, group.getDomainId());
            }
            if (group.getLinks() != null) {
                builder.addAttribute(LINKS, group.getLinks());
            }

            if (listGroupUsers != null) {
                List<String> usersList = new ArrayList<>(listGroupUsers.size());
                for (User user : listGroupUsers) {
                    usersList.add(user.getId());
                }
                builder.addAttribute(GROUP_MEMBERS, usersList);
            }

            ConnectorObject connectorObject = builder.build();
            handler.handle(connectorObject);

        } else throw new UnknownUidException("Returned Group object is null");
    }


    //Grant a role to a group in a project
    public void grantProjectGroupRole(String projectId, String groupId, String roleId) {
        OSClient.OSClientV3 os = authenticate(getConfiguration());
        ActionResponse grantProjectGroupRoleResponse = os.identity().roles().grantProjectGroupRole(projectId, groupId, roleId);
        if (!grantProjectGroupRoleResponse.isSuccess()) {
            LOG.info("grantProjectGroupRole failed!");
            handleActionResponse(grantProjectGroupRoleResponse);
        } else LOG.info("grantProjectGroupRole success!");
    }

    //Revoke a role from a group in a project
    public void revokeProjectGroupRole(String projectId, String groupId, String roleId) {
        OSClient.OSClientV3 os = authenticate(getConfiguration());
        ActionResponse revokeProjectGroupRoleResponse = os.identity().roles().revokeProjectGroupRole(projectId, groupId, roleId);
        if (!revokeProjectGroupRoleResponse.isSuccess()) {
            LOG.info("revokeProjectGroupRole failed!");
            handleActionResponse(revokeProjectGroupRoleResponse);
        } else LOG.info("revokeProjectGroupRole success!");

    }

    //Grant a role to a group in a domain
    public void grantDomainGroupRole(String domainId, String groupId, String roleId) {
        OSClient.OSClientV3 os = authenticate(getConfiguration());
        ActionResponse grantDomainGroupRoleResponse = os.identity().roles().grantDomainGroupRole(domainId, groupId, roleId);
        if (!grantDomainGroupRoleResponse.isSuccess()) {
            LOG.info("grantDomainGroupRole failed!");
            handleActionResponse(grantDomainGroupRoleResponse);
        } else LOG.info("grantDomainGroupRole success!");

    }

    //Revoke a role from a group in a domain
    public void revokeDomainGroupRole(String domainId, String groupId, String roleId) {
        OSClient.OSClientV3 os = authenticate(getConfiguration());
        ActionResponse revokeDomainGroupRoleResponse = os.identity().roles().revokeDomainGroupRole(domainId, groupId, roleId);
        if (!revokeDomainGroupRoleResponse.isSuccess()) {
            LOG.info("revokeDomainGroupRole failed!");
            handleActionResponse(revokeDomainGroupRoleResponse);
        } else LOG.info("revokeDomainGroupRole success!");

    }

    public void grantGroupRoles(Attribute attribute, Uid uid) {
        for (Object v : attribute.getValue()) {
            LOG.info("value {0}", v);
            if (!(v instanceof String)) {
                LOG.error("Not string!");
            } else {
                //attribute=projectId:roleId, uid=groupId
                if (((String) v).contains(":")) {
                    String[] split = ((String) v).split(":");
                    String projectId = split[0];
                    String roleId = split[1];
                    String groupId = uid.getUidValue();
                    LOG.info("projectId: {0}, groupId {1}, roleId {2} ", projectId, groupId, roleId);
                    grantProjectGroupRole(projectId, groupId, roleId);
                }
                //attribute=domainId.roleId, uid=groupId
                else if (((String) v).contains(".")) {
                    String[] split = ((String) v).split(".");
                    String domainId = split[0];
                    String roleId = split[1];
                    String groupId = uid.getUidValue();
                    LOG.info("domainId: {0}, groupId {1}, roleId {2} ", domainId, groupId, roleId);
                    grantDomainGroupRole(domainId, groupId, roleId);
                }
            }
        }
    }

    public void revokeGroupRoles(Attribute attribute, Uid uid) {
        for (Object v : attribute.getValue()) {
            LOG.info("value {0}", v);
            if (!(v instanceof String)) {
                LOG.error("Not string!");
            } else {
                //attribute=projectId:roleId, uid=groupId
                if (((String) v).contains(":")) {
                    String[] split = ((String) v).split(":");
                    String projectId = split[0];
                    String roleId = split[1];
                    String groupId = uid.getUidValue();
                    LOG.info("projectId: {0}, groupId {1}, roleId {2} ", projectId, groupId, roleId);
                    revokeProjectGroupRole(projectId, groupId, roleId);
                }
                //attribute=domainId.roleId, uid=groupId
                else if (((String) v).contains(".")) {
                    String[] split = ((String) v).split(".");
                    String domainId = split[0];
                    String roleId = split[1];
                    String groupId = uid.getUidValue();
                    LOG.info("domainId: {0}, groupId {1}, roleId {2} ", domainId, groupId, roleId);
                    revokeDomainGroupRole(domainId, groupId, roleId);
                }
            }
        }
    }
}
