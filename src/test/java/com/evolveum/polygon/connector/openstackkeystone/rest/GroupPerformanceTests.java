package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

import java.util.*;


public class GroupPerformanceTests extends BasicConfigurationForTests {

    private Uid simbaUid;
    private Set<Uid> groupsUid = new HashSet<Uid>();


    @Test(priority = 30)
    public void Create500Groups() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        ObjectClass objectClassGroup = ObjectClass.GROUP;

        for (int i = 0; i < 500; i++) {

            Set<Attribute> attributesCreatedGroup = new HashSet<Attribute>();
            attributesCreatedGroup.add(AttributeBuilder.build("__NAME__", "Name testGroupPer" + i));
            attributesCreatedGroup.add(AttributeBuilder.build("description", "The Lion King"));
            attributesCreatedGroup.add(AttributeBuilder.build("domain_id", "default"));
            openStackConnector.init(configuration);
            Uid groupUid = openStackConnector.create(objectClassGroup, attributesCreatedGroup, options);
            openStackConnector.dispose();
            groupsUid.add(groupUid);

        }

    }

    @Test(priority = 31)
    public void Update500GroupsTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        ObjectClass objectClassGroup = ObjectClass.GROUP;
        int i = 0;
        for (Uid groupUid : groupsUid) {

            Set<Attribute> attributesUpdateGroup = new HashSet<Attribute>();
            attributesUpdateGroup.add(AttributeBuilder.build("description", "The Lion King - updated"));
            attributesUpdateGroup.add(AttributeBuilder.build("__NAME__", "Name testGroupPer" + i + " Update"));
            openStackConnector.init(configuration);
            openStackConnector.update(objectClassGroup, groupUid, attributesUpdateGroup, options);
            openStackConnector.dispose();
            i++;
        }
    }

    @Test(priority = 32)
    public void CreateUserAndAddUserToEachGroupTest() {
        OpenStackConnector gitlabRestConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        ObjectClass objectClassGroup = ObjectClass.GROUP;
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;

        Set<Attribute> attributesAccount = new HashSet<Attribute>();
        attributesAccount.add(AttributeBuilder.build("email", "testUserPer@performance.com"));
        GuardedString pass = new GuardedString("TestPassword".toCharArray());
        attributesAccount.add(AttributeBuilder.build("__PASSWORD__", pass));
        attributesAccount.add(AttributeBuilder.build("__NAME__", "Simba King"));

        gitlabRestConnector.init(configuration);
        simbaUid = gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
        gitlabRestConnector.dispose();
        Set<Attribute> simbaAttributeUid = new HashSet<Attribute>(Collections.singleton(simbaUid));
        for (Uid groupUid : groupsUid) {
            gitlabRestConnector.init(configuration);
            gitlabRestConnector.addAttributeValues(objectClassGroup, groupUid, simbaAttributeUid, options);
            gitlabRestConnector.dispose();
        }
    }


    @Test(priority = 33)
    public void SearchGroupsWithContainsAllValuesFilterTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();
        OpenStackConnectorConfiguration configuration = getConfiguration();
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        AttributeFilter equalsFilter;
        equalsFilter = (EqualsFilter) FilterBuilder.equalTo(simbaUid);

        final ArrayList<ConnectorObject> resultsUser = new ArrayList<>();
        SearchResultsHandler handlerUser = new SearchResultsHandler() {

            @Override
            public boolean handle(ConnectorObject connectorObject) {
                resultsUser.add(connectorObject);
                return true;
            }

            @Override
            public void handleResult(SearchResult result) {
            }
        };

        openStackConnector.init(configuration);
        openStackConnector.executeQuery(objectClassAccount, equalsFilter, handlerUser, options);
        openStackConnector.dispose();

        System.out.println(resultsUser.get(0).getAttributes());

        if (resultsUser.size() == 0 || resultsUser.get(0).getAttributeByName("usergroups") == null || resultsUser.get(0).getAttributeByName("usergroups").getValue().size() != 500) {
            throw new InvalidAttributeValueException("Group doesn't 500 members.");
        }
    }

    @Test(priority = 34)
    public void Delete500GroupAndUserTest() {
        OpenStackConnector gitlabRestConnector = new OpenStackConnector();
        OpenStackConnectorConfiguration configuration = getConfiguration();
        ObjectClass objectClassGroup = ObjectClass.GROUP;
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        for (Uid group : groupsUid) {
            gitlabRestConnector.init(configuration);
            gitlabRestConnector.delete(objectClassGroup, group, options);
            gitlabRestConnector.dispose();
        }

        gitlabRestConnector.init(configuration);
        gitlabRestConnector.delete(objectClassAccount, simbaUid, options);
        gitlabRestConnector.dispose();


    }
}


