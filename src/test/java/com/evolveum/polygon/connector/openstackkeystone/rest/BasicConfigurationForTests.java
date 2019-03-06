package com.evolveum.polygon.connector.openstackkeystone.rest;

public class BasicConfigurationForTests {

    private PropertiesParser parser = new PropertiesParser();

    protected OpenStackConnectorConfiguration getConfiguration() {
        OpenStackConnectorConfiguration configuration = new OpenStackConnectorConfiguration();
        configuration.setDomainName(parser.getDomainName());
        configuration.setEndpoint(parser.getEndpoint());
        configuration.setProjectName(parser.getProjectName());
        configuration.setPassword(parser.getSecret());
        configuration.setUserId(parser.getUserId());

        return configuration;
    }

}
