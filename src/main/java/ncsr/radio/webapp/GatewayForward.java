package ncsr.radio.webapp;

import ncsr.radio.webapp.helper.dto.*;
import net.servicestack.client.JsonServiceClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/getSensorStatus")
public class GatewayForward {
    private boolean useLocalIoT = true;
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

            Authenticate auth = new Authenticate();
            auth.setUserName(USERNAME);
            auth.setPassword(PASSWORD);
            if(useLocalIoT) {
                BASE_URL = "http://" + waitForIP(INSTALLATION_ID);
            }
            System.out.println(BASE_URL);
            client = new JsonServiceClient(BASE_URL);
            client.post("/authenticate", auth, AuthenticateResponse.class);
            JSONObject jsonObject = new JSONObject();

            if(useLocalIoT){
                InstallationSensorsSecurityResponse response = client.get("/installations/" + INSTALLATION_ID + "/sensors/security", InstallationSensorsSecurityResponse.class);

                for (SensorWithAreaName s : response.Sensors) {
                    if (s.Id.equals(id)) {
                        jsonObject.put("id", id);
                        jsonObject.put("status", s.Status);
                        jsonObject.put("date", s.LastDateStatus);
                        break;
                    }
                }
            }
            else {
                SensorStatuses statusesRequest = new SensorStatuses();
                statusesRequest.setId(id);
                SensorStatusesResponse statusesResponse = client.get(statusesRequest);
                jsonObject.put("id", id);
                jsonObject.put("status", statusesResponse.Statuses.get(statusesResponse.Statuses.size() - 1).Status);
                jsonObject.put("date", statusesResponse.Statuses.get(statusesResponse.Statuses.size() - 1).Date.toString());
                jsonObject.put("trigger", statusesResponse.Statuses.get(statusesResponse.Statuses.size() - 1).Trigger);
                jsonObject.put("triggerName", statusesResponse.Statuses.get(statusesResponse.Statuses.size() - 1).TriggerName);
            }

            return jsonObject.toString();
        }
        catch(Exception e){
            System.err.println(e.toString());
            return null;
        }

    }

    private String waitForIP(String gatewayId){
        boolean found = false;
        String IP = "";

        DatagramSocket socket = null;

        while(!found){
            try {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                socket = new DatagramSocket(8998);
                socket.setBroadcast(true);
                socket.setSoTimeout(2000);

                socket.send(new DatagramPacket("ok".getBytes(), 2, InetAddress.getByName("255.255.255.255"), 8967));

                // This loops exit for time out
                while (!found) {
                    socket.receive(packet);

                    System.out.println("AutoDiscovered -> " + new String(packet.getData(), 0, packet.getLength()) + " on IP: " + packet.getAddress().getHostAddress() + " and port: " + packet.getPort());

                    if (new String(packet.getData(), 0, packet.getLength()).equalsIgnoreCase(gatewayId)) {
                        found = true;
                        IP = packet.getAddress().getHostAddress() + ":" + (packet.getPort() + 1);
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            } finally {
                if (socket != null){
                    socket.close();
                }
            }
        }

        System.out.println(IP);
        return IP;
    }

}
