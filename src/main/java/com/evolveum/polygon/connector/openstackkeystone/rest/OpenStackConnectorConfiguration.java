package com.evolveum.polygon.connector.openstackkeystone.rest;


import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.StatefulConfiguration;

public class OpenStackConnectorConfiguration extends AbstractConfiguration
        implements StatefulConfiguration {

    private static final Log LOG = Log.getLog(OpenStackConnector.class);

    private String endpoint;
    private String userId;
    private String secret;
    private String projectName;
    private String domainName;

    @ConfigurationProperty(order = 1, displayMessageKey = "endpoint", required = true)
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @ConfigurationProperty(order = 2, displayMessageKey = "credentials - user ID", required = true)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @ConfigurationProperty(order = 3, displayMessageKey = "credentials - secret", required = true)
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @ConfigurationProperty(order = 4, displayMessageKey = "Project name", required = true)
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @ConfigurationProperty(order = 5, displayMessageKey = "Domain name", required = true)
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }


    @Override
    public void validate() {

        LOG.info("Processing trough configuration validation procedure.");
        if (StringUtil.isBlank(endpoint)) {
            throw new ConfigurationException("endpoint  cannot be empty.");
        }
        if (StringUtil.isBlank(userId)) {
            throw new ConfigurationException("userId cannot be empty.");
        }
        if (StringUtil.isBlank(secret)) {
            throw new ConfigurationException("secret cannot be empty.");
        }
        if (StringUtil.isBlank(projectName)) {
            throw new ConfigurationException("projectName cannot be empty.");
        }
        if (StringUtil.isBlank(domainName)) {
            throw new ConfigurationException("domainName cannot be empty.");
        }
        LOG.info("Configuration valid");

    }

    @Override
    public void release() {

    LOG.info("The release of configuration resources is being performed");

    this.endpoint=null;
    this.userId=null;
    this.secret=null;
    this.projectName=null;
    this.domainName=null;

    }
}
