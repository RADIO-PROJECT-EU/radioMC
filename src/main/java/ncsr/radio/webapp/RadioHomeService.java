package ncsr.radio.webapp;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

/**
 * Created by gstavrinos on 3/3/16.
 */

@Path("/smarthome")
public class RadioHomeService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response homeAction(InputStream indata) {

        String response = "";

        try {
            StringBuilder sb2 = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(indata));
            String line;
            while ((line = in.readLine()) != null) {
                sb2.append(line);
            }
            System.out.println("Data Received: " + sb2.toString());
            JSONObject action= new JSONObject(sb2.toString());

            //this is the "menu" of actions
            if(action.getString("action_type").equals("goto")){
                //two ways to send goto commands: 1. With ID, 2. With coordinates.
                if(action.getJSONObject("location").has("id")){
                    File f = new File(this.getClass().getClassLoader().getResource(".").toString().replace("file:","")+"rooms.json");
                    boolean found = false;
                    line = null;
                    StringBuilder sb1 = new StringBuilder();
                    /*if(f.exists() && !f.isDirectory()) {
                        if(f.length() > 0) {
                            found = true;
                        }*/
                        try {
                            InputStream is = this.getClass().getResourceAsStream("/rooms.json");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            while ((line = reader.readLine()) != null) {
                                sb1.append(line);
                            }
                            reader.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    /*}
                    if (!found) {
                        return Response.status(404).build();
                    }*/

                    JSONObject current_rooms = new JSONObject(sb1.toString());

                    JSONArray ja = current_rooms.getJSONArray("rooms");

                    for (int i = 0; i < ja.length(); i++) {
                        if(action.getJSONObject("location").getInt("id") == ja.getJSONObject(i).getInt("id")){
                            gotoRobot(ja.getJSONObject(i).getDouble("x"),ja.getJSONObject(i).getDouble("y"), ja.getJSONObject(i).getDouble("theta"));
                            //TODO get response from robot here
                            //response = ...
                            //temp response below
                            response = "The robot is coming to take you to "+ja.getJSONObject(i).getString("name")+"!<br>Please wait!!";
                            //response = "The robot is busy!<button class='notification-button'>Please notify your caregiver!</button>";
                            //response = "Already in this room!<button class='notification-button'>Please select another room!</button>";
                            break;
                        }
                    }
                }
                else{
                    gotoRobot(action.getJSONObject("location").getDouble("x"),action.getJSONObject("location").getDouble("y"),action.getJSONObject("location").getDouble("theta"));
                    //TODO get response from robot here
                    //response = ...
                }
            }


        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return Response.status(200).type("application/json;charset=utf-8").entity("{\"response\":\""+response+"\"}").build();

    }

    private void gotoRobot(double x, double y, double theta){
        //TODO code to send robot to x,y
        HttpClient httpClient = HttpClientBuilder.create().build();
        JSONObject json = new JSONObject();
        json.put("action_type", "goto");
        JSONObject j2 = new JSONObject();
        j2.put("x",x);
        j2.put("y",y);
        j2.put("theta",theta);
        json.put("location",j2);
        try {
            HttpPost request = new HttpPost("http://localhost:8081/radio_action_manager_node/action");
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            // handle response here...
        }
        catch (Exception ex) {
            // handle exception here
        }
        System.out.println("Robot is heading to "+x+","+y);
    }

}
