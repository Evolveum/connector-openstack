package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.v3.Role;
import org.openstack4j.openstack.identity.v3.domain.KeystoneRole;

import java.util.List;
import java.util.Set;


public class RoleProcessing extends ObjectProcessing {

    //required
    private static final String NAME = "name";

    //optional
    private static final String DESCRIPTION = "description";
    private static final String DOMAIN_ID = "domain_id";

    private static final String ROLE_NAME = "Role";

    public RoleProcessing(OpenStackConnectorConfiguration configuration) {
        super(configuration);
    }

    public void buildRoleObjectClass(SchemaBuilder schemaBuilder) {

        ObjectClassInfoBuilder roleObjClassBuilder = new ObjectClassInfoBuilder();

        roleObjClassBuilder.setType(ROLE_NAME);

        AttributeInfoBuilder attrName = new AttributeInfoBuilder(NAME);
        attrName.setRequired(true).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        roleObjClassBuilder.addAttributeInfo(attrName.build());


        AttributeInfoBuilder attrDomain_id = new AttributeInfoBuilder(DOMAIN_ID);
        attrDomain_id.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        roleObjClassBuilder.addAttributeInfo(attrDomain_id.build());

        AttributeInfoBuilder attrDescription = new AttributeInfoBuilder(DESCRIPTION);
        attrDescription.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        roleObjClassBuilder.addAttributeInfo(attrDescription.build());


        schemaBuilder.defineObjectClass(roleObjClassBuilder.build());


    }


    public Uid createRole(Set<Attribute> attributes) {
        LOG.info("Start createRole, attributes: {0}", attributes);

        if (attributes == null || attributes.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }

        Role role = new KeystoneRole();
        boolean set_required_attribute_name = false;

        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(NAME)) {
                String roleName = AttributeUtil.getAsStringValue(attribute);
                if (!StringUtil.isBlank(roleName)) {
                    role.toBuilder().name(roleName);
                    set_required_attribute_name = true;
                }
            }
            if (attribute.getName().equals(DOMAIN_ID)) {
                role.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute));
            }

        }

        //role name is not set or is empty
        if (!set_required_attribute_name) {
            throw new InvalidAttributeValueException("Missing value of required attribute name in Group");
        }

        role.toBuilder().build();
        LOG.info("Role: {0} ", role);

        OSClient.OSClientV3 os = authenticate(getConfiguration());
        Role createdRole = os.identity().roles().create(role);
        LOG.info("createdRole {0}", createdRole);
        return new Uid(createdRole.getId());
    }

    public void deleteRole(Uid uid) {
        if (uid == null) {
            throw new InvalidAttributeValueException("uid not provided");
        }

        OSClient.OSClientV3 os = authenticate(getConfiguration());
        LOG.info("Delete role with UID: {0}", uid.getUidValue());
        os.identity().roles().delete(uid.getUidValue());

    }

    public void updateRole(Uid uid, Set<Attribute> attributes) {

        OSClient.OSClientV3 os = authenticate(getConfiguration());
        Role role = os.identity().roles().get(uid.getUidValue());
        LOG.info("Role is : {0}", role);
        if (role != null) {
            for (Attribute attribute : attributes) {

                if (attribute.getName().equals(DOMAIN_ID)) {
                    role = os.identity().roles().update(role.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute)).build());
                }

                if (attribute.getName().equals(NAME)) {
                    role = os.identity().roles().update(role.toBuilder().name(AttributeUtil.getAsStringValue(attribute)).build());
                }
            }
        }  else throw new UnknownUidException("Returned Role object is null");
    }

    public void executeQueryForRole(Filter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("executeQueryForRole()");
        if (query instanceof EqualsFilter) {
            LOG.info("query instanceof EqualsFilter");
            if (((EqualsFilter) query).getAttribute() instanceof Uid) {
                LOG.info("((EqualsFilter) query).getAttribute() instanceof Uid");

                Uid uid = (Uid) ((EqualsFilter) query).getAttribute();
                LOG.info("Uid {0}", uid);
                if (uid.getUidValue() == null) {
                    invalidAttributeValue("Uid", query);
                }

                OSClient.OSClientV3 os = authenticate(getConfiguration());
                Role role = os.identity().roles().get(uid.getUidValue());

                convertRoleToConnectorObject(role, handler);

            } else if (((EqualsFilter) query).getAttribute().getName().equals(NAME)) {
                LOG.info("((EqualsFilter) query).getAttribute().equals(\"name\")");

                List<Object> allValues = ((EqualsFilter) query).getAttribute().getValue();
                if (allValues == null || allValues.get(0) == null) {
                    invalidAttributeValue("Name", query);
                }

                String attributeValue = allValues.get(0).toString();
                LOG.info("Attribute value is: {0}", attributeValue);

                OSClient.OSClientV3 os = authenticate(getConfiguration());
                List<? extends Role> roles = os.identity().roles().getByName(attributeValue);
                for (Role role : roles) {
                    convertRoleToConnectorObject(role, handler);
                }
            }
        } else if (query == null) {
            LOG.info("query==null");

            OSClient.OSClientV3 os = authenticate(getConfiguration());
            List<? extends Role> roles = os.identity().roles().list();
            for (Role role : roles) {
                convertRoleToConnectorObject(role, handler);
            }
        }

    }


    protected void invalidAttributeValue(String attrName, Filter query) {
        StringBuilder sb = new StringBuilder();
        sb.append("Value of").append(attrName).append("attribute not provided for query: ").append(query);
        throw new InvalidAttributeValueException(sb.toString());
    }

    public void convertRoleToConnectorObject(Role role, ResultsHandler handler) {
        LOG.info("convertRoleToConnectorObject, role: {0}, handler {1}", role, handler);
        if (role != null) {
            ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
            builder.setObjectClass(new ObjectClass(ROLE_NAME));
            if (role.getId() != null) {
                //  builder.addAttribute(ID, role.getId());
                builder.setUid(new Uid(String.valueOf(role.getId())));
            }
            if (role.getName() != null) {
                builder.addAttribute(NAME, role.getName());
                builder.setName(role.getName());
            }

            if (role.getDomainId() != null) {
                builder.addAttribute(DOMAIN_ID, role.getDomainId());
            }

            ConnectorObject connectorObject = builder.build();
            handler.handle(connectorObject);

        } else throw new UnknownUidException("Returned Role object is null");
    }

}
