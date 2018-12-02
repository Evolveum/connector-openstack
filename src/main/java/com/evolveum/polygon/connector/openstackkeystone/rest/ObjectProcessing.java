package com.evolveum.polygon.connector.openstackkeystone.rest;


import org.identityconnectors.common.logging.Log;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;

public class ObjectProcessing {


    public OpenStackConnectorConfiguration getConfiguration() {
        return configuration;
    }

    private OpenStackConnectorConfiguration configuration;

    public ObjectProcessing(OpenStackConnectorConfiguration configuration) {
        this.configuration = configuration;
    }

    static OSClientV3 authenticate(OpenStackConnectorConfiguration configuration) {

        //        # Scoping to a project just by name isn't possible as the project name is only unique within a domain.
        //# You can either use this as the id of the project is unique across domains
        return OSFactory.builderV3()
                .endpoint(configuration.getEndpoint())
                .credentials(configuration.getUserId(), configuration.getSecret())
                .scopeToProject(Identifier.byName(configuration.getProjectName()), Identifier.byName(configuration.getDomainName()))
                .authenticate();

    }

    static final Log LOG = Log.getLog(OpenStackConnector.class);

    public void test() {
        LOG.info("Start test.");
        OSClientV3 os = authenticate(getConfiguration());
        os.identity().users().list();
    }

}
