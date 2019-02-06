package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.framework.common.objects.*;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DomainPerformanceTests extends BasicConfigurationForTests {


    private Set<Uid> domainsUid = new HashSet<Uid>();


    @Test(priority = 30)
    public void Create500Domains() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        ObjectClass objectClassDomain = new ObjectClass("Domain");

        for (int i = 0; i < 500; i++) {

            Set<Attribute> attributesCreateDomain = new HashSet<Attribute>();
            attributesCreateDomain.add(AttributeBuilder.build("description", "The Lion King"));
            attributesCreateDomain.add(AttributeBuilder.build("__ENABLE__", true));
            attributesCreateDomain.add(AttributeBuilder.build("__NAME__", "Mufasa domain" + i));

            openStackConnector.init(configuration);
            Uid domainUid = openStackConnector.create(objectClassDomain, attributesCreateDomain, options);
            openStackConnector.dispose();
            domainsUid.add(domainUid);

        }

    }

    @Test(priority = 31)
    public void Update500DomainTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        ObjectClass objectClassDomain = new ObjectClass("Domain");
        int i = 0;
        for (Uid domainUid : domainsUid) {

            Set<Attribute> attributesUpdateDomain = new HashSet<Attribute>();
            attributesUpdateDomain.add(AttributeBuilder.build("description", "The Lion King - updated"));
            attributesUpdateDomain.add(AttributeBuilder.build("__NAME__", "Scar Domain " + i));
            attributesUpdateDomain.add(AttributeBuilder.build("__ENABLE__", false));

            openStackConnector.init(configuration);
            openStackConnector.update(objectClassDomain, domainUid, attributesUpdateDomain, options);
            openStackConnector.dispose();
            i++;
        }
    }

    @Test(priority = 34)
    public void Delete500GDomainTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();
        OpenStackConnectorConfiguration configuration = getConfiguration();
        ObjectClass objectClassDomain = new ObjectClass("Domain");
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        for (Uid domain : domainsUid) {
            openStackConnector.init(configuration);
            openStackConnector.delete(objectClassDomain, domain, options);
            openStackConnector.dispose();
        }
    }


}
