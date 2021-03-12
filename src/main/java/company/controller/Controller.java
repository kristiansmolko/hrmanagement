package company.controller;

import company.database.Database;
import company.entity.User;
import company.enums.Gender;
import company.log.Log;
import company.utill.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Controller {
    Database dat = new Database();
    Log log = new Log();
    Util util = new Util();

    @GetMapping("/users")
    public ResponseEntity<String> getUsers(){
        List<User> list = dat.getAllUsers();
        String object = util.getJson(list);
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object);
    }

    @PostMapping("/user/new")
    public ResponseEntity<String> insertNewUser(@RequestBody String data){
        JSONObject response = new JSONObject();
        try {
            JSONObject object = (JSONObject) new JSONParser().parse(data);
            String fname = (String) object.get("fname");
            String lname = (String) object.get("lname");
            int age = Integer.parseInt(String.valueOf(object.get("age")));
            String gender = (String) object.get("gender");
            if (fname == null || fname.equals("")){
                log.error("Wrong first name");
                JSONObject error = new JSONObject();
                error.put("error", "Wrong first name");
                return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(error.toJSONString());
            }
            if (lname == null || lname.equals("")){
                log.error("Wrong last name");
                JSONObject error = new JSONObject();
                error.put("error", "Wrong last name");
                return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(error.toJSONString());
            }
            if (age <= 0){
                log.error("Incorrect age");
                JSONObject error = new JSONObject();
                error.put("error", "Incorrect age");
                return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(error.toJSONString());
            }
            Gender g = gender==null?Gender.OTHER:gender.equalsIgnoreCase("male")?
                    Gender.MALE:gender.equalsIgnoreCase("female")? Gender.FEMALE:Gender.OTHER;
            User newUser = new User(fname, lname, age, g.getValue());
            dat.insertNewUser(newUser);
            response.put("info", "User added");
            log.info("User added: " + newUser.toString());

        } catch (Exception e) { log.error("Wrong input data"); }
        return ResponseEntity.status(201).contentType(MediaType.APPLICATION_JSON).body(response.toJSONString());
    }

    @GetMapping("/user/{input}")
    public ResponseEntity<String> getUsersBy(){
        List<User> list = dat.getMales();
        String object = util.getJson(list);
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object);
    }
}
