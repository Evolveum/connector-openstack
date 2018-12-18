import com.evolveum.polygon.connector.openstackkeystone.rest.OpenStackConnector;
import com.evolveum.polygon.connector.openstackkeystone.rest.OpenStackConnectorConfiguration;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.testng.annotations.Test;

public class ConfigurationValidityTests extends BasicConfigurationForTests {

    @Test(expectedExceptions = ConfigurationException.class)
    public void creteTestConfigurationMandatoryValueMissing(){
        OpenStackConnector openStackConnector = new OpenStackConnector();

        OpenStackConnectorConfiguration conf = getConfiguration();
        conf.setDomainName("");
        openStackConnector.init(conf);
        try {
            openStackConnector.test();
        } finally {
            openStackConnector.dispose();
        }
    }


}
