package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ProjectPerformanceTests extends BasicConfigurationForTests {

    private Set<Uid> projectsUid = new HashSet<Uid>();


    @Test(priority = 40)
    public void Create500Projects() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        ObjectClass objectClassProject = new ObjectClass("Project");

        for (int i = 0; i < 500; i++) {

            Set<Attribute> attributesCreateProject = new HashSet<Attribute>();
            attributesCreateProject.add(AttributeBuilder.build("description", "The Lion King"));
            attributesCreateProject.add(AttributeBuilder.build("enabled", true));
            attributesCreateProject.add(AttributeBuilder.build("__NAME__", "Nala project" + i));
            attributesCreateProject.add(AttributeBuilder.build("domain_id", "default"));

            openStackConnector.init(configuration);
            Uid projectUid = openStackConnector.create(objectClassProject, attributesCreateProject, options);
            openStackConnector.dispose();
            projectsUid.add(projectUid);

        }

    }

    @Test(priority = 41)
    public void Update500ProjectTest() {
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration configuration = getConfiguration();

        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        ObjectClass objectClassProject = new ObjectClass("Project");
        int i = 0;
        for (Uid projectUid : projectsUid) {

            Set<Attribute> attributesUpdateProject = new HashSet<Attribute>();
            attributesUpdateProject.add(AttributeBuilder.build("description", "The Lion King - updated"));
            attributesUpdateProject.add(AttributeBuilder.build("__NAME__", "Simba project " + i));
            openStackConnector.init(configuration);
            openStackConnector.update(objectClassProject, projectUid, attributesUpdateProject, options);
            openStackConnector.dispose();
            i++;
        }
    }


    @Test(priority = 42)
    public void Delete500GProjectTest() {
        OpenStackConnector gitlabRestConnector = new OpenStackConnector();
        OpenStackConnectorConfiguration configuration = getConfiguration();
        ObjectClass objectClassProject = new ObjectClass("Project");
        OperationOptions options = new OperationOptions(new HashMap<String, Object>());

        for (Uid project : projectsUid) {
            gitlabRestConnector.init(configuration);
            gitlabRestConnector.delete(objectClassProject, project, options);
            gitlabRestConnector.dispose();
        }
    }


}
