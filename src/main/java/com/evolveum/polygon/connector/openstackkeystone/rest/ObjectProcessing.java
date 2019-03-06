package com.evolveum.polygon.connector.openstackkeystone.rest;


import com.evolveum.polygon.common.GuardedStringAccessor;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.*;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.ActionResponse;
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
        GuardedString guardedString = configuration.getPassword();
        GuardedStringAccessor accessor = new GuardedStringAccessor();
        guardedString.access(accessor);
        return OSFactory.builderV3()
                .endpoint(configuration.getEndpoint())
                .credentials(configuration.getUserId(), accessor.getClearString())
                .scopeToProject(Identifier.byName(configuration.getProjectName()), Identifier.byName(configuration.getDomainName()))
                .authenticate();

    }

    static final Log LOG = Log.getLog(OpenStackConnector.class);

    public void test() {
        LOG.info("Start test.");
        OSClientV3 os = authenticate(getConfiguration());
        os.identity().users().list();
    }

    void handleActionResponse(ActionResponse actionResponse) {
        int statusCode = actionResponse.getCode();
        String message = actionResponse.getFault();
        LOG.info("HandleActionResponse, statuscode: {0}, message {1}", statusCode, message);

        if (statusCode == 400 || statusCode == 405 || statusCode == 406) {
            throw new ConnectorIOException(actionResponse.getFault());
        } else if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 407) {
            throw new PermissionDeniedException(message);
        } else if (statusCode == 404 || statusCode == 410) {
            throw new UnknownUidException(message);
        } else if (statusCode == 408) {
            throw new OperationTimeoutException(message);
        } else if (statusCode == 412) {
            throw new PreconditionFailedException(message);
        } else if (statusCode == 409) {
            throw new AlreadyExistsException(message);
        }

        throw new ConnectorException(message);
    }

}
