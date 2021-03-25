package company.controller;

import company.log.Log;
import company.user.Login;
import company.utill.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SecretController {
    Log log = new Log();
    Util util = new Util();
    Login login = new Login();
    Map<String, String> map = new HashMap();
    private ResponseEntity.BodyBuilder unauthorized = ResponseEntity.status(401);
    private ResponseEntity.BodyBuilder badRequest = ResponseEntity.status(400);
    private ResponseEntity.BodyBuilder ok = ResponseEntity.status(200);

    @GetMapping("/secret")
    public ResponseEntity<String> secret(@RequestHeader("token") String token){
        if (!util.authorizeToken(token))
            return badRequest.body("Wrong token");
        for (Map.Entry<String, String> entry : map.entrySet())
            if (entry.getValue().equals(token))
                return ok.body("Welcome message for " + entry.getKey());
        log.error("Unauthorized access");
        return unauthorized.body("Unauthorized access");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String data){
        try {
            JSONObject object = (JSONObject) new JSONParser().parse(data);
            String user = (String) object.get("login");
            String pass = (String) object.get("password");
            if (user == null || pass == null || pass.isEmpty() || user.isEmpty()) {
                log.error("Missing login or password");
                return ResponseEntity.status(400).body("Missing name or password");
            }
            String token = login.loginUser(user, pass);
            if (token == null)
                return ResponseEntity.status(400).body("Wrong password");
            else if (token.equals("401"))
                return ResponseEntity.status(401).body("Wait minute before logging");
            map.put(user, token);
            JSONObject json = new JSONObject();
            json.put("login", user);
            json.put("token", token);
            log.print("User logged in");
            return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(json.toJSONString());
        } catch (ParseException e) { log.error(e.toString()); }
        log.error("Error");
        return ResponseEntity.status(400).body("Error");
    }

    @RequestMapping(value = "/logout", method = RequestMethod.DELETE)
    public ResponseEntity<String> logout(@RequestBody String data){
        try {
            JSONObject object = (JSONObject) new JSONParser().parse(data);
            String token = (String) object.get("token");
            for (Map.Entry<String, String> entry : map.entrySet())
                if (entry.getValue().equals(token)) {
                    map.remove(entry.getKey());
                    log.info("Logged out user " + entry.getKey());
                    return ResponseEntity.status(200).body("User logged out");
                }
            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) { log.error(e.toString()); }
        log.error("Error");
        return ResponseEntity.status(400).body("Error");
    }

    @RequestMapping(value = "/booking", method = RequestMethod.GET)
    public ResponseEntity<String> getBooking(@RequestBody String body) throws ParseException {
        JSONObject response = new JSONObject();
        response.put("Hotel", "Kosice Hotel");
        response.put("Country", "Slovakia");
        response.put("City", "Kosice");
        response.put("Status", "Pending");
        JSONObject object = (JSONObject) new JSONParser().parse(body);
        String token = String.valueOf(object.get("token"));
        if (!token.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet())
                if (entry.getValue().equals(token)) {
                    response.put("User", entry.getKey());
                    response.remove("Status");
                    response.put("Status", "Pay 200$");
                }
        }
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(
                response.toJSONString());
    }

}
