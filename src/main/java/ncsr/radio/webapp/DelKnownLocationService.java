package ncsr.radio.webapp;

import net.minidev.json.parser.JSONParser;
import org.json.JSONArray;
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
import java.util.List;

/**
 * Created by gstavrinos on 3/3/16.
 */

@Path("/delrooms")
public class DelKnownLocationService {


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
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!found) {
            return Response.status(404).build();
        }

        try {
            System.out.println(sb1.toString());
            JSONObject current_rooms = new JSONObject(sb1.toString());

            StringBuilder sb2 = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(indata));
            while ((line = in.readLine()) != null) {
                sb2.append(line);
            }
            System.out.println("Data Received: " + sb2.toString());
            JSONObject removed_rooms = new JSONObject(sb2.toString());


            JSONArray ja = current_rooms.getJSONArray("rooms");
            JSONArray ja2 = removed_rooms.getJSONArray("ids");
            found = false; //now we use found for ids instead of the file.

            if (ja2 != null) {
                for (int i = 0; i < ja2.length(); i++) {
                    int temp = ja2.getJSONObject(i).getInt("id");
                    if (ja != null) {
                        int k = 0;
                        while (true) {
                            if (ja.length() > 0){
                                if (temp == ja.getJSONObject(k).getInt("id")) {
                                    found = true;
                                    ja.remove(k);
                                } else {
                                    k++;
                                }
                                if (k >= ja.length()) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (found){
                JSONObject new_rooms = new JSONObject();
                new_rooms.put("rooms", ja);
                new_rooms.put("version", current_rooms.get("version"));


                String current_rooms_string = new_rooms.toString();
                //Let's make the output file a bit easier to read
                current_rooms_string = current_rooms_string.replace("},", "},\n");
                current_rooms_string = current_rooms_string.replace("[", "\n[\n");
                current_rooms_string = current_rooms_string.replace("]", "\n]\n");
                current_rooms_string = current_rooms_string.replace(",", ", ");
                java.nio.file.Path file = Paths.get(this.getClass().getClassLoader().getResource(".").toString().replace("file:", "") + "rooms.json");
                List<String> lines = new ArrayList<String>();
                String l[] = current_rooms_string.split("\n");
                for (String i : l) {
                    lines.add(i);
                }
                Files.write(file, lines, Charset.forName("UTF-8"));
                System.out.println(current_rooms_string);
            }

            return Response.status(200).type("application/json;charset=utf-8").build();
        }
        catch (Exception e) {
            e.printStackTrace();

            return Response.status(500).type("text/plain;charset=utf-8").entity(e.getStackTrace()).build();
        }

    }

}
