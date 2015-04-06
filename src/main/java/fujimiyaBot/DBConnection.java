package fujimiyaBot;

import org.bson.Document;

import twitter4j.Status;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DBConnection {

    static MongoClientURI mongoClientURI = new MongoClientURI(System.getenv("MONGOLAB_URI"));
    static MongoClient client = new MongoClient(mongoClientURI);
    static MongoDatabase db = client.getDatabase(mongoClientURI.getDatabase());
    public static void storeImageUrlToBlackList(Status reply) {
        String blackListDBName = "blacklist";
    }

    public static void storeImageUrl(Status succeededStatus,
            FetchedImage fetchedImage) {
        // TODO Auto-generated method stub
        
    }

    public static Status getLastStatus() {
        Document doc = new Document("c","d");
        MongoCollection<Document> con = db.getCollection("collection");
        con.insertOne(doc);
        // TODO Auto-generated method stub
        return null;
    }

    public static void setLastStatus(Status status) {
        // TODO Auto-generated method stub
        
    }

    public static boolean isInBlackList(String link) {
        // TODO Auto-generated method stub
        return false;
    }

}
