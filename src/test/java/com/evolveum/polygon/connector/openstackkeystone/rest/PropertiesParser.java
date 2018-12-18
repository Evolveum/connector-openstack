package com.evolveum.polygon.connector.openstackkeystone.rest;

import org.identityconnectors.common.logging.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class PropertiesParser {

    private static final Log LOGGER = Log.getLog(PropertiesParser.class);
    private Properties properties;
    private String FilePath = "../connector-openStack-Keystone/testProperties/propertiesforTest.properties";
    private final String ENDPOINT = "endpoint";
    private final String SECRET = "secret";
    private final String PROJECTNAME = "projectName";
    private final String USERID = "userId";
    private final String DOMAINNAME = "domainName";


    public PropertiesParser() {

        try {
            InputStreamReader fileInputStream = new InputStreamReader(new FileInputStream(FilePath),
                    StandardCharsets.UTF_8);
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found: {0}", e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error("IO exception occurred {0}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }


    public String getEndpoint() {
        return (String) properties.get(ENDPOINT);
    }

    public String getUserId() {
        return (String) properties.get(USERID);
    }

    public String getSecret() {
        return (String) properties.get(SECRET);
    }

    public String getProjectName() {
        return (String) properties.get(PROJECTNAME);
    }

    public String getDomainName() {
        return (String) properties.get(DOMAINNAME);
    }


}