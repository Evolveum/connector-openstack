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

public class UserPerformanceTests extends BasicConfigurationForTests {

    private Uid prideRockUid;
    private Set<Uid> usersUid = new HashSet<Uid>();

    @Test(priority = 20)
    public void CreateGroupAnd500UsersTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        Set<Attribute> attributesCreatedGroup = new HashSet<Attribute>();
        attributesCreatedGroup.add(AttributeBuilder.build("__NAME__", "test group"));

        ObjectClass objectClassGroup = ObjectClass.GROUP;
        openStackConnector.init(configuration);
        prideRockUid = openStackConnector.create(objectClassGroup, attributesCreatedGroup, options);
        openStackConnector.dispose();

        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;

        for (int i = 0; i < 500; i++) {
            Set<Attribute> attributes = new HashSet<Attribute>();
            attributes.add(AttributeBuilder.build("email", "testUserPer" + i + "@performance.com"));
            GuardedString pass = new GuardedString(("testUserPer" + i).toCharArray());
            attributes.add(AttributeBuilder.build("__PASSWORD__", pass));
            attributes.add(AttributeBuilder.build("__NAME__", "testUserPer" + i));
            openStackConnector.init(configuration);
            Uid userUid = openStackConnector.create(objectClassAccount, attributes, options);
            openStackConnector.dispose();
            usersUid.add(userUid);

        }

    }


    @Test(priority = 21)
    public void Update500UsersTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;

        int i = 0;
        for (Uid user : usersUid) {
            Set<Attribute> attributesUpdateAccount = new HashSet<Attribute>();
            attributesUpdateAccount.add(AttributeBuilder.build("description", "The Lion King - updated"));
            attributesUpdateAccount.add(AttributeBuilder.build("__ENABLE__", false));
            attributesUpdateAccount.add(AttributeBuilder.build("__NAME__", "Kovu" + i));
            attributesUpdateAccount.add(AttributeBuilder.build("email", "kovu@lion.com"));

            openStackConnector.init(configuration);
            openStackConnector.update(objectClassAccount, user, attributesUpdateAccount, options);
            openStackConnector.dispose();
            i++;
        }

    }

    @Test(priority = 22)
    public void CreateMembershipToGroupFor500UsersTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
        Set<Attribute> simbaAttributeUid = new HashSet<Attribute>();
        simbaAttributeUid.add(AttributeBuilder.build("usergroups", prideRockUid.getUidValue()));

        for (Uid userUid : usersUid) {
            openStackConnector.init(configuration);
            openStackConnector.addAttributeValues(objectClassAccount, userUid, simbaAttributeUid, options);
            openStackConnector.dispose();
        }


    }


    @Test(priority = 23)
    public void SearchGroupWith500UsersTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();
        OpenStackConnectorConfiguration configuration = getConfiguration();
        ObjectClass objectClassGroup = ObjectClass.GROUP;

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        AttributeFilter equalsFilter;
        equalsFilter = (EqualsFilter) FilterBuilder.equalTo(prideRockUid);

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

        openStackConnector.init(configuration);
        openStackConnector.executeQuery(objectClassGroup, equalsFilter, handlerGroup, options);
        openStackConnector.dispose();

        if (resultsGroup.size() == 0 || resultsGroup.get(0).getAttributeByName("group_members") == null || resultsGroup.get(0).getAttributeByName("group_members").getValue().size() != 500) {
            throw new InvalidAttributeValueException("Group doesn't 500 members.");
        }
    }


    @Test(priority = 24)
    public void DeleteMembershipFromGroupFor500UsersTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;


        Set<Attribute> simbaAttributeUid = new HashSet<Attribute>();
        simbaAttributeUid.add(AttributeBuilder.build("usergroups", prideRockUid.getUidValue()));

        for (Uid userUid : usersUid) {
            openStackConnector.init(configuration);
            openStackConnector.removeAttributeValues(objectClassAccount, userUid, simbaAttributeUid, options);
            openStackConnector.dispose();
        }

    }

    @Test(priority = 25)
    public void SearchGroupWithZeroUsersTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();
        OpenStackConnectorConfiguration configuration = getConfiguration();
        ObjectClass objectClassGroup = ObjectClass.GROUP;

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        AttributeFilter equalsFilter;
        equalsFilter = (EqualsFilter) FilterBuilder.equalTo(prideRockUid);

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

        openStackConnector.init(configuration);
        openStackConnector.executeQuery(objectClassGroup, equalsFilter, handlerGroup, options);
        openStackConnector.dispose();


        if ((resultsGroup.size() == 0) || (resultsGroup.get(0).getAttributeByName("group_members") == null) || (resultsGroup.get(0).getAttributeByName("group_members").getValue().size() != 0)) {
            throw new InvalidAttributeValueException("Group doesn't 0 members.");
        }

    }

    @Test(priority = 26)
    public void DeleteGroupWith500UsersAnd500UsersTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();
        OpenStackConnectorConfiguration configuration = getConfiguration();
        ObjectClass objectClassGroup = ObjectClass.GROUP;
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        openStackConnector.init(configuration);
        openStackConnector.delete(objectClassGroup, prideRockUid, options);
        openStackConnector.dispose();

        for (Uid user : usersUid) {
            openStackConnector.init(configuration);
            openStackConnector.delete(objectClassAccount, user, options);
            openStackConnector.dispose();
        }
    }
}
