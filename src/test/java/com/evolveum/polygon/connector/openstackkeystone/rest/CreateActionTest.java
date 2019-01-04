package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.framework.common.objects.*;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.testng.annotations.Test;

import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CreateActionTest extends BasicConfigurationForTests {

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void creteTestNotSupportedObjectClass(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);

        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Set<Attribute> attributesAccount = new HashSet<>();
        attributesAccount.add(AttributeBuilder.build("email","lion_the_king_of_jungle@cat.com"));


        ObjectClass objectClassAccount = new ObjectClass("Animal");

        try {
            openStackConnector.create(objectClassAccount, attributesAccount, options);
        } finally {
            openStackConnector.dispose();
        }
    }

    @Test(expectedExceptions = InvalidAttributeValueException.class)
    public void creteTestWithNotFilledMandatoryAttributeForAccount(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);

        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Set<Attribute> attributesAccount = new HashSet<>();
        attributesAccount.add(AttributeBuilder.build("email","lion_the_king_of_jungle@cat.com"));


        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;

        try {
            openStackConnector.create(objectClassAccount, attributesAccount, options);
        } finally {
            openStackConnector.dispose();
        }
    }

    //TODO AlreadyExistsException ?
    @Test(expectedExceptions = ClientResponseException.class )
    public void creteTestUserWithExistingLoginName(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);

        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Set<Attribute> attributesAccount = new HashSet<>();
        attributesAccount.add(AttributeBuilder.build("__NAME__","Simba"));


        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;

        Uid simbaUid = openStackConnector.create(objectClassAccount, attributesAccount, options);
        try {
            openStackConnector.create(objectClassAccount, attributesAccount, options);
        } finally {
            openStackConnector.delete(objectClassAccount, simbaUid, options);
            openStackConnector.dispose();
        }
    }

    @Test(expectedExceptions = InvalidAttributeValueException.class)
    public void creteTestWithNotFilledMandatoryAttributeForGroup(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);

        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Set<Attribute> attributesGroup = new HashSet<>();
        attributesGroup.add(AttributeBuilder.build("description","Timon and Pumbaa"));

        ObjectClass objectClassGroup = ObjectClass.GROUP;

        try {
            openStackConnector.create(objectClassGroup, attributesGroup, options);
        } finally {
            openStackConnector.dispose();
        }
    }

    @Test(expectedExceptions = InvalidAttributeValueException.class)
    public void creteTestWithNotFilledMandatoryAttributeForProject(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);

        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Set<Attribute> attributesGroup = new HashSet<>();
        attributesGroup.add(AttributeBuilder.build("description","Nala"));

        ObjectClass objectClassGroup = new ObjectClass("Project");

        try {
            openStackConnector.create(objectClassGroup, attributesGroup, options);
        } finally {
            openStackConnector.dispose();
        }
    }

    @Test(expectedExceptions = InvalidAttributeValueException.class)
    public void creteTestWithNotFilledMandatoryAttributeForDomain(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);

        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Set<Attribute> attributesGroup = new HashSet<>();
        attributesGroup.add(AttributeBuilder.build("description","Mufasa"));

        ObjectClass objectClassGroup = new ObjectClass("Domain");

        try {
            openStackConnector.create(objectClassGroup, attributesGroup, options);
        } finally {
            openStackConnector.dispose();
        }
    }

    @Test(expectedExceptions = InvalidAttributeValueException.class)
    public void creteTestWithNotFilledMandatoryAttributeForRole(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        openStackConnector.init(conf);

        OperationOptions options = new OperationOptions(new HashMap<String,Object>());

        Set<Attribute> attributesGroup = new HashSet<>();
        attributesGroup.add(AttributeBuilder.build("description","Scar"));

        ObjectClass objectClassGroup = new ObjectClass("Role");

        try {
            openStackConnector.create(objectClassGroup, attributesGroup, options);
        } finally {
            openStackConnector.dispose();
        }
    }



}
