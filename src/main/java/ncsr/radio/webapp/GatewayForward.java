package ncsr.radio.webapp;

import ncsr.radio.webapp.helper.dto;
import net.servicestack.client.JsonServiceClient;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/getSensorStatus")
public class GatewayForward {
    private JsonServiceClient client;
    private String USERNAME = "RTWG2";
    private String PASSWORD = "raDi2";
    private String INSTALLATION_ID = "00000000-0000-0000-0000-b827eb3126a9";

    private String BASE_URL = "http://dev.nassist-test.com/api";
    private String MOVEMENT_DETECTION_SENSOR_ID = "77664dc6-0e9d-4861-8db9-fb73090061e6";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() throws JSONException {
        try {
            sensorStatus(MOVEMENT_DETECTION_SENSOR_ID);
            String result = sensorStatus(MOVEMENT_DETECTION_SENSOR_ID);
            return Response.status(200).type("application/json;charset=utf-8").entity(result).build();
        }
        catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(e.getStackTrace()).build();
        }
    }

    private String sensorStatus(String id){
        try{

            dto.Authenticate auth = new dto.Authenticate();
            auth.setUserName(USERNAME);
            auth.setPassword(PASSWORD);

            client = new JsonServiceClient(BASE_URL);
            client.post("/authenticate", auth, dto.AuthenticateResponse.class);
            dto.SensorStatuses statusesRequest = new dto.SensorStatuses();
            statusesRequest.setId(id);

            dto.SensorStatusesResponse statusesResponse = client.get(statusesRequest);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("status", statusesResponse.Statuses.get(statusesResponse.Statuses.size()-1).Status);
            jsonObject.put("date", statusesResponse.Statuses.get(statusesResponse.Statuses.size()-1).Date.toString());
            jsonObject.put("trigger", statusesResponse.Statuses.get(statusesResponse.Statuses.size()-1).Trigger);
            jsonObject.put("triggerName", statusesResponse.Statuses.get(statusesResponse.Statuses.size()-1).TriggerName);

            return jsonObject.toString();
        }
        catch(Exception e){
            System.err.println(e.toString());
            return null;
        }

    }

}
