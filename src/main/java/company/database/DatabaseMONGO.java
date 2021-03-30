package company.database;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
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
    private static MongoDatabase database;
    private static MongoCollection<Document> test;

    public void insertUser(JSONObject object){
        database = mongoClient.getDatabase("myFirstDb");
        test = database.getCollection("users");
        object.put("gender",
                object.get("gender") == null ? 2 : object.get("gender").equals("female") ? 1 : 0);
        Document doc = Document.parse(object.toString());
        test.insertOne(doc);
        log.print("User added to mongodb");
    }

    public List<User> getUsers(){
        List<User> list = new ArrayList<>();
        database = mongoClient.getDatabase("myFirstDb");
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
        database = mongoClient.getDatabase("myFirstDb");
        test = database.getCollection("hobby");
        Document doc = Document.parse(object.toString());
        test.insertOne(doc);
        log.print("User added to mongodb");
    }

    public JSONObject getHobby(){
        database = mongoClient.getDatabase("myFirstDb");
        test = database.getCollection("hobby");
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        MongoCollection<Document> table = test;
        for (Document doc : table.find()){
            try {
                JSONObject object = (JSONObject) new JSONParser().parse(doc.toJson());
                System.out.println(object);
                array.add(object);
            } catch (ParseException e) { e.printStackTrace(); }
        }
        json.put("length", array.size());
        json.put("users", array);
        return json;
    }
}
