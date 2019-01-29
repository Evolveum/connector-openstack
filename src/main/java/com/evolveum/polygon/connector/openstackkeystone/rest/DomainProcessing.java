package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.identity.v3.Domain;
import org.openstack4j.openstack.identity.v3.domain.KeystoneDomain;

import java.util.List;
import java.util.Set;

public class DomainProcessing extends ObjectProcessing {
    public DomainProcessing(OpenStackConnectorConfiguration configuration) {
        super(configuration);
    }

    //optional
    private static final String DOMAIN_NAME = "Domain";
    private static final String DESCRIPTION = "description";

    private static final String LINKS = "links";


    public void buildDomainObjectClass(SchemaBuilder schemaBuilder) {

        ObjectClassInfoBuilder domainObjectClass = new ObjectClassInfoBuilder();

        domainObjectClass.setType(DOMAIN_NAME);


        AttributeInfoBuilder attrDescription = new AttributeInfoBuilder(DESCRIPTION);
        attrDescription.setRequired(false).setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
        domainObjectClass.addAttributeInfo(attrDescription.build());

        domainObjectClass.addAttributeInfo(OperationalAttributeInfos.ENABLE);

        schemaBuilder.defineObjectClass(domainObjectClass.build());

        //read-only && multi-valued
        AttributeInfoBuilder attrLinks = new AttributeInfoBuilder(LINKS);
        attrLinks.setRequired(false).setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true).setMultiValued(true);
        domainObjectClass.addAttributeInfo(attrLinks.build());


    }

    public Uid createDomain(Set<Attribute> attributes) {
        LOG.info("Start createDomain, attributes: {0}", attributes);

        if (attributes == null || attributes.isEmpty()) {
            throw new InvalidAttributeValueException("attributes not provided or empty");
        }

        Domain domain = new KeystoneDomain();
        boolean set_required_attribute_name = false;

        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(Name.NAME)) {
                String domainName = AttributeUtil.getAsStringValue(attribute);
                if (!StringUtil.isBlank(domainName)) {
                    domain.toBuilder().name(domainName);
                    set_required_attribute_name = true;
                }
            }

            if (attribute.getName().equals(DESCRIPTION)) {
                domain.toBuilder().description(AttributeUtil.getAsStringValue(attribute));
            }

            if (attribute.getName().equals(OperationalAttributes.ENABLE_NAME)) {
                domain.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute));
            }
        }

        //domain name is not set or is empty
        if (!set_required_attribute_name) {
            throw new InvalidAttributeValueException("Missing value of required attribute name in Domain");
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

        ActionResponse deleteDomainResponse = os.identity().domains().delete(uid.getUidValue());
        if (!deleteDomainResponse.isSuccess()) {
            LOG.info("deleteDomain failed!");
            handleActionResponse(deleteDomainResponse);
        } else LOG.info("deleteDomain success!");

    }

    public void updateDomain(Uid uid, Set<Attribute> attributes) {

        OSClient.OSClientV3 os = authenticate(getConfiguration());

        Domain domain = os.identity().domains().get(uid.getUidValue());
        LOG.info("Domain is : {0}", domain);
        if (domain != null) {
            for (Attribute attribute : attributes) {
                if (attribute.getName().equals(OperationalAttributes.ENABLE_NAME)) {
                    domain = os.identity().domains().update(domain.toBuilder().enabled(AttributeUtil.getBooleanValue(attribute)).build());
                }
                if (attribute.getName().equals(Name.NAME)) {
                    domain = os.identity().domains().update(domain.toBuilder().name(AttributeUtil.getAsStringValue(attribute)).build());
                }
                if (attribute.getName().equals(DESCRIPTION)) {
                    domain = os.identity().domains().update(domain.toBuilder().description(AttributeUtil.getAsStringValue(attribute)).build());
                }
            }
        } else throw new UnknownUidException("Returned Domain object is null");

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

            } else if (((EqualsFilter) query).getAttribute() instanceof Name) {
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


    private void invalidAttributeValue(String attrName, Filter query) {
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
                builder.addAttribute(Name.NAME, domain.getName());
                builder.setName(domain.getName());
            }
            if (domain.getDescription() != null) {
                builder.addAttribute(DESCRIPTION, domain.getDescription());
            }
            if (domain.isEnabled()) {
                builder.addAttribute(OperationalAttributes.ENABLE_NAME, true);
            } else {
                builder.addAttribute(OperationalAttributes.ENABLE_NAME, false);
            }
            if (domain.getLinks() != null) {
                builder.addAttribute(LINKS, domain.getLinks());
            }

            ConnectorObject connectorObject = builder.build();
            handler.handle(connectorObject);

        } else throw new UnknownUidException("Returned Domain object is null");

    }


}