/**
 * Copyright 2019 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.File;

import com.xebialabs.itest.ITestHelper;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;

public class IntegrationTest
{
    private static final String DOCKER_COMPOSE_FNAME = "build/resources/test/docker/docker-compose.yml";
    private static String IMPORT_CONFIG_FNAME = "build/resources/test/initialize/data/server-configs.json";
    private static String IMPORT_TEMPLATE_FNAME = "build/resources/test/initialize/data/release-template.json";

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