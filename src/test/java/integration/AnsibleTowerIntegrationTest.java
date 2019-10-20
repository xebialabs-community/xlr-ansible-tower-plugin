/**
 * Copyright 2019 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package integration;
import static org.junit.Assert.assertTrue;

import integration.util.AnsibleTowerTestHelper;
import java.io.File;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import org.testcontainers.containers.DockerComposeContainer;

public class AnsibleTowerIntegrationTest
{
    private static final String DOCKER_COMPOSE_FNAME = "build/resources/test/docker/docker-compose.yml";
    private static String IMPORT_CONFIG_FNAME = "build/resources/test/docker/initialize/data/server-configs.json";
    private static String IMPORT_TEMPLATE_FNAME = "build/resources/test/docker/initialize/data/release-template.json";
    private static String EXPECTED_RESULT_FNAME = "build/resources/test/testExpected/testAnsibleTower.json";

    @ClassRule
    public static DockerComposeContainer docker =
        new DockerComposeContainer(new File(DOCKER_COMPOSE_FNAME))
            .withLocalCompose(true);


    // TODO: add wait for service and timeout like this...
    // environment = new DockerComposeContainer(new File("src/test/resources/compose-test.yml"))
    //     .withExposedService("redis_1", REDIS_PORT, 
    //         Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));


    @BeforeClass
    public static void setup() throws Exception
    {
        AnsibleTowerTestHelper.initialize();
        AnsibleTowerTestHelper.loadConfig(IMPORT_CONFIG_FNAME);
        AnsibleTowerTestHelper.loadTemplate(IMPORT_TEMPLATE_FNAME);
    }

    @Test
    public void testAnsibleTower() throws Exception
    {
        JSONObject theResult = AnsibleTowerTestHelper.getAnsibleTowerReleaseResult();
        //System.out.println("RESULT:\n"+theResult);

        assertTrue(theResult != null);

        // The file, testCherwell, contains the JSONObject we expect to be returned from XLR. Order of variables does not matter
        String expected = AnsibleTowerTestHelper.readFile(EXPECTED_RESULT_FNAME);
        try {
            // This will assert that all pre-exisiting variables are there, have been set to the correct and no variables were add. Order does not matter.
            JSONAssert.assertEquals(expected, theResult, JSONCompareMode.NON_EXTENSIBLE);
        } catch (Exception e) {
            System.out.println("FAILED: EXCEPTION: "+e.getMessage());
            e.printStackTrace();
        }
        System.out.println("");
        System.out.println("testAnsibleTower passed");

    }
}