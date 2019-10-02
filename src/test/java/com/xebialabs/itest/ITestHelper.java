/**
 * Copyright 2019 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.xebialabs.itest;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class ITestHelper 
{
    
    private static final String BASE_URI = "http://localhost:15516/api/v1";
    private static final String IMPORT_TEMPLATE_URI = "/templates/import";
    private static final String IMPORT_CONFIG_URI = "/config";
    
    public static String templateId;

    public static void initialize() throws IOException, InterruptedException 
    {
        
        System.out.println("...pausing for 1 minute, waiting for XLR to start.");
        Thread.sleep(60000);
        System.out.println("docker-compose running");
    }

    public static void loadConfig(String fname) throws Exception
    {
        System.out.println("load configuration '"+fname+"'");

        // Load the template if present
        Response response = postConfig(IMPORT_CONFIG_URI, fname);
        if (response.getStatusCode() > 299) 
        {
            System.out.println("Config Load Failed: '" + response.getStatusLine() + "'");
        } 
    }

    public static String loadTemplate(String fname) throws Exception
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

    private static Response postConfig(String uri, String fname) throws Exception
    {
        try {
            RestAssured.baseURI = BASE_URI;

            RequestSpecification httpRequest = given().auth().preemptive().basic("admin", "admin");

            // Load config
            JSONObject requestParamsConfig = getRequestParamsFromFile(fname);
            httpRequest.header("Content-Type", "application/json");
            httpRequest.header("Accept", "application/json");
            httpRequest.body(requestParamsConfig.toJSONString());
            
            System.out.println("The uri we are posting to - "+BASE_URI+uri);
            // Post server config
            return httpRequest.post(BASE_URI+uri);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        
    }

    private static Response postFile(String uri, String fname) throws Exception
    {
        try 
        {
            RestAssured.baseURI = BASE_URI;

            RequestSpecification httpRequest = given().auth().preemptive().basic("admin", "admin");

            JSONObject requestParams = new JSONObject();

            httpRequest.body(requestParams.toJSONString());
            httpRequest.contentType("multipart/form-data");
            httpRequest.multiPart(new File(fname));

            System.out.println("The uri we are posting to - "+BASE_URI+uri);
            // Post file
            return httpRequest.post(BASE_URI+uri);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            throw e;
        }
    }



    public static JSONObject getRequestParamsFromFile(String filePath) throws Exception
    {
        JSONObject requestParams = new JSONObject();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader(filePath))
        {
            //Read JSON file
            Object obj = jsonParser.parse(reader);
 
            requestParams = (JSONObject) obj;
            //System.out.println(requestParams);
 
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return requestParams;
    }
}