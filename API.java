package edu.uky.cs405g.sample.httpcontrollers;
//
// Sample code used with permission from Dr. Bumgardner
// Name: Ngoc Phan
// SID: 12246097
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.uky.cs405g.sample.Launcher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.sql.*;
import java.util.Random;
import java.util.UUID;

@Path("/api")
public class API {

    private Type mapType;
    private Gson gson;

    public API() {
        mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        gson = new Gson();
    }

    //curl http://localhost:9990/api/status
    //{"status_code":1}
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {
        String responseString = "{\"status_code\":0}";
        try {
            //Here is where you would put your system test, 
            //but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // healthcheck()

    //curl http://localhost:9998/api/listusers
    //{"1":"@paul","2":"@chuck"}
    @GET
    @Path("/listusers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers() {
        String responseString = "{}";
        try {
            Map<String, String> teamMap = Launcher.dbEngine.getUsers();
            responseString = Launcher.gson.toJson(teamMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // listUsers()


    //curl -d '{"foo":"silly1","bar":"silly2"}' \
    //     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/exampleJSON
    //
    //{"status_code":1, "foo":silly1, "bar":silly2}
    @POST
    @Path("/exampleJSON")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exampleJSON(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String fooval = myMap.get("foo");
            String barval = myMap.get("bar");
            //Here is where you would put your system test,
            //but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1, "
                    + "\"foo\":\"" + fooval + "\", "
                    + "\"bar\":\"" + barval + "\"}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // exampleJSON()

    //curl http://localhost:9990/api/exampleGETBDATE/2
    //{"bdate":"1968-01-26"}
    @GET
    @Path("/exampleGETBDATE/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exampleBDATE(@PathParam("idnum") String idnum) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            Map<String, String> teamMap = Launcher.dbEngine.getBDATE(idnum);
            responseString = Launcher.gson.toJson(teamMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // exampleBDATE

    // curl -d '{"handle":"@cooldude42", "password":"mysecret!", "fullname":"Angus Mize", "location":"Kentucky", "xmail":"none@nowhere.com", "bdate":"1970-07-01"}'
    //      -H "Content-Type: application/json"
    //      -X POST http://localhost:9990/api/createuser
    //
    // {"status":"4"}
    // {"status":"-2", "error":"SQL Constraint Exception"}
    @POST
    @Path("/createuser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createuser(InputStream inputData) {
        String responseString = "{\"status\":\"-2\", \"error\":\"SQL Constraint Exception\"}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String handle = myMap.get("handle");
            String pass = myMap.get("password");
            String fname = myMap.get("fullname");
            String location = myMap.get("location");
            String xmail = myMap.get("xmail");
            String bdateStr = myMap.get("bdate");
            Date bdate = Date.valueOf(bdateStr);

            int idnum = Launcher.dbEngine.insertuser(handle, pass, fname, location, xmail, bdate);
            if (idnum > 0) {
                responseString = "{\"status\":\"" + idnum + "\"}";
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }//createuser

    // curl -d '{"handle":"@cooldude42", "password":"mysecret!"}'
    //      -H "Content-Type: application/json"
    //      -X POST http://localhost:9990/api/seeuser/2 (Links to an external site.)
    // 2 = Identity.idnum
    // Output: {"status":"1", "handle":"@carlos", "fullname":"Carlos Mize", "location":"Kentucky", "email":carlos@notgmail.com", "bdate":"1970-01-26","joined":"2020-04-01"}
    // Output: {}. // no match found, could be blocked, user doesn't know.
    @POST
    @Path("/seeuser/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response seeuser(@PathParam("idnum") String idnum) {
        String responseString = null;
        try {
            responseString = Launcher.dbEngine.getUserData(idnum);
        }
        catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }// seeuser

    //curl  -d '{"handle":"@cooldude42", "password":"mysecret!"}'
    //      -H "Content-Type: application/json"
    //      -X POST http://localhost:9990/api/block/2 (Links to an external site.)
    // 2 = Identity.idnum
    // Output: {"status":"1"}
    // Output: {"status":"0", "error":"DNE"}
    @POST
    @Path("/block/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response block(InputStream inputData, @PathParam("idnum") String idnum) {
        String responseString = "\"status\":\"0\",\"error\":\"DNE\"";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String handle = myMap.get("handle");
            String pass = myMap.get("password");

            int usernum = Launcher.dbEngine.getId(handle, pass);
            responseString = Launcher.dbEngine.blockuser(usernum, Integer.parseInt(idnum));
        }
        catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }// block

    public Integer count = 0;		// keep track of the likes in the reprint function.
    @POST
    @Path("/reprint/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reprint(InputStream inputData, @PathParam("idnum") String idnum){
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String handle = myMap.get("handle");
            String password = myMap.get("password");
            String action = myMap.get("likeit");

            int usernum = Launcher.dbEngine.getId(handle, password);
            if(usernum == -2){
                responseString = "{\"status\":\"-2\", \"error\":\"password and handle do not match\"}";
            }else{
                int num = Launcher.dbEngine.reprint(count, usernum, Integer.parseInt(idnum), Boolean.parseBoolean(action));
                count++;
                if(num > 0){
                    responseString = "{\"status\":\"1\"}";
                }else if(num < 0){
                    responseString = "{\"status\":\"0\", \"error\":\"Story not found\"}";
                }else if(num == 0){
                    responseString =  "{\"status\":\"0\", \"error\":\"blocked\"}";
                }

                responseString = "{\"status\":\"" + num + "\"}";
            }
        }
        catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();

    }

    @POST
    @Path("/follow/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response follow(InputStream inputData, @PathParam("idnum") String idnum) {
        String responseString = "{\"status\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String handle = myMap.get("handle");
            String password = myMap.get("password");

            int userid = Launcher.dbEngine.getId(handle, password);
            int output = Launcher.dbEngine.follow(Integer.parseInt(idnum), userid);
            if(userid == -2){
                responseString = "{\"status\":\"-2\", \"error\":\"password and handle do not match\"}";
            }else if(output > 0){
                responseString = "{\"status\":\"1\"}";
            }else if(output < 0){
                responseString = "{\"status\":\"-1\", \"error\":\"DNE\"}";
            }else {
                responseString =  "{\"status\":\"0\", \"error\":\"blocked\"}";
            }

            //responseString = Launcher.dbEngine.follow(idnum, userid);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }  //follow()

    @POST
    @Path("/unfollow/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unfollow(InputStream inputData, @PathParam("idnum") String idnum) {
        String responseString = "{\"status\":\"0\"}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String handle = myMap.get("handle");
            String password = myMap.get("password");

            int userid = Launcher.dbEngine.getId(handle, password);
            int output = Launcher.dbEngine.unfollow(Integer.parseInt(idnum), userid);
            if(output > 0){
                responseString = "{\"status\":\"1\"}";
            }else if(output < 0){
                responseString = "{\"status\":\"-1\", \"error\":\"DNE\"}";
            }else{
                responseString =  " {\"status\":\"0\", \"error\":\"not currently followed\"}";
            }

            //responseString = Launcher.dbEngine.follow(idnum, userid);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }  //unfollow()

    @GET
    @Path("/suggestions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response suggestions(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String handle =myMap.get("handle");
            String password = myMap.get("password");
            int userid = Launcher.dbEngine.getId(handle, password);
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = Launcher.dbEngine.suggestions(userid);
            if(userid == -2){
                responseString = "{\"status\":\"-2\", \"error\":\"password and handle do not match\"}";
            } //else if(output > 0){
            // responseString = "{\"status\":\"1\"}";
            //}else if(output < 0){
            //  responseString = "{\"status\":\"-1\", \"error\":\"DNE\"}";
            // }else {
            //   responseString =  "{\"status\":\"0\", \"error\":\"blocked\"}";
            //}
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();

    }// api/suggestions

    @GET
    @Path("/poststory")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response poststory() {
        String responseString = "{}";
        try {
            Map<String, String> teamMap = Launcher.dbEngine.getPoststory();
            responseString = Launcher.gson.toJson(teamMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // poststory()

}