package ncsr.radio.webapp;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by gstavrinos on 3/2/16.
 */

@Path("/getrooms")
public class GetKnownLocationsService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRooms() throws JSONException {
        try {
            InputStream is = this.getClass().getResourceAsStream("/rooms.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            reader.close();
            String result = out.toString();
            return Response.status(200).entity(result).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity(e.getStackTrace()).build();
        }
    }
}