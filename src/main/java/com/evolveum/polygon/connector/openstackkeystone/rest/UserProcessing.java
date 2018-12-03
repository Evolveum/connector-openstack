package com.evolveum.polygon.connector.openstackkeystone.rest;


import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.identity.v3.Group;
import org.openstack4j.model.identity.v3.User;
import org.openstack4j.openstack.identity.v3.domain.KeystoneUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserProcessing extends ObjectProcessing {

    private static final String DEFAULT_PROJECT_ID = "default_project_id";
    private static final String DOMAIN_ID = "domain_id";
    private static final String ENABLED = "enabled";
    //required
    private static final String NAME = "name";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";
    private static final String DESCRIPTION = "description";
    private static final String USERGROUPS = "usergroups";


    //The user ID
    private static final String ID = "id";


    public UserProcessing(OpenStackConnectorConfiguration configuration) {
        super(configuration);
    }

    public void buildUserObjectClass(SchemaBuilder schemaBuilder) {

        ObjectClassInfoBuilder userObjClassBuilder = new ObjectClassInfoBuilder();

        userObjClassBuilder.setType(ObjectClass.ACCOUNT_NAME);

        AttributeInfoBuilder attrName = new AttributeInfoBuilder(NAME);
        attrName.setRequired(true).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrName.build());

        AttributeInfoBuilder attrDefault_project_id = new AttributeInfoBuilder(DEFAULT_PROJECT_ID);
        attrDefault_project_id.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrDefault_project_id.build());

        AttributeInfoBuilder attrDomain_id = new AttributeInfoBuilder(DOMAIN_ID);
        attrDomain_id.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrDomain_id.build());

        AttributeInfoBuilder attrEnabled = new AttributeInfoBuilder(ENABLED);
        attrEnabled.setRequired(false).setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrEnabled.build());

        AttributeInfoBuilder attrPassword = new AttributeInfoBuilder(PASSWORD);
        attrPassword.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrPassword.build());

        AttributeInfoBuilder attrEmail = new AttributeInfoBuilder(EMAIL);
        attrEmail.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrEmail.build());

        AttributeInfoBuilder attrDescription = new AttributeInfoBuilder(DESCRIPTION);
        attrDescription.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrDescription.build());


        AttributeInfoBuilder attrUserInGroups = new AttributeInfoBuilder(USERGROUPS);
        attrUserInGroups.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true).setMultiValued(true);
        userObjClassBuilder.addAttributeInfo(attrUserInGroups.build());

        schemaBuilder.defineObjectClass(userObjClassBuilder.build());


    }


    public Uid createUser(Set<Attribute> attributes) {
        LOG.info("Start createUser, attributes: {0}", attributes);

        if (attributes == null || attributes.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }

        KeystoneUser keystoneUser = new KeystoneUser();

        for (Attribute attribute : attributes) {
            if (attribute.getName().equals("default_project_id")) {

                keystoneUser.toBuilder().defaultProjectId(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals("domain_id")) {
                keystoneUser.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals("enabled")) {
                keystoneUser.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute));
            }
            if (attribute.getName().equals("name")) {
                keystoneUser.toBuilder().name(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals("password")) {
                keystoneUser.toBuilder().password(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals("email")) {
                keystoneUser.toBuilder().email(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals("description")) {
                keystoneUser.toBuilder().description(AttributeUtil.getAsStringValue(attribute));
            }
        }

        keystoneUser.toBuilder().build();
        LOG.info("KeystoneUser: {0} ", keystoneUser);

        OSClientV3 os = authenticate(getConfiguration());


        User createdUser = os.identity().users().create(keystoneUser);
        LOG.info("createdKeystoneUser {0}", createdUser);
        return new Uid(createdUser.getId());
    }

    public void deleteUser(Uid uid) {
        if (uid == null) {
            throw new InvalidAttributeValueException("uid not provided");
        }

        OSClientV3 os = authenticate(getConfiguration());
        LOG.info("Delete user with UID: {0}", uid.getUidValue());
        os.identity().users().delete(uid.getUidValue());

    }

    public void updateUser(Uid uid, Set<Attribute> attributes) {

        OSClientV3 os = authenticate(getConfiguration());
        User user = os.identity().users().get(uid.getUidValue());
        LOG.info("User is : {0}", user);
        if (user != null) {
            for (Attribute attribute : attributes) {
                if (attribute.getName().equals("default_project_id")) {
                    user = os.identity().users().update(user.toBuilder().defaultProjectId(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals("domain_id")) {
                    user = os.identity().users().update(user.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals("enabled")) {
                    user = os.identity().users().update(user.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute)).build());
                }
                if (attribute.getName().equals("name")) {
                    user = os.identity().users().update(user.toBuilder().name(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals("password")) {
                    user = os.identity().users().update(user.toBuilder().password(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals("email")) {
                    user = os.identity().users().update(user.toBuilder().email(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals("description")) {
                    user = os.identity().users().update(user.toBuilder().description(AttributeUtil.getAsStringValue(attribute)).build());
                }
            }
        } else LOG.error("User object is null");
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

            } else if (((EqualsFilter) query).getAttribute().getName().equals(NAME)) {
                LOG.info("((EqualsFilter) query).getAttribute().equals(\"name\")");

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
                //  builder.addAttribute(ID, user.getId());
                builder.setUid(new Uid(String.valueOf(user.getId())));
            }
            if (user.getName() != null) {
                builder.addAttribute(NAME, user.getName());
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
//            if (!user.getPassword().isEmpty()){
//                builder.addAttribute(PASSWORD, user.getPassword());
//            }
            if (user.isEnabled()) {
                builder.addAttribute(ENABLED, true);
            } else {
                builder.addAttribute(ENABLED, false);
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

        } else LOG.error("User object is null!");
    }
}
