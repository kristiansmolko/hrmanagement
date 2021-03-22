package company.controller;

import company.log.Log;
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
    Map<String, String> map = new HashMap();
    private final String password = "Kosice2021!";
    private ResponseEntity.BodyBuilder unauthorized = ResponseEntity.status(401);
    private ResponseEntity.BodyBuilder badRequest = ResponseEntity.status(400);
    private ResponseEntity.BodyBuilder ok = ResponseEntity.status(200);

    @GetMapping("/secret")
    public ResponseEntity<String> secret(@RequestHeader("token") String token){
        if (!util.authorizeToken(token))
            return badRequest.body("Wrong token");
        String tokenNum = token.substring(7);
        for (Map.Entry<String, String> entry : map.entrySet())
            if (entry.getValue().equals(tokenNum))
                return ok.body("Welcome message for " + entry.getKey());
        log.error("Unauthorized access");
        return unauthorized.body("Unauthorized access");
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
            String token = util.generateToken();
            map.put(login, token);
            JSONObject json = new JSONObject();
            json.put("login", login);
            json.put("token", "Bearer " + token);
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
            String token = ((String) object.get("token")).substring(7);
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
            String tokenNum = token.substring(7);
            for (Map.Entry<String, String> entry : map.entrySet())
                if (entry.getValue().equals(tokenNum)) {
                    response.put("User", entry.getKey());
                    response.remove("Status");
                    response.put("Status", "Pay 200$");
                }
        }
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(
                response.toJSONString());
    }

}
