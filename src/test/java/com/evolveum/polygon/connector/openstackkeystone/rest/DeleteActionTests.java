package com.evolveum.polygon.connector.openstackkeystone.rest;


import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
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
import java.util.concurrent.TimeUnit;


public class DeleteActionTests extends BasicConfigurationForTests {

    @Test(expectedExceptions = UnknownUidException.class)
    public void deleteGroupTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        Set<Attribute> attributesCreatedGroup = new HashSet<Attribute>();
        attributesCreatedGroup.add(AttributeBuilder.build("__NAME__", "Timon"));
        attributesCreatedGroup.add(AttributeBuilder.build("description", "Pumbaa"));

        ObjectClass objectClassGroup = ObjectClass.GROUP;

        Uid timonUid = openStackConnector.create(objectClassGroup, attributesCreatedGroup, options);

        openStackConnector.delete(objectClassGroup, timonUid, options);

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

        try {
            AttributeFilter equalsFilter;
            equalsFilter = (EqualsFilter) FilterBuilder.equalTo(timonUid);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            openStackConnector.executeQuery(objectClassGroup, equalsFilter, handlerGroup, options);
        } finally {
            openStackConnector.dispose();
        }
    }

    @Test(expectedExceptions = UnknownUidException.class)
    public void deleteProjectTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        Set<Attribute> attributesCreatedProject = new HashSet<Attribute>();
        attributesCreatedProject.add(AttributeBuilder.build("__NAME__", "Zazu"));
        attributesCreatedProject.add(AttributeBuilder.build("description", "Cartoon"));

        ObjectClass objectClassProject = new ObjectClass("Project");

        Uid zazuUid = openStackConnector.create(objectClassProject, attributesCreatedProject, options);

        openStackConnector.delete(objectClassProject, zazuUid, options);

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

        try {
            AttributeFilter equalsFilter;
            equalsFilter = (EqualsFilter) FilterBuilder.equalTo(zazuUid);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            openStackConnector.executeQuery(objectClassProject, equalsFilter, handlerProject, options);
        } finally {
            openStackConnector.dispose();
        }
    }


    @Test(expectedExceptions = UnknownUidException.class)
    public void deleteDomainTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        Set<Attribute> attributesCreatedDomain = new HashSet<Attribute>();
        attributesCreatedDomain.add(AttributeBuilder.build("__NAME__", "Rafiki"));
        attributesCreatedDomain.add(AttributeBuilder.build("description", "Cartoon"));

        ObjectClass objectClassDomain = new ObjectClass("Domain");

        Uid zazuUid = openStackConnector.create(objectClassDomain, attributesCreatedDomain, options);

        openStackConnector.delete(objectClassDomain, zazuUid, options);

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

        try {
            AttributeFilter equalsFilter;
            equalsFilter = (EqualsFilter) FilterBuilder.equalTo(zazuUid);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            openStackConnector.executeQuery(objectClassDomain, equalsFilter, handlerDomain, options);
        } finally {
            openStackConnector.dispose();
        }
    }

    @Test(expectedExceptions = UnknownUidException.class)
    public void deleteRoleTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        Set<Attribute> attributesCreatedRole = new HashSet<Attribute>();
        attributesCreatedRole.add(AttributeBuilder.build("__NAME__", "Shenzi"));
        attributesCreatedRole.add(AttributeBuilder.build("description", "Cartoon"));

        ObjectClass objectClassRole = new ObjectClass("Role");

        Uid shenziUid = openStackConnector.create(objectClassRole, attributesCreatedRole, options);

        openStackConnector.delete(objectClassRole, shenziUid, options);

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

        try {
            AttributeFilter equalsFilter;
            equalsFilter = (EqualsFilter) FilterBuilder.equalTo(shenziUid);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            openStackConnector.executeQuery(objectClassRole, equalsFilter, handlerRole, options);
        } finally {
            openStackConnector.dispose();
        }
    }

    @Test(expectedExceptions = UnknownUidException.class)
    public void deleteUserTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        Set<Attribute> attributesCreatedUser = new HashSet<Attribute>();
        attributesCreatedUser.add(AttributeBuilder.build("__NAME__", "Banzai"));
        attributesCreatedUser.add(AttributeBuilder.build("description", "Cartoon"));
        attributesCreatedUser.add(AttributeBuilder.build("enabled", true));
//        attributesCreatedUser.add(AttributeBuilder.build("password", "LionKing99"));
        attributesCreatedUser.add(AttributeBuilder.build("domain_id", "default"));
        attributesCreatedUser.add(AttributeBuilder.build("default_project_id", "project"));
        attributesCreatedUser.add(AttributeBuilder.build("email", "lionking@mail.com"));

        GuardedString pass = new GuardedString("LionKing99".toCharArray());
        attributesCreatedUser.add(AttributeBuilder.build("__PASSWORD__", pass));

        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;

        Uid banzaiUid = openStackConnector.create(objectClassAccount, attributesCreatedUser, options);

        openStackConnector.delete(objectClassAccount, banzaiUid, options);

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

        try {
            AttributeFilter equalsFilter;
            equalsFilter = (EqualsFilter) FilterBuilder.equalTo(banzaiUid);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            openStackConnector.executeQuery(objectClassAccount, equalsFilter, handlerAccount, options);
        } finally {
            openStackConnector.dispose();
        }
    }

}
