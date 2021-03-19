package company.controller;

import company.log.Log;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecretController {
    Log log = new Log();
    private final String password = "Kosice2021!";

    @GetMapping("/secret")
    public String secret(){
        return "secret";
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String data){
        try {
            JSONObject object = (JSONObject) new JSONParser().parse(data);
            String login = (String) object.get("login");
            String pass = (String) object.get("password");
            if (login == null || pass == null || pass.isEmpty() || login.isEmpty()) {
                log.error("Missing login or password");
                return ResponseEntity.status(400).body("Missing name or password");
            }
            if (!pass.equals(password)) {
                log.error("Wrong password");
                return ResponseEntity.status(401).body("Wrong password");
            }
            return ResponseEntity.status(200).body("Welcome " + login);
        } catch (ParseException e) { e.printStackTrace(); }
        return null;
    }

}
