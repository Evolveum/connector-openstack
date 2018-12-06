package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.v3.Domain;
import org.openstack4j.openstack.identity.v3.domain.KeystoneDomain;

import java.util.List;
import java.util.Set;

public class DomainProcessing extends ObjectProcessing {
    public DomainProcessing(OpenStackConnectorConfiguration configuration) {
        super(configuration);
    }

    private static final String DOMAIN_NAME = "Domain";
    private static final String DESCRIPTION = "description";
    private static final String ENABLED = "enabled";
    //required
    private static final String NAME = "name";


    public void buildDomainObjectClass(SchemaBuilder schemaBuilder) {

        ObjectClassInfoBuilder domainObjectClass = new ObjectClassInfoBuilder();

        domainObjectClass.setType(DOMAIN_NAME);

        AttributeInfoBuilder attrName = new AttributeInfoBuilder(NAME);
        attrName.setRequired(true).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        domainObjectClass.addAttributeInfo(attrName.build());


        AttributeInfoBuilder attrDescription = new AttributeInfoBuilder(DESCRIPTION);
        attrDescription.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        domainObjectClass.addAttributeInfo(attrDescription.build());

        AttributeInfoBuilder attrMembers = new AttributeInfoBuilder(ENABLED);
        attrMembers.setRequired(false).setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
        domainObjectClass.addAttributeInfo(attrMembers.build());

        schemaBuilder.defineObjectClass(domainObjectClass.build());

    }

    public Uid createDomain(Set<Attribute> attributes) {
        LOG.info("Start createDomain, attributes: {0}", attributes);

        if (attributes == null || attributes.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }


        Domain domain = new KeystoneDomain();

        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(NAME)) {
                domain.toBuilder().name(AttributeUtil.getAsStringValue(attribute));
            }

            if (attribute.getName().equals(DESCRIPTION)) {
                domain.toBuilder().description(AttributeUtil.getAsStringValue(attribute));
            }

            if (attribute.getName().equals(ENABLED)) {
                domain.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute));
            }
        }


        domain.toBuilder().build();
        LOG.info("KeystoneDomain: {0} ", domain);

        OSClient.OSClientV3 os = authenticate(getConfiguration());
        Domain createdDomain = os.identity().domains().create(domain);
        LOG.info("createdKeystoneDomain {0}", createdDomain);
        return new Uid(createdDomain.getId());
    }

    public void deleteDomain(Uid uid) {
        if (uid == null) {
            throw new InvalidAttributeValueException("uid not provided");
        }

        OSClient.OSClientV3 os = authenticate(getConfiguration());
        LOG.info("Delete domain with UID: {0}", uid.getUidValue());
        os.identity().domains().delete(uid.getUidValue());


    }

    public void updateDomain(Uid uid, Set<Attribute> attributes) {

        OSClient.OSClientV3 os = authenticate(getConfiguration());

        Domain domain = os.identity().domains().get(uid.getUidValue());
        LOG.info("Domain is : {0}", domain);
        if (domain != null) {
            for (Attribute attribute : attributes) {


                if (attribute.getName().equals(ENABLED)) {
                    domain = os.identity().domains().update(domain.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute)).build());
                }
                if (attribute.getName().equals(NAME)) {
                    domain = os.identity().domains().update(domain.toBuilder().name(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals(DESCRIPTION)) {
                    domain = os.identity().domains().update(domain.toBuilder().description(AttributeUtil.getAsStringValue(attribute)).build());
                }
            }
        } else LOG.error("Domain object is null");


    }

    public void executeQueryForDomain(Filter query, ResultsHandler handler, OperationOptions options) {
        LOG.info("executeQueryForDomain()");
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
                Domain domain = os.identity().domains().get(uid.getUidValue());
                convertDomainToConnectorObject(domain, handler);

            } else if (((EqualsFilter) query).getAttribute().getName().equals(NAME)) {
                LOG.info("((EqualsFilter) query).getAttribute().equals(\"name\")");

                List<Object> allValues = ((EqualsFilter) query).getAttribute().getValue();
                if (allValues == null || allValues.get(0) == null) {
                    invalidAttributeValue("Name", query);
                }

                String attributeValue = allValues.get(0).toString();
                LOG.info("Attribute value is: {0}", attributeValue);

                OSClient.OSClientV3 os = authenticate(getConfiguration());
                List<? extends Domain> domains = os.identity().domains().getByName(attributeValue);
                for (Domain domain : domains) {
                    convertDomainToConnectorObject(domain, handler);
                }
            }
        } else if (query == null) {
            LOG.info("query==null");

            OSClient.OSClientV3 os = authenticate(getConfiguration());
            List<? extends Domain> domains = os.identity().domains().list();
            for (Domain domain : domains) {
                convertDomainToConnectorObject(domain, handler);
            }
        }

    }


    protected void invalidAttributeValue(String attrName, Filter query) {
        StringBuilder sb = new StringBuilder();
        sb.append("Value of").append(attrName).append("attribute not provided for query: ").append(query);
        throw new InvalidAttributeValueException(sb.toString());
    }

    public void convertDomainToConnectorObject(Domain domain, ResultsHandler handler) {

        if (domain != null) {
            ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
            builder.setObjectClass(new ObjectClass(DOMAIN_NAME));
            if (domain.getId() != null) {
                builder.setUid(new Uid(String.valueOf(domain.getId())));
            }
            if (domain.getName() != null) {
                builder.addAttribute(NAME, domain.getName());
                builder.setName(domain.getName());
            }
            if (domain.getDescription() != null) {
                builder.addAttribute(DESCRIPTION, domain.getDescription());
            }
            if (domain.isEnabled()) {
                builder.addAttribute(ENABLED, true);
            } else {
                builder.addAttribute(ENABLED, false);
            }

            ConnectorObject connectorObject = builder.build();
            handler.handle(connectorObject);

        } else LOG.error("domain object is null!");

    }


}