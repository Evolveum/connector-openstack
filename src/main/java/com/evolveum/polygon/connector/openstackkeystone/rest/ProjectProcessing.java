package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.v3.Project;
import org.openstack4j.openstack.identity.v3.domain.KeystoneProject;

import java.util.List;
import java.util.Set;

public class ProjectProcessing extends ObjectProcessing {


    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String DOMAIN_ID = "domain_id";
    private static final String ENABLED = "enabled";
    private static final String PARENT_ID = "parent_id";
    protected static final String PROJECT_NAME = "Project";

    public ProjectProcessing(OpenStackConnectorConfiguration configuration) {
        super(configuration);
    }

    public void buildProjectObjectClass(SchemaBuilder schemaBuilder) {

        ObjectClassInfoBuilder projectObjClassBuilder = new ObjectClassInfoBuilder();

        projectObjClassBuilder.setType(PROJECT_NAME);


        AttributeInfoBuilder attrName = new AttributeInfoBuilder(NAME);
        attrName.setRequired(true).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        projectObjClassBuilder.addAttributeInfo(attrName.build());

        AttributeInfoBuilder attrDescription = new AttributeInfoBuilder(DESCRIPTION);
        attrDescription.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        projectObjClassBuilder.addAttributeInfo(attrDescription.build());

        AttributeInfoBuilder attrDomainId = new AttributeInfoBuilder(DOMAIN_ID);
        attrDomainId.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        projectObjClassBuilder.addAttributeInfo(attrDomainId.build());

        AttributeInfoBuilder attrEnabled = new AttributeInfoBuilder(ENABLED);
        attrEnabled.setRequired(false).setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
        projectObjClassBuilder.addAttributeInfo(attrEnabled.build());

        //parent_id is immutable
        AttributeInfoBuilder attrParentId = new AttributeInfoBuilder(PARENT_ID);
        attrParentId.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(false).setReadable(true);
        projectObjClassBuilder.addAttributeInfo(attrParentId.build());


        schemaBuilder.defineObjectClass(projectObjClassBuilder.build());


    }


    public Uid createProject(Set<Attribute> attributes) {
        LOG.info("Start createUser, attributes: {0}", attributes);

        if (attributes == null || attributes.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }


        KeystoneProject keystoneProject = new KeystoneProject();

        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(NAME)) {
                keystoneProject.toBuilder().name(AttributeUtil.getAsStringValue(attribute));
            }

            if (attribute.getName().equals(DESCRIPTION)) {
                keystoneProject.toBuilder().description(AttributeUtil.getAsStringValue(attribute));
            }
            if (attribute.getName().equals(DOMAIN_ID)) {
                keystoneProject.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute));
            }

            if (attribute.getName().equals(ENABLED)) {
                keystoneProject.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute));
            }
            if (attribute.getName().equals(PARENT_ID)) {
                keystoneProject.toBuilder().parentId(AttributeUtil.getAsStringValue(attribute));
            }
        }


        keystoneProject.toBuilder().build();
        LOG.info("KeystoneProject: {0} ", keystoneProject);

        OSClient.OSClientV3 os = authenticate(getConfiguration());
        Project createdProject = os.identity().projects().create(keystoneProject);
        LOG.info("createdKeystoneUser {0}", createdProject);
        return new Uid(createdProject.getId());
    }

    public void deleteProject(Uid uid) {
        if (uid == null) {
            throw new InvalidAttributeValueException("uid not provided");
        }

        OSClient.OSClientV3 os = authenticate(getConfiguration());
        LOG.info("Delete project with UID: {0}", uid.getUidValue());
        os.identity().projects().delete(uid.getUidValue());


    }

    public void updateProject(Uid uid, Set<Attribute> attributes) {

        OSClient.OSClientV3 os = authenticate(getConfiguration());

        Project project = os.identity().projects().get(uid.getUidValue());
        LOG.info("Project is : {0}", project);
        if (project != null) {
            for (Attribute attribute : attributes) {

                if (attribute.getName().equals("domain_id")) {
                    project = os.identity().projects().update(project.toBuilder().domainId(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals("enabled")) {
                    project = os.identity().projects().update(project.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute)).build());
                }
                if (attribute.getName().equals("name")) {
                    project = os.identity().projects().update(project.toBuilder().name(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals("description")) {
                    project = os.identity().projects().update(project.toBuilder().description(AttributeUtil.getAsStringValue(attribute)).build());
                }
            }
        } else LOG.error("Project object is null");


    }

    public void executeQueryForProject(Filter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("executeQueryForProject()");
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
                Project project = os.identity().projects().get(uid.getUidValue());
                convertProjectToConnectorObject(project, handler);

            } else if (((EqualsFilter) query).getAttribute().getName().equals(NAME)) {
                LOG.info("((EqualsFilter) query).getAttribute().equals(\"name\")");

                List<Object> allValues = ((EqualsFilter) query).getAttribute().getValue();
                if (allValues == null || allValues.get(0) == null) {
                    invalidAttributeValue("Name", query);
                }

                String attributeValue = allValues.get(0).toString();
                LOG.info("Attribute value is: {0}", attributeValue);

                OSClient.OSClientV3 os = authenticate(getConfiguration());
                List<? extends Project> projects = os.identity().projects().getByName(attributeValue);
                for (Project project : projects) {
                    convertProjectToConnectorObject(project, handler);
                }
            }
        } else if (query == null) {
            LOG.info("query==null");

            OSClient.OSClientV3 os = authenticate(getConfiguration());
            List<? extends Project> projects = os.identity().projects().list();
            for (Project project : projects) {
                convertProjectToConnectorObject(project, handler);
            }
        }

    }


    protected void invalidAttributeValue(String attrName, Filter query) {
        StringBuilder sb = new StringBuilder();
        sb.append("Value of").append(attrName).append("attribute not provided for query: ").append(query);
        throw new InvalidAttributeValueException(sb.toString());
    }

    public void convertProjectToConnectorObject(Project project, ResultsHandler handler) {

        if (project != null) {
            ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
            builder.setObjectClass(ObjectClass.ACCOUNT);
            if (project.getId() != null) {
                builder.setUid(new Uid(String.valueOf(project.getId())));
            }
            if (project.getName() != null) {
                builder.addAttribute(NAME, project.getName());
                builder.setName(project.getName());
            }
            if (project.getDescription() != null) {
                builder.addAttribute(DESCRIPTION, project.getDescription());
            }
            if (project.getDomainId() != null) {
                builder.addAttribute(DOMAIN_ID, project.getDomainId());
            }

            if (project.isEnabled()) {
                builder.addAttribute(ENABLED, true);
            } else {
                builder.addAttribute(ENABLED, false);
            }

            ConnectorObject connectorObject = builder.build();
            handler.handle(connectorObject);

        } else LOG.error("project object is null!");

    }


}
