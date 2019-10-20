/**
 * Copyright 2019 XEBIALABS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package integration.util;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;


public class AnsibleTowerTestHelper 
{
    
    private static final String BASE_URI = "http://localhost:15516/api/v1";
    private static final String IMPORT_TEMPLATE_URI = "/templates/import";
    private static final String IMPORT_CONFIG_URI = "/config";
    private static final String START_RELEASE_ANSIBLE = "/templates/Applications/Releasebc6b57e850474a8999d5c47cb8d2bdef/start";
    private static final String GET_RELEASE_PREFIX = "/releases/";
    private static final String GET_VARIABLES_SUFFIX = "/variableValues";
    private static RequestSpecification httpRequest = null;
    
    public static String templateId;

    private AnsibleTowerTestHelper() {
        /*
         * Private Constructor will prevent the instantiation of this class directly
         */
    }

    static {
        baseURI = BASE_URI;
        // Configure authentication
        httpRequest = given().auth().preemptive().basic("admin", "admin");
    }

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
            // Load config
            JSONObject requestParamsConfig = getRequestParamsFromFile(fname);
            httpRequest.header("Content-Type", "application/json");
            httpRequest.header("Accept", "application/json");
            httpRequest.body(requestParamsConfig.toJSONString());
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
            JSONObject requestParams = new JSONObject();
            httpRequest.body(requestParams.toJSONString());
            httpRequest.contentType("multipart/form-data");
            httpRequest.multiPart(new File(fname));
            // Post file
            return httpRequest.post(BASE_URI+uri);
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            throw e;
        }
    }

    public static org.json.JSONObject getAnsibleTowerReleaseResult() throws Exception{
        org.json.JSONObject releaseResultJSON = null;
        String responseId = "";
        String releaseResultStr = "";
        // Prepare httpRequest, start the release
        JSONObject requestParams = getRequestParams();
        Response response = given().auth().preemptive().basic("admin", "admin")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body(requestParams.toJSONString())
            .post(START_RELEASE_ANSIBLE);

        ///////// retrieve the planned release id.
        if (response.getStatusCode() != 200) {
            System.out.println("Status line, Start release was " + response.getStatusLine() );
        } else {
            responseId = response.jsonPath().getString("id");
            System.out.println("Start release was successful, id = "+ responseId);
        }

        ///////// Get Archived responses
        // Sleep so XLR can finish processing releases
        System.out.println("Pausing for 2 minutes, waiting for release to complete. If most requests fail with 404, consider sleeping longer.");
        Thread.sleep(120000);
        //////////
        response = given().auth().preemptive().basic("admin", "admin")
        .header("Content-Type", "application/json")
        .header("Accept", "application/json")
        .body(requestParams.toJSONString())
        .get(GET_RELEASE_PREFIX + responseId + GET_VARIABLES_SUFFIX);

        if (response.getStatusCode() != 200) {
            System.out.println("Status line for get variables was " + response.getStatusLine() + "");
        } else {
            //releaseResult = response.jsonPath().get("phases[0].tasks[1].comments[0].text").toString();
            releaseResultStr = response.jsonPath().prettyPrint();
            try {
                releaseResultJSON =  new org.json.JSONObject(releaseResultStr);
            } catch (Exception e) {
                System.out.println("FAILED: EXCEPTION: "+e.getMessage());
                e.printStackTrace();
                throw e;
            }        
        }
        return releaseResultJSON;
    }


    /////////////////// Util methods

    public static String readFile(String path) throws IOException {
        StringBuilder result = new StringBuilder("");

        File file = new File(path);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        return result.toString();
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

    public static JSONObject getRequestParams() {
        // must use intermediate parameterized HashMap to avoid warnings
        HashMap<String,Object> params = new HashMap<String,Object>();
        
        params.put("releaseTitle", "release from api");
        JSONObject requestParams = new JSONObject(params);
        return requestParams;
    }

}