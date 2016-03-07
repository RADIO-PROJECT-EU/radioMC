package ncsr.radio.webapp;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.json.JSONObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gstavrinos on 3/3/16.
 */

@Path("/addrooms")
public class AddKnownLocationsService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setKnownLocation(InputStream indata){

        File f = new File(this.getClass().getClassLoader().getResource(".").toString().replace("file:","")+"rooms.json");
        boolean found = false;
        StringBuilder sb1 = new StringBuilder();
        String line = null;
        if(f.exists() && !f.isDirectory()) {
            if(f.length() > 0) {
                found = true;
            }
            InputStream is = this.getClass().getResourceAsStream("/rooms.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            try {
                while ((line = reader.readLine()) != null) {
                    sb1.append(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            System.out.println(sb1.toString());
            JSONObject current_rooms;
            if(found){
                current_rooms = new JSONObject(sb1.toString());
            }
            else{
                current_rooms = new JSONObject();
            }

            StringBuilder sb2 = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(indata));
            while ((line = in.readLine()) != null) {
                sb2.append(line);
            }
            System.out.println("Data Received: " + sb2.toString());
            JSONObject added_rooms = new JSONObject(sb2.toString());

            JSONParser p = new JSONParser(JSONParser.MODE_JSON_SIMPLE);

            net.minidev.json.JSONObject current_rooms_ = (net.minidev.json.JSONObject) p.parse(current_rooms.toString());
            net.minidev.json.JSONObject added_rooms_ = (net.minidev.json.JSONObject) p.parse(added_rooms.toString());

            current_rooms_.merge(added_rooms_);
            String current_rooms_string = current_rooms_.toString();
            //Let's make the output file a bit easier to read
            current_rooms_string = current_rooms_string.replace("},","},\n");
            current_rooms_string = current_rooms_string.replace("[","\n[\n");
            current_rooms_string = current_rooms_string.replace("]","\n]\n");
            current_rooms_string = current_rooms_string.replace(",",", ");
            System.out.println(this.getClass().getClassLoader().getResource(".").toString().replace("file:","")+"rooms.json");
            java.nio.file.Path file = Paths.get(this.getClass().getClassLoader().getResource(".").toString().replace("file:","")+"rooms.json");
            List<String> lines = new ArrayList<String>();
            String l[] = current_rooms_string.split("\n");
            for(String i: l) {
                lines.add(i);
            }
            Files.write(file, lines, Charset.forName("UTF-8"));
            System.out.println(current_rooms_string);

            return Response.status(200).build();
        }
        catch (Exception e) {
            e.printStackTrace();

            return Response.status(500).build();
        }
    }

}
