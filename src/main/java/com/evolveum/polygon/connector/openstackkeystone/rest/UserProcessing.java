package com.evolveum.polygon.connector.openstackkeystone.rest;


import com.evolveum.polygon.common.GuardedStringAccessor;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.identity.v3.Group;
import org.openstack4j.model.identity.v3.User;
import org.openstack4j.openstack.identity.v3.domain.KeystoneUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserProcessing extends ObjectProcessing {

    //required
    private static final String NAME = "name";

    //optional
    private static final String DEFAULT_PROJECT_ID = "default_project_id";
    private static final String DOMAIN_ID = "domain_id";
    private static final String ENABLED = "enabled";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";
    private static final String DESCRIPTION = "description";


    private static final String USERGROUPS = "usergroups";

    private static final String LINKS = "links";


    //The user ID
    private static final String ID = "id";


    public UserProcessing(OpenStackConnectorConfiguration configuration) {
        super(configuration);
    }

    public void buildUserObjectClass(SchemaBuilder schemaBuilder) {

        ObjectClassInfoBuilder userObjClassBuilder = new ObjectClassInfoBuilder();

        userObjClassBuilder.setType(ObjectClass.ACCOUNT_NAME);


//        AttributeInfoBuilder attrName = new AttributeInfoBuilder(NAME);
//        attrName.setRequired(true).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
//        userObjClassBuilder.addAttributeInfo(attrName.build());

        AttributeInfoBuilder attrDefault_project_id = new AttributeInfoBuilder(DEFAULT_PROJECT_ID);
        attrDefault_project_id.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrDefault_project_id.build());

        AttributeInfoBuilder attrDomain_id = new AttributeInfoBuilder(DOMAIN_ID);
        attrDomain_id.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrDomain_id.build());

        AttributeInfoBuilder attrEnabled = new AttributeInfoBuilder(ENABLED);
        attrEnabled.setRequired(false).setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
        userObjClassBuilder.addAttributeInfo(attrEnabled.build());

//        AttributeInfoBuilder attrPassword = new AttributeInfoBuilder(PASSWORD);
//        attrPassword.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
//        userObjClassBuilder.addAttributeInfo(attrPassword.build());

        userObjClassBuilder.addAttributeInfo(OperationalAttributeInfos.PASSWORD);


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
            if (attribute.getName().equals(ENABLED)) {
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
                if (attribute.getName().equals(DEFAULT_PROJECT_ID)) {
                    user = os.identity().users().update(user.toBuilder().defaultProjectId(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals(DOMAIN_ID)) {
                    user = os.identity().users().update(user.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals(ENABLED)) {
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
                //  builder.addAttribute(ID, user.getId());
                builder.setUid(new Uid(String.valueOf(user.getId())));
            }
            if (user.getName() != null) {
              //  builder.addAttribute(NAME, user.getName());
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
}
