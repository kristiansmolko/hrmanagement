package company.controller;

import company.database.DatabaseMONGO;
import company.database.DatabaseMYSQL;
import company.entity.User;
import company.entity.XMLList;
import company.enums.Gender;
import company.log.Log;
import company.utill.Util;
import io.swagger.annotations.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.List;

@RestController
//@RequestMapping("/api")
public class Controller {
    DatabaseMYSQL dat = new DatabaseMYSQL();
    DatabaseMONGO mongoDat = new DatabaseMONGO();
    Log log = new Log();
    Util util = new Util();

    @GetMapping("/users")
    public ResponseEntity<String> getUsers(){
        List<User> list = dat.getAllUsers();
        String object = util.getJson(list);
        log.print("Users found");
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object);
    }

    @PostMapping("/user/new")
    @ApiOperation(value = "Insert new user to database",
            notes = "Accepts json with fname, lname and age value, gender is not mandatory (default value: other)")
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
            mongoDat.insertUser(object);
        } catch (Exception e) {
            log.error("Wrong input data");
            return ResponseEntity.status(400).body("Wrong input data");
        }
        return ResponseEntity.status(201).contentType(MediaType.APPLICATION_JSON).body(response.toJSONString());
    }

    @GetMapping(value = "/user", params = "gender")
    @ApiOperation(value = "Get users by gender")
    public ResponseEntity<String> getUsersByGender(@RequestParam(value = "gender")String gender){
        List<User> list = null;
        if (gender.equalsIgnoreCase("male")) list = dat.getMales();
        else if (gender.equalsIgnoreCase("female")) list = dat.getFemales();
        else return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(null);
        String object = util.getJson(list);
        log.print("Users found");
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object);
    }

    @GetMapping("/user/age")
    public ResponseEntity<String> getUsersByAge(@RequestParam(value = "from") int value1, @RequestParam(value = "to") int value2){
        if (value1 <= 0 || value2 <= 0 || value1 > value2 || value1 >= 100 || value2 >= 100){
            JSONObject error = new JSONObject();
            log.error("Wrong input");
            error.put("error", "Wrong input");
            return  ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(error.toJSONString());
        }
        List<User> list = dat.getUsersByAge(value1, value2);
        String object = util.getJson(list);
        log.print("Users found");
        return  ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object);
    }

    @GetMapping("/user/{id}")
    @ApiOperation(
            value = "Get user based on ID",
            notes = "Accepts number and returns user with specified id, if exist",
            responseContainer = "List",
            response = User.class
    )
//    @ApiResponse(
//            code = 200,
//            message = "Successfully found user",
//            response = User.class,
//            responseContainer = "List"
//    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Successfully found user", responseContainer = "List"),
                    @ApiResponse(code = 404, message = "Could not find user"),
                    @ApiResponse(code = 500, message = "Internal error")
            }
    )
    public ResponseEntity<String> getUsersById(@ApiParam(value = "ID value for user", required = true)
                                                   @PathVariable int id){
        User u = dat.getUserById(id);
        if (u == null) {
            JSONObject error = new JSONObject();
            log.error("This user does not exist");
            error.put("error", "This user does not exist");
            return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON).body(error.toJSONString());
        }
        String object = util.getJson(u);
        log.print("User found");
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object);
    }

    @GetMapping(value = "/user", params = "search")
    public ResponseEntity<String> getUsersBySubstring(@RequestParam(value = "search") String substring){
        List<User> list = dat.getUser(substring);
        if (list == null){
            JSONObject error = new JSONObject();
            log.error("Incorrect parameter");
            error.put("error", "Incorrect parameter");
            return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(error.toJSONString());
        }
        String object = util.getJson(list);
        log.print("Users found");
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object);
    }

    @GetMapping("/")
    public ResponseEntity<String> getDetails(){
        JSONObject object = new JSONObject();
        List<User> list = dat.getAllUsers();
        double age = 0;
        int min = 100, max = 0, females = 0, males = 0;
        for (User u : list){
            int tempAge = u.getAge();
            age -= -tempAge;
            if (tempAge < min) min = tempAge;
            else if (tempAge > max) max = tempAge;

            if (u.getGender().equals(Gender.MALE)) males -= -1;
            else if (u.getGender().equals(Gender.FEMALE)) females -= -1;
        }
        object.put("count", list.size());
        object.put("males", males);
        object.put("females", females);
        object.put("age", util.normalizeNum(age/list.size()));
        object.put("min", min);
        object.put("max", max);
        log.print("Success");
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object.toJSONString());
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<String> updateUserAge(@PathVariable int id, @RequestBody String data){
        if (dat.getUserById(id) == null){
            JSONObject error = new JSONObject();
            error.put("error", "User not found");
            log.error("User not found");
            return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON).body(error.toJSONString());
        }
        try {
            JSONObject object = (JSONObject) new JSONParser().parse(data);
            int newAge = Integer.parseInt(String.valueOf(object.get("newage")));
            if (newAge <= 0 || newAge > 99){
                JSONObject error = new JSONObject();
                error.put("error", "Wrong input");
                log.error("Wrong input");
                return ResponseEntity.status(400).contentType(MediaType.APPLICATION_JSON).body(error.toJSONString());
            }
            dat.changeAge(id, newAge);
        } catch (ParseException e) { log.error(e.toString()); }
        JSONObject object = new JSONObject();
        object.put("info", "Age changed");
        log.print("Age changed");
        return ResponseEntity.status(204).contentType(MediaType.APPLICATION_JSON).body(object.toJSONString());
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id){
        if (dat.getUserById(id) == null){
            JSONObject error = new JSONObject();
            error.put("error", "User not found");
            log.error("User not found");
            return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON).body(error.toJSONString());
        }
        dat.deleteUser(id);
        log.print("User deleted");
        return ResponseEntity.status(204).contentType(MediaType.APPLICATION_JSON).body(null);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        JSONObject object = new JSONObject();
        object.put("test", 15);
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object.toJSONString());
    }

    @GetMapping(value = "/users", params = "type")
    public ResponseEntity<String> getUsersXML(@RequestParam(value = "type") String type){
        if(type == null || !type.equals("xml"))
            return ResponseEntity.status(400).body("Wrong type");
        List<User> list = dat.getAllUsers();
        XMLList xml = new XMLList();
        xml.setList(list);
        try {
            JAXBContext context = JAXBContext.newInstance(company.entity.XMLList.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            marshaller.marshal(xml, sw);
            return ResponseEntity.status(200).contentType(MediaType.APPLICATION_XML).body(sw.toString());
        } catch (JAXBException e) { log.error(e.toString()); }
        return ResponseEntity.status(400).body("Error");
    }

    @GetMapping("/backend")
    public ResponseEntity<String> getMongoUsers(){
        List<User> list = mongoDat.getUsers();
        String object = util.getJson(list);
        log.print("Users found");
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object);
    }

    @PostMapping(value = "/hobby/user/{name}")
    public ResponseEntity<String> addUserHobby(@PathVariable String name, @RequestBody String data){
        if (name == null || name.isEmpty()) {
            log.error("Missing name");
            return ResponseEntity.status(400).body("Missing name");
        }
        if (mongoDat.isUser(name)){
            mongoDat.updateHobby(name, data);
            log.print("User updated");
            return ResponseEntity.status(200).body("User");
        }
        try {
            JSONObject object = (JSONObject) new JSONParser().parse(data);
            object.put("name", name);
            mongoDat.insertHobby(object);
            log.print("User created");
            return ResponseEntity.status(201).body("Added user with hobby");
        } catch (ParseException e) { log.error(e.toString()); }
        log.error("Error");
        return ResponseEntity.status(400).body("Error");
    }

    @GetMapping("/hobby/users")
    public ResponseEntity<String> getHobbies(){
        JSONObject object = mongoDat.getHobby();
        log.print("All users");
        return ResponseEntity.status(200).contentType(MediaType.APPLICATION_JSON).body(object.toJSONString());
    }

    @DeleteMapping("/hobby/user/{name}")
    public ResponseEntity<String> deleteUser(@PathVariable String name){
        if (!mongoDat.isUser(name)) {
            log.error("User not found");
            return ResponseEntity.status(404).body("User not found");
        }
        if (mongoDat.deleteUser(name)) {
            log.print("User deleted");
            return ResponseEntity.status(200).body("User deleted");
        }
        log.error("Error");
        return ResponseEntity.status(400).body("Error");
    }
}
