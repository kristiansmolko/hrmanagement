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

            return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(json.toJSONString());
        } catch (ParseException e) { log.error(e.toString()); }
        return null;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.DELETE)
    public ResponseEntity<String> logout(@RequestBody String data){
        try {
            JSONObject object = (JSONObject) new JSONParser().parse(data);
            String user = (String) object.get("user");
            for (Map.Entry<String, String> entry : map.entrySet())
                if (entry.getKey().equals(user)) {
                    map.remove(user);
                    return ResponseEntity.status(200).body("User logged out");
                }
        } catch (Exception e) { log.error(e.toString()); }
        return ResponseEntity.status(400).body("Error");
    }

}
