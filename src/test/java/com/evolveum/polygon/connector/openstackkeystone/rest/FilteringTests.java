package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.common.security.GuardedString;
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

public class FilteringTests extends BasicConfigurationForTests {

    @Test
    public void filteringTestAccountObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;

        Set<Attribute> attributesFirstCreateAccount = new HashSet<Attribute>();
        attributesFirstCreateAccount.add(AttributeBuilder.build("description", "The Lion King"));
        attributesFirstCreateAccount.add(AttributeBuilder.build("enabled", true));
        attributesFirstCreateAccount.add(AttributeBuilder.build("domain_id", "default"));
        attributesFirstCreateAccount.add(AttributeBuilder.build("__NAME__", "Simba"));
        attributesFirstCreateAccount.add(AttributeBuilder.build("default_project_id", "project"));
        attributesFirstCreateAccount.add(AttributeBuilder.build("email", "nomail"));
        GuardedString pass = new GuardedString("5tr0ngp4ssw0rd".toCharArray());
        attributesFirstCreateAccount.add(AttributeBuilder.build("__PASSWORD__", pass));

        Uid simbaUid = openStackConnector.create(objectClassAccount, attributesFirstCreateAccount, options);

        Set<Attribute> attributesSecondCreateAccount = new HashSet<Attribute>();
        attributesSecondCreateAccount.add(AttributeBuilder.build("description", "The Lion King"));
        attributesSecondCreateAccount.add(AttributeBuilder.build("enabled", true));
        attributesSecondCreateAccount.add(AttributeBuilder.build("domain_id", "default"));
        attributesSecondCreateAccount.add(AttributeBuilder.build("__NAME__", "Scar"));
        attributesSecondCreateAccount.add(AttributeBuilder.build("default_project_id", "project"));
        attributesSecondCreateAccount.add(AttributeBuilder.build("email", "nomail"));
        attributesSecondCreateAccount.add(AttributeBuilder.build("__PASSWORD__", pass));

        Uid scarUid = openStackConnector.create(objectClassAccount, attributesSecondCreateAccount, options);

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

        AttributeFilter equalsFilterAccount1;
        equalsFilterAccount1 = (EqualsFilter) FilterBuilder.equalTo(AttributeBuilder.build(Name.NAME, "Simba"));

        openStackConnector.executeQuery(objectClassAccount, equalsFilterAccount1, handlerAccount, options);

        try {
            if (!resultsAccount.get(0).getAttributes().contains(simbaUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched user by Name");
            }
        } catch (Exception e) {
            openStackConnector.delete(objectClassAccount, simbaUid, options);
            openStackConnector.delete(objectClassAccount, scarUid, options);
            openStackConnector.dispose();

        }

        resultsAccount.clear();

        AttributeFilter equalsFilterAccount2;
        equalsFilterAccount2 = (EqualsFilter) FilterBuilder.equalTo(scarUid);

        openStackConnector.executeQuery(objectClassAccount, equalsFilterAccount2, handlerAccount, options);

        try {
            if (!resultsAccount.get(0).getAttributes().contains(scarUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched user by UID.");
            }
        } finally {
            openStackConnector.delete(objectClassAccount, simbaUid, options);
            openStackConnector.delete(objectClassAccount, scarUid, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void filteringTestGroupObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassGroup = ObjectClass.GROUP;

        Set<Attribute> attributesFirstCreateGroup = new HashSet<Attribute>();
        attributesFirstCreateGroup.add(AttributeBuilder.build("description", "The Lion King"));
        attributesFirstCreateGroup.add(AttributeBuilder.build("__NAME__", "Timon Group"));
        attributesFirstCreateGroup.add(AttributeBuilder.build("domain_id", "default"));

        Uid timonGroupUid = openStackConnector.create(objectClassGroup, attributesFirstCreateGroup, options);

        Set<Attribute> attributesSecondCreateGroup = new HashSet<Attribute>();
        attributesSecondCreateGroup.add(AttributeBuilder.build("description", "The Lion King"));
        attributesSecondCreateGroup.add(AttributeBuilder.build("__NAME__", "Pumbaa Group"));
        attributesSecondCreateGroup.add(AttributeBuilder.build("domain_id", "default"));

        Uid pumbaaGroupUid = openStackConnector.create(objectClassGroup, attributesSecondCreateGroup, options);

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

        AttributeFilter equalsFilterGroup1;
        equalsFilterGroup1 = (EqualsFilter) FilterBuilder.equalTo(AttributeBuilder.build(Name.NAME, "Timon Group"));

        openStackConnector.executeQuery(objectClassGroup, equalsFilterGroup1, handlerGroup, options);

        try {
            if (!resultsGroup.get(0).getAttributes().contains(timonGroupUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched group by Name");
            }
        } catch (Exception e) {
            openStackConnector.delete(objectClassGroup, timonGroupUid, options);
            openStackConnector.delete(objectClassGroup, pumbaaGroupUid, options);
            openStackConnector.dispose();

        }

        resultsGroup.clear();

        AttributeFilter equalsFilterGroup2;
        equalsFilterGroup2 = (EqualsFilter) FilterBuilder.equalTo(pumbaaGroupUid);

        openStackConnector.executeQuery(objectClassGroup, equalsFilterGroup2, handlerGroup, options);

        try {
            if (!resultsGroup.get(0).getAttributes().contains(pumbaaGroupUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched group by UID.");
            }
        } finally {
            openStackConnector.delete(objectClassGroup, timonGroupUid, options);
            openStackConnector.delete(objectClassGroup, pumbaaGroupUid, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void filteringTestDomainObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassDomain = new ObjectClass("Domain");

        Set<Attribute> attributesFirstCreateDomain = new HashSet<Attribute>();
        attributesFirstCreateDomain.add(AttributeBuilder.build("description", "The Lion King"));
        attributesFirstCreateDomain.add(AttributeBuilder.build("__NAME__", "Nala Domain"));
        attributesFirstCreateDomain.add(AttributeBuilder.build("enabled", false));

        Uid nalaDomainUid = openStackConnector.create(objectClassDomain, attributesFirstCreateDomain, options);

        Set<Attribute> attributesSecondCreateDomain = new HashSet<Attribute>();
        attributesSecondCreateDomain.add(AttributeBuilder.build("description", "The Lion King"));
        attributesSecondCreateDomain.add(AttributeBuilder.build("__NAME__", "Musafa Domain"));
        attributesSecondCreateDomain.add(AttributeBuilder.build("enabled", false));

        Uid musafaDomainUid = openStackConnector.create(objectClassDomain, attributesSecondCreateDomain, options);

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

        AttributeFilter equalsFilterDomain1;
        equalsFilterDomain1 = (EqualsFilter) FilterBuilder.equalTo(AttributeBuilder.build(Name.NAME, "Nala Domain"));

        openStackConnector.executeQuery(objectClassDomain, equalsFilterDomain1, handlerDomain, options);

        try {
            if (!resultsDomain.get(0).getAttributes().contains(nalaDomainUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched domain by Name");
            }
        } catch (Exception e) {
            openStackConnector.delete(objectClassDomain, nalaDomainUid, options);
            openStackConnector.delete(objectClassDomain, musafaDomainUid, options);
            openStackConnector.dispose();

        }

        resultsDomain.clear();

        AttributeFilter equalsFilterDomain2;
        equalsFilterDomain2 = (EqualsFilter) FilterBuilder.equalTo(musafaDomainUid);

        openStackConnector.executeQuery(objectClassDomain, equalsFilterDomain2, handlerDomain, options);

        try {
            if (!resultsDomain.get(0).getAttributes().contains(musafaDomainUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched domain by UID.");
            }
        } finally {
            openStackConnector.delete(objectClassDomain, nalaDomainUid, options);
            openStackConnector.delete(objectClassDomain, musafaDomainUid, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void filteringTestProjectObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassProject = new ObjectClass("Project");

        Set<Attribute> attributesFirstCreateProject = new HashSet<Attribute>();
        attributesFirstCreateProject.add(AttributeBuilder.build("description", "The Lion King"));
        attributesFirstCreateProject.add(AttributeBuilder.build("__NAME__", "Zazu Project"));
        attributesFirstCreateProject.add(AttributeBuilder.build("enabled", true));
        attributesFirstCreateProject.add(AttributeBuilder.build("domain_id", "default"));
        attributesFirstCreateProject.add(AttributeBuilder.build("parent_id", "default"));

        Uid zazuProjectUid = openStackConnector.create(objectClassProject, attributesFirstCreateProject, options);

        Set<Attribute> attributesSecondCreateProject = new HashSet<Attribute>();
        attributesSecondCreateProject.add(AttributeBuilder.build("description", "The Lion King"));
        attributesSecondCreateProject.add(AttributeBuilder.build("__NAME__", "Rafiki Project"));
        attributesSecondCreateProject.add(AttributeBuilder.build("enabled", true));
        attributesSecondCreateProject.add(AttributeBuilder.build("domain_id", "default"));
        attributesSecondCreateProject.add(AttributeBuilder.build("parent_id", "default"));

        Uid rafikiProjectUid = openStackConnector.create(objectClassProject, attributesSecondCreateProject, options);

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

        AttributeFilter equalsFilterProject1;
        equalsFilterProject1 = (EqualsFilter) FilterBuilder.equalTo(AttributeBuilder.build(Name.NAME, "Zazu Project"));

        openStackConnector.executeQuery(objectClassProject, equalsFilterProject1, handlerProject, options);

        try {
            if (!resultsProject.get(0).getAttributes().contains(zazuProjectUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched project by Name");
            }
        } catch (Exception e) {
            openStackConnector.delete(objectClassProject, zazuProjectUid, options);
            openStackConnector.delete(objectClassProject, rafikiProjectUid, options);
            openStackConnector.dispose();

        }

        resultsProject.clear();

        AttributeFilter equalsFilterProject2;
        equalsFilterProject2 = (EqualsFilter) FilterBuilder.equalTo(rafikiProjectUid);

        openStackConnector.executeQuery(objectClassProject, equalsFilterProject2, handlerProject, options);

        try {
            if (!resultsProject.get(0).getAttributes().contains(rafikiProjectUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched project by UID.");
            }
        } finally {
            openStackConnector.delete(objectClassProject, zazuProjectUid, options);
            openStackConnector.delete(objectClassProject, rafikiProjectUid, options);
            openStackConnector.dispose();
        }
    }

    @Test
    public void filteringTestRoleObjectClass() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        openStackConnector.init(configuration);

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassRole = new ObjectClass("Role");

        Set<Attribute> attributesFirstCreateRole = new HashSet<Attribute>();
        //  attributesFirstCreateRole.add(AttributeBuilder.build("description", "The Lion King"));
        attributesFirstCreateRole.add(AttributeBuilder.build("__NAME__", "Sarabi Role"));

        Uid sarabiRoleUid = openStackConnector.create(objectClassRole, attributesFirstCreateRole, options);

        Set<Attribute> attributesSecondCreateRole = new HashSet<Attribute>();
        // attributesSecondCreateRole.add(AttributeBuilder.build("description", "The Lion King"));
        attributesSecondCreateRole.add(AttributeBuilder.build("__NAME__", "Sarafina Role"));

        Uid sarafinaRoleUid = openStackConnector.create(objectClassRole, attributesSecondCreateRole, options);

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

        AttributeFilter equalsFilterRole1;
        equalsFilterRole1 = (EqualsFilter) FilterBuilder.equalTo(AttributeBuilder.build(Name.NAME, "Sarabi Role"));

        openStackConnector.executeQuery(objectClassRole, equalsFilterRole1, handlerRole, options);

        try {
            if (!resultsRole.get(0).getAttributes().contains(sarabiRoleUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched role by Name");
            }
        } catch (Exception e) {
            openStackConnector.delete(objectClassRole, sarabiRoleUid, options);
            openStackConnector.delete(objectClassRole, sarafinaRoleUid, options);
            openStackConnector.dispose();

        }

        resultsRole.clear();

        AttributeFilter equalsFilterRole2;
        equalsFilterRole2 = (EqualsFilter) FilterBuilder.equalTo(sarafinaRoleUid);

        openStackConnector.executeQuery(objectClassRole, equalsFilterRole2, handlerRole, options);

        try {
            if (!resultsRole.get(0).getAttributes().contains(sarafinaRoleUid)) {
                throw new InvalidAttributeValueException("EqualsFilter not return searched role by UID.");
            }
        } finally {
            openStackConnector.delete(objectClassRole, sarabiRoleUid, options);
            openStackConnector.delete(objectClassRole, sarafinaRoleUid, options);
            openStackConnector.dispose();
        }
    }


}
