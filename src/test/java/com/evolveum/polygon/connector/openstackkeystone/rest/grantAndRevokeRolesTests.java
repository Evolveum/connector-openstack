package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.objects.*;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.identity.v3.Role;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.evolveum.polygon.connector.openstackkeystone.rest.ObjectProcessing.authenticate;

public class grantAndRevokeRolesTests extends BasicConfigurationForTests {

    @Test
    public void grantAndRevokeProjectUserRole() throws Exception {
        OpenStackConnector openStackConnector = new OpenStackConnector();
        OpenStackConnectorConfiguration configuration = getConfiguration();
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
        ObjectClass objectClassProject = new ObjectClass("Project");
        ObjectClass objectClassRole = new ObjectClass("Role");

        //create user
        Set<Attribute> attributesAccount = new HashSet<Attribute>();
        attributesAccount.add(AttributeBuilder.build("email", "testUserPer@performance.com"));
        GuardedString pass = new GuardedString("TestPassword".toCharArray());
        attributesAccount.add(AttributeBuilder.build("__PASSWORD__", pass));
        attributesAccount.add(AttributeBuilder.build("__NAME__", "Simba Is The King"));
        openStackConnector.init(configuration);
        Uid userUid = openStackConnector.create(objectClassAccount, attributesAccount, options);
        openStackConnector.dispose();

        //create project
        Set<Attribute> attributesCreatedProject = new HashSet<Attribute>();
        attributesCreatedProject.add(AttributeBuilder.build("__NAME__", "projects test"));
        openStackConnector.init(configuration);
        Uid projectUid = openStackConnector.create(objectClassProject, attributesCreatedProject, options);
        openStackConnector.dispose();

        //create role
        Set<Attribute> attributesCreatedRole = new HashSet<Attribute>();
        attributesCreatedRole.add(AttributeBuilder.build("__NAME__", "roles test"));
        openStackConnector.init(configuration);
        Uid roleUid = openStackConnector.create(objectClassRole, attributesCreatedRole, options);
        openStackConnector.dispose();


        String concatenateProjectRole = projectUid.getUidValue() + ":" + roleUid.getUidValue();
        Set<Attribute> projectUserRoleAttribute = new HashSet<>();
        projectUserRoleAttribute.add(AttributeBuilder.build("userroles", concatenateProjectRole));
        openStackConnector.init(configuration);
        //grantProjectUserRole
        openStackConnector.addAttributeValues(objectClassAccount, userUid, projectUserRoleAttribute, options);
        openStackConnector.dispose();

        OSClient.OSClientV3 os = authenticate(configuration);

        //checkProjectUserRole("projectId", "userId", "roleId");
        try {
            List<? extends Role> userRoles = os.identity().users().listProjectUserRoles(userUid.getUidValue(), projectUid.getUidValue());
            Assert.assertFalse(userRoles.isEmpty());

            //revokeProjectUserRole
            openStackConnector.init(configuration);
            openStackConnector.removeAttributeValues(objectClassAccount, userUid, projectUserRoleAttribute, options);
            openStackConnector.dispose();

            userRoles = os.identity().users().listProjectUserRoles(projectUid.getUidValue(), userUid.getUidValue());
            Assert.assertTrue(userRoles.isEmpty());
        } finally {
            openStackConnector.init(configuration);
            openStackConnector.delete(objectClassAccount, userUid, options);
            openStackConnector.delete(objectClassProject, projectUid, options);
            openStackConnector.delete(objectClassRole, roleUid, options);
            openStackConnector.dispose();
        }

    }

    @Test
    public void grantAndRevokeProjectGroupRole() {
        OpenStackConnector openStackConnector = new OpenStackConnector();
        OpenStackConnectorConfiguration configuration = getConfiguration();
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());
        ObjectClass objectClassGroup = ObjectClass.GROUP;
        ObjectClass objectClassProject = new ObjectClass("Project");
        ObjectClass objectClassRole = new ObjectClass("Role");

        //create group
        Set<Attribute> attributesGroup = new HashSet<Attribute>();
        attributesGroup.add(AttributeBuilder.build("__NAME__", "Group test name"));
        openStackConnector.init(configuration);
        Uid groupId = openStackConnector.create(objectClassGroup, attributesGroup, options);
        openStackConnector.dispose();

        //create project
        Set<Attribute> attributesCreatedProject = new HashSet<Attribute>();
        attributesCreatedProject.add(AttributeBuilder.build("__NAME__", "projects test for group"));
        openStackConnector.init(configuration);
        Uid projectUid = openStackConnector.create(objectClassProject, attributesCreatedProject, options);
        openStackConnector.dispose();

        //create role
        Set<Attribute> attributesCreatedRole = new HashSet<Attribute>();
        attributesCreatedRole.add(AttributeBuilder.build("__NAME__", "roles test for group"));
        openStackConnector.init(configuration);
        Uid roleUid = openStackConnector.create(objectClassRole, attributesCreatedRole, options);
        openStackConnector.dispose();


        String concatenateProjectRole = projectUid.getUidValue() + ":" + roleUid.getUidValue();
        Set<Attribute> projectGroupRoleAttribute = new HashSet<>();
        projectGroupRoleAttribute.add(AttributeBuilder.build("grouproles", concatenateProjectRole));
        openStackConnector.init(configuration);
        openStackConnector.addAttributeValues(objectClassGroup, groupId, projectGroupRoleAttribute, options);
        openStackConnector.dispose();
        OSClient.OSClientV3 os = authenticate(configuration);

        try {
            List<? extends Role> userRoles = os.identity().groups().listProjectGroupRoles(groupId.getUidValue(), projectUid.getUidValue());
            Assert.assertFalse(userRoles.isEmpty());

            //revokeProjectGroupRole
            openStackConnector.init(configuration);
            openStackConnector.removeAttributeValues(objectClassGroup, groupId, projectGroupRoleAttribute, options);
            openStackConnector.dispose();

            userRoles = os.identity().users().listProjectUserRoles(projectUid.getUidValue(), groupId.getUidValue());
            Assert.assertTrue(userRoles.isEmpty());

        } finally {
            openStackConnector.init(configuration);
            openStackConnector.delete(objectClassGroup, groupId, options);
            openStackConnector.delete(objectClassProject, projectUid, options);
            openStackConnector.delete(objectClassRole, roleUid, options);
            openStackConnector.dispose();
        }

    }

}
