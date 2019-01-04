package com.evolveum.polygon.connector.openstackkeystone.rest;
import java.util.*;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

public class SchemaConsistencyTests extends BasicConfigurationForTests {


    @Test
    public void schemaTestGroupObjectClass(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);
        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Schema schema = openStackConnector.schema();

        Set<AttributeInfo> attributesInfoGroup = schema.findObjectClassInfo(ObjectClass.GROUP_NAME).getAttributeInfo();
        Set<Attribute> attributesGroup = new HashSet<Attribute>();

        for(AttributeInfo attributeInfo : attributesInfoGroup){
            if(!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()){
                if(attributeInfo.getName().equals("domain_id")){
                    attributesGroup.add(AttributeBuilder.build(attributeInfo.getName(),"default"));
                } else if(attributeInfo.getType().equals(String.class)){
                    attributesGroup.add(AttributeBuilder.build(attributeInfo.getName(),"GroupExample"));
                }
            }
        }

        ObjectClass objectClassGroup = ObjectClass.GROUP;
        Uid uidGroup = openStackConnector.create(objectClassGroup, attributesGroup, options);

        AttributeFilter filterGroup;
        filterGroup = (EqualsFilter) FilterBuilder.equalTo(uidGroup);

        final ArrayList<ConnectorObject> resultsGroup = new ArrayList<>();
        SearchResultsHandler handlerGroup = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsGroup.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.executeQuery(objectClassGroup, filterGroup, handlerGroup, options);

        try {
            if(!resultsGroup.get(0).getAttributes().containsAll(attributesGroup)){
                throw new InvalidAttributeValueException("Attributes of created group and searched group is not same.");
            }
        } finally {
            openStackConnector.delete(objectClassGroup, uidGroup, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void schemaTestProjectObjectClass(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);
        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Schema schema = openStackConnector.schema();

        Set<AttributeInfo> attributesInfoProject = schema.findObjectClassInfo("Project").getAttributeInfo();
        Set<Attribute> attributesProject = new HashSet<Attribute>();

        for(AttributeInfo attributeInfo : attributesInfoProject){
            if(!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()){
                if(attributeInfo.getName().equals("domain_id")){
                    attributesProject.add(AttributeBuilder.build(attributeInfo.getName(),"default"));
                } else if(attributeInfo.getName().equals("parent_id")){
                    attributesProject.add(AttributeBuilder.build(attributeInfo.getName(),"default"));
                } else if(attributeInfo.getType().equals(String.class)){
                    attributesProject.add(AttributeBuilder.build(attributeInfo.getName(),"ProjectExample"));
                }else if(attributeInfo.getType().equals(Boolean.class)){
                    attributesProject.add(AttributeBuilder.build(attributeInfo.getName(),true));
                }
            }
        }
        ObjectClass objectClassProject = new ObjectClass("Project");
        Uid uidProject = openStackConnector.create(objectClassProject, attributesProject, options);

        AttributeFilter filterProject;
        filterProject = (EqualsFilter) FilterBuilder.equalTo(uidProject);

        final ArrayList<ConnectorObject> resultsProject = new ArrayList<>();
        SearchResultsHandler handlerProject = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsProject.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.executeQuery(objectClassProject, filterProject, handlerProject, options);

        try {
            if(!resultsProject.get(0).getAttributes().containsAll(attributesProject)){
                throw new InvalidAttributeValueException("Attributes of created project and searched project is not same.");
            }
        } finally {
            openStackConnector.delete(objectClassProject, uidProject, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void schemaTestDomainObjectClass(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);
        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Schema schema = openStackConnector.schema();

        Set<AttributeInfo> attributesInfoDomain = schema.findObjectClassInfo("Domain").getAttributeInfo();
        Set<Attribute> attributesDomain = new HashSet<Attribute>();

        for(AttributeInfo attributeInfo : attributesInfoDomain){
            if(!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()){
                 if(attributeInfo.getType().equals(String.class)){
                    attributesDomain.add(AttributeBuilder.build(attributeInfo.getName(),"DomainExample"));
                }else if(attributeInfo.getType().equals(Boolean.class)){
                    attributesDomain.add(AttributeBuilder.build(attributeInfo.getName(),true));
                }
            }
        }
        ObjectClass objectClassDomain = new ObjectClass("Domain");
        Uid uidDomain = openStackConnector.create(objectClassDomain, attributesDomain, options);

        AttributeFilter filterDomain;
        filterDomain = (EqualsFilter) FilterBuilder.equalTo(uidDomain);

        final ArrayList<ConnectorObject> resultsDomain = new ArrayList<>();
        SearchResultsHandler handlerDomain = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsDomain.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.executeQuery(objectClassDomain, filterDomain, handlerDomain, options);

        try {
            if(!resultsDomain.get(0).getAttributes().containsAll(attributesDomain)){
                throw new InvalidAttributeValueException("Attributes of created domain and searched domain is not same.");
            }
        } finally {
            //to delete domain must disable first
            Set<Attribute> attributesAccount3 = new HashSet<>();
            attributesAccount3.add(AttributeBuilder.build("enabled", false));
            openStackConnector.update(objectClassDomain,uidDomain,attributesAccount3,options);

            openStackConnector.delete(objectClassDomain, uidDomain, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void schemaTestRoleObjectClass(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);
        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Schema schema = openStackConnector.schema();

        Set<AttributeInfo> attributesInfoRole = schema.findObjectClassInfo("Role").getAttributeInfo();
        Set<Attribute> attributesRole = new HashSet<Attribute>();

        for(AttributeInfo attributeInfo : attributesInfoRole){
            if(!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()){
                if(attributeInfo.getName().equals("domain_id")){
                    attributesRole.add(AttributeBuilder.build(attributeInfo.getName(),"default"));
                }  else if(attributeInfo.getType().equals(String.class)){
                    attributesRole.add(AttributeBuilder.build(attributeInfo.getName(),"RoleExample"));
                }
            }
        }
        ObjectClass objectClassRole = new ObjectClass("Role");
        Uid uidRole = openStackConnector.create(objectClassRole, attributesRole, options);

        AttributeFilter filterRole;
        filterRole = (EqualsFilter) FilterBuilder.equalTo(uidRole);

        final ArrayList<ConnectorObject> resultsRole = new ArrayList<>();
        SearchResultsHandler handlerRole = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsRole.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.executeQuery(objectClassRole, filterRole, handlerRole, options);

        try {
            if(!resultsRole.get(0).getAttributes().containsAll(attributesRole)){
                throw new InvalidAttributeValueException("Attributes of created role and searched role is not same.");
            }
        } finally {
            openStackConnector.delete(objectClassRole, uidRole, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void schemaTestAccountObjectClass(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);
        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Schema schema = openStackConnector.schema();

        Set<AttributeInfo> attributesInfoAccount = schema.findObjectClassInfo(ObjectClass.ACCOUNT_NAME).getAttributeInfo();
        Set<Attribute> attributesAccount = new HashSet<Attribute>();

        for(AttributeInfo attributeInfo : attributesInfoAccount){
            if(!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()){
                if(attributeInfo.getName().equals("domain_id")){
                    attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),"default"));
                } else if(attributeInfo.getType().equals(String.class)){
                    attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),"AccountExample"));
                }else if(attributeInfo.getType().equals(Boolean.class)){
                    attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),true));
                }
            }
        }
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
        Uid uidAccount = openStackConnector.create(objectClassAccount, attributesAccount, options);

        attributesAccount.remove(AttributeBuilder.build("password","AccountExample"));


        AttributeFilter filterAccount;
        filterAccount = (EqualsFilter) FilterBuilder.equalTo(uidAccount);

        final ArrayList<ConnectorObject> resultsAccount = new ArrayList<>();
        SearchResultsHandler handlerAccount = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsAccount.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.executeQuery(objectClassAccount, filterAccount, handlerAccount, options);

        try {
            if(!resultsAccount.get(0).getAttributes().containsAll(attributesAccount)){
                throw new InvalidAttributeValueException("Attributes of created account and searched account is not same.");
            }
        } finally {
            openStackConnector.delete(objectClassAccount, uidAccount, options);
            openStackConnector.dispose();
        }
    }

}
