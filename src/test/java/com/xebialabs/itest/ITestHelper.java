/**
 * This class facilitates the prepration of XL Tools for integration testing.  Here is the flow sequence:
 * 
 *
 * At this point the integration tests can load the desired template and run their tests.
 * 
 */
package com.xebialabs.itest;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;

import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.Wait;
import java.time.Duration;

public class ITestHelper 
{
    private static final String DOCKER_COMPOSE_FNAME = "build/resources/test/resources/docker/docker-compose.yml";

    private static final String BASE_URI = "http://localhost:15516/api/v1";
    private static final int XLR_PORT = 15516;
    private static final String IMPORT_TEMPLATE_URI = "/templates/import";
    private static final String IMPORT_CONFIG_URI = "/config";
    
    public static DockerComposeContainer docker;
    public static String templateId;

    public static void initialize() throws IOException, InterruptedException 
    {
        System.out.println("starting docker-compose...");
        docker = new DockerComposeContainer(new File(DOCKER_COMPOSE_FNAME))
            .withLocalCompose(true);

            // .withExposedService("xlr_1", XLR_PORT, 
            //     Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)));

        // TODO: add wait for service and timeout like this...
        // environment = new DockerComposeContainer(new File("src/test/resources/compose-test.yml"))
        //     .withExposedService("redis_1", REDIS_PORT, 
        //         Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));

        System.out.println("...pausing for 60, waiting for XLR to start.");
        Thread.sleep(60000);
        System.out.println("docker-compose running");
    }

    public static void shutdown()
    {
        // docker.down??
    }

    public static void loadConfig(String fname)
    {
        System.out.println("load configuration '"+fname+"'");

        // Load the template if present
        Response response = postFile(IMPORT_CONFIG_URI, fname);
        if (response.getStatusCode() > 299) 
        {
            System.out.println("Config Load Failed: '" + response.getStatusLine() + "'");
        } 
    }

    public static String loadTemplate(String fname)
    {
        System.out.println("load template '"+fname+"'");

        // Load the template if present
        Response response = postFile(IMPORT_TEMPLATE_URI, fname);
        if (response.getStatusCode() > 299) 
        {
            System.out.println("Template Load Failed: '" + response.getStatusLine() + "'");
            return null;
        } 

        // save the template id so it can be referenced later
        return response.jsonPath().getString("id");
    }

    private static Response postFile(String uri, String fname)
    {
        try 
        {
            RestAssured.baseURI = BASE_URI;

            RequestSpecification httpRequest = given().auth().preemptive().basic("admin", "admin");

            JSONObject requestParams = new JSONObject();

            httpRequest.body(requestParams.toJSONString());
            httpRequest.contentType("multipart/form-data");
            httpRequest.multiPart(new File(fname));

            // Post file
            return httpRequest.post(uri);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            return null;
        }
    }
}