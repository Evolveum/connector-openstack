package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UpdateTests extends BasicConfigurationForTests {


    @Test
    public void updateTestAccountObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;

        Set<Attribute> attributesCreateAccount = new HashSet<Attribute>();
        attributesCreateAccount.add(AttributeBuilder.build("description", "The Lion King"));
        attributesCreateAccount.add(AttributeBuilder.build("enabled", true));
        attributesCreateAccount.add(AttributeBuilder.build("domain_id", "default"));
        attributesCreateAccount.add(AttributeBuilder.build("__NAME__", "Kiara"));
        attributesCreateAccount.add(AttributeBuilder.build("default_project_id", "project"));
        attributesCreateAccount.add(AttributeBuilder.build("email", "kiara@lion.com"));

        Uid accountUid = openStackConnector.create(objectClassAccount, attributesCreateAccount, options);

        Set<Attribute> attributesUpdateAccount = new HashSet<Attribute>();
        attributesUpdateAccount.add(AttributeBuilder.build("description", "The Lion King - updated"));
        attributesUpdateAccount.add(AttributeBuilder.build("enabled", false));
        attributesUpdateAccount.add(AttributeBuilder.build("__NAME__", "Kovu"));
        attributesUpdateAccount.add(AttributeBuilder.build("email", "kovu@lion.com"));

        try {
            openStackConnector.update(objectClassAccount, accountUid, attributesUpdateAccount, options);
        } catch (Exception e) {
            openStackConnector.delete(objectClassAccount, accountUid, options);
            openStackConnector.dispose();
        }


        AttributeFilter filterAccount;
        filterAccount = (EqualsFilter) FilterBuilder.equalTo(accountUid);

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
            if (!resultsAccount.get(0).getAttributes().containsAll(attributesUpdateAccount)) {
                throw new InvalidAttributeValueException("Attributes of created user and searched user is not same.");
            }
        } finally {
            openStackConnector.delete(objectClassAccount, accountUid, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void updateTestGroupObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassGroup = ObjectClass.GROUP;

        Set<Attribute> attributesCreateGroup = new HashSet<Attribute>();
        attributesCreateGroup.add(AttributeBuilder.build("description", "The Lion King"));
        attributesCreateGroup.add(AttributeBuilder.build("domain_id", "default"));
        attributesCreateGroup.add(AttributeBuilder.build("__NAME__", "Shenzi Group"));


        Uid groupUid = openStackConnector.create(objectClassGroup, attributesCreateGroup, options);

        Set<Attribute> attributesUpdateGroup = new HashSet<Attribute>();
        attributesUpdateGroup.add(AttributeBuilder.build("description", "The Lion King - updated"));
        attributesUpdateGroup.add(AttributeBuilder.build("__NAME__", "Banzai Group"));

        try {
            openStackConnector.update(objectClassGroup, groupUid, attributesUpdateGroup, options);
        } catch (Exception e) {
            openStackConnector.delete(objectClassGroup, groupUid, options);
            openStackConnector.dispose();
        }


        AttributeFilter filterGroup;
        filterGroup = (EqualsFilter) FilterBuilder.equalTo(groupUid);

        final ArrayList<ConnectorObject> resultsAccount = new ArrayList<>();
        SearchResultsHandler handlerGroup = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsAccount.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.executeQuery(objectClassGroup, filterGroup, handlerGroup, options);

        try {
            if (!resultsAccount.get(0).getAttributes().containsAll(attributesUpdateGroup)) {
                throw new InvalidAttributeValueException("Attributes of created group and searched group is not same.");
            }
        } finally {
            openStackConnector.delete(objectClassGroup, groupUid, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void updateTestDomainObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassDomain = new ObjectClass("Domain");

        Set<Attribute> attributesCreateDomain = new HashSet<Attribute>();
        attributesCreateDomain.add(AttributeBuilder.build("description", "The Lion King"));
        attributesCreateDomain.add(AttributeBuilder.build("enabled", true));
        attributesCreateDomain.add(AttributeBuilder.build("__NAME__", "Ed Domain"));


        Uid domainUid = openStackConnector.create(objectClassDomain, attributesCreateDomain, options);

        Set<Attribute> attributesUpdateDomain = new HashSet<Attribute>();
        attributesUpdateDomain.add(AttributeBuilder.build("description", "The Lion King - updated"));
        attributesUpdateDomain.add(AttributeBuilder.build("__NAME__", "Gopher Group"));
        attributesUpdateDomain.add(AttributeBuilder.build("enabled", false));

        try {
            openStackConnector.update(objectClassDomain, domainUid, attributesUpdateDomain, options);
        } catch (Exception e) {
            openStackConnector.delete(objectClassDomain, domainUid, options);
            openStackConnector.dispose();
        }


        AttributeFilter filterDomain;
        filterDomain = (EqualsFilter) FilterBuilder.equalTo(domainUid);

        final ArrayList<ConnectorObject> resultsAccount = new ArrayList<>();
        SearchResultsHandler handlerDomain = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsAccount.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.executeQuery(objectClassDomain, filterDomain, handlerDomain, options);

        try {
            if (!resultsAccount.get(0).getAttributes().containsAll(attributesUpdateDomain)) {
                throw new InvalidAttributeValueException("Attributes of created domain and searched domain is not same.");
            }
        } finally {
            openStackConnector.delete(objectClassDomain, domainUid, options);
            openStackConnector.dispose();
        }
    }


    @Test
    public void updateTestProjectObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassProject = new ObjectClass("Project");

        Set<Attribute> attributesCreateProject = new HashSet<Attribute>();
        attributesCreateProject.add(AttributeBuilder.build("description", "The Lion King"));
        attributesCreateProject.add(AttributeBuilder.build("enabled", true));
        attributesCreateProject.add(AttributeBuilder.build("__NAME__", "Ed Project"));
        attributesCreateProject.add(AttributeBuilder.build("domain_id", "default"));


        Uid projectUid = openStackConnector.create(objectClassProject, attributesCreateProject, options);

        Set<Attribute> attributesUpdateProject = new HashSet<Attribute>();
        attributesUpdateProject.add(AttributeBuilder.build("description", "The Lion King - updated"));
        attributesUpdateProject.add(AttributeBuilder.build("__NAME__", "Gopher Group"));
        attributesUpdateProject.add(AttributeBuilder.build("enabled", false));

        try {
            openStackConnector.update(objectClassProject, projectUid, attributesUpdateProject, options);
        } catch (Exception e) {
            openStackConnector.delete(objectClassProject, projectUid, options);
            openStackConnector.dispose();
        }


        AttributeFilter filterProject;
        filterProject = (EqualsFilter) FilterBuilder.equalTo(projectUid);

        final ArrayList<ConnectorObject> resultsAccount = new ArrayList<>();
        SearchResultsHandler handlerProject = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsAccount.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.executeQuery(objectClassProject, filterProject, handlerProject, options);

        try {
            if (!resultsAccount.get(0).getAttributes().containsAll(attributesUpdateProject)) {
                throw new InvalidAttributeValueException("Attributes of created project and searched project is not same.");
            }
        } finally {
            openStackConnector.delete(objectClassProject, projectUid, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void updateTestRoleObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassRole = new ObjectClass("Role");

        Set<Attribute> attributesCreateRole = new HashSet<Attribute>();
        //    attributesCreateRole.add(AttributeBuilder.build("description", "The Lion King"));
        attributesCreateRole.add(AttributeBuilder.build("__NAME__", "Quint"));


        Uid roleUid = openStackConnector.create(objectClassRole, attributesCreateRole, options);

        Set<Attribute> attributesUpdateRole = new HashSet<Attribute>();
        //   attributesUpdateRole.add(AttributeBuilder.build("description", "The Lion King - updated"));
        attributesUpdateRole.add(AttributeBuilder.build("__NAME__", "Speedy the Snail"));

        try {
            openStackConnector.update(objectClassRole, roleUid, attributesUpdateRole, options);
        } catch (Exception e) {
            openStackConnector.delete(objectClassRole, roleUid, options);
            openStackConnector.dispose();
        }


        AttributeFilter filterRole;
        filterRole = (EqualsFilter) FilterBuilder.equalTo(roleUid);

        final ArrayList<ConnectorObject> resultsAccount = new ArrayList<>();
        SearchResultsHandler handlerRole = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsAccount.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.executeQuery(objectClassRole, filterRole, handlerRole, options);

        try {
            if (!resultsAccount.get(0).getAttributes().containsAll(attributesUpdateRole)) {
                throw new InvalidAttributeValueException("Attributes of created role and searched role is not same.");
            }
        } finally {
            openStackConnector.delete(objectClassRole, roleUid, options);
            openStackConnector.dispose();
        }
    }

}
