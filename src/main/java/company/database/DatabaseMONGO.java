package company.database;

import com.mongodb.MongoClient;
import com.mongodb.client.*;
import company.entity.User;
import company.log.Log;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class DatabaseMONGO {
    Log log = new Log();
    private static final MongoClient mongoClient = new MongoClient();
    private static final MongoDatabase database = mongoClient.getDatabase("myFirstDb");;
    private static MongoCollection<Document> test;

    public boolean isUser(String name){
        test = database.getCollection("hobby");
        MongoCollection<Document> table = test;
        for (Document doc : table.find()){
            try {
                JSONObject object = (JSONObject) new JSONParser().parse(doc.toJson());
                if (object.containsValue(name))
                    return true;
            } catch (ParseException e) { e.printStackTrace(); }
        }
        return false;
    }

    public void insertUser(JSONObject object){
        //users dat
        test = database.getCollection("users");
        object.put("gender",
                object.get("gender") == null ? 2 : object.get("gender").equals("female") ? 1 : 0);
        Document doc = Document.parse(object.toString());
        test.insertOne(doc);
        log.print("User added to mongodb");
    }

    public List<User> getUsers(){
        //users dat
        List<User> list = new ArrayList<>();
        test = database.getCollection("users");
        MongoCollection<Document> table = test;
        for (Document doc : table.find()){
            try {
                JSONObject object = (JSONObject) new JSONParser().parse(doc.toJson());
                list.add(new User((String) object.get("fname"),
                        (String) object.get("lname"),
                        Integer.parseInt(String.valueOf(object.get("age"))),
                        Integer.parseInt(String.valueOf(object.get("gender")))
                ));
            } catch (ParseException e) { e.printStackTrace(); }
        }
        return list;
    }

    public void insertHobby(JSONObject object){
        //hobby
        test = database.getCollection("hobby");
        Document doc = Document.parse(object.toString());
        test.insertOne(doc);
        log.print("User added to mongodb");
    }

    public JSONObject getHobby(){
        //hobby
        test = database.getCollection("hobby");
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        MongoCollection<Document> table = test;
        for (Document doc : table.find()){
            try {
                JSONObject object = (JSONObject) new JSONParser().parse(doc.toJson());
                object.remove("_id");
                System.out.println(object);
                array.add(object);
            } catch (ParseException e) { e.printStackTrace(); }
        }
        json.put("length", array.size());
        json.put("users", array);
        return json;
    }

    public boolean deleteUser(String name){
        //hobby
        test = database.getCollection("hobby");
        Document query = new Document();
        query.put("name", name);
        test.deleteOne(query);
        return true;
    }


}
