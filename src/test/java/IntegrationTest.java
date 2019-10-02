import com.xebialabs.itest.ITestHelper;

import java.io.IOException;

import org.junit.Test;
import org.junit.BeforeClass;

public class IntegrationTest
{
    private static String IMPORT_CONFIG_FNAME = "./build/resources/test/initialize/server-configs.json";
    private static String IMPORT_TEMPLATE_FNAME = "./build/resources/test/initialize/release-template.json";

    @BeforeClass
    public static void setup() throws IOException, InterruptedException
    {
        ITestHelper.initialize();
        ITestHelper.loadConfig(IMPORT_CONFIG_FNAME);
        ITestHelper.loadTemplate(IMPORT_TEMPLATE_FNAME);
    }

    @Test
    public void launchJobTest()
    {
        System.out.println("launchJobTest");

    }
}