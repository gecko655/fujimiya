package fujimiyaBot;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import org.bson.Document;

import twitter4j.Status;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class DBConnection {

    private static final Logger logger;
    static{
        logger = Logger.getLogger(DBConnection.class.getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(new StreamHandler(){{setOutputStream(System.out);}});
    }

    private static final MongoDatabase db;
    static{
        MongoClientURI mongoClientURI = new MongoClientURI(System.getenv("MONGOLAB_URI"));
        try(MongoClient client = new MongoClient(mongoClientURI)){
            db = client.getDatabase(mongoClientURI.getDatabase());
        }
    }
    private static final String imageUrlCollectionName = "imageUrl";
    private static final String blackListCollectionName = "blackList";
    private static final String lastStatusCollectionName = "lastStatus";
    private static final String urlKey = "url";
    private static final String reportedUserKey = "reporteduser";
    private static final String statusIdKey = "statusid";
    private static final String lastStatusKey = "laststatus";

    public static void storeImageUrl(Status succeededStatus,
            FetchedImage fetchedImage) {
        MongoCollection<Document> collection = db.getCollection(imageUrlCollectionName);
        Document doc = new Document(statusIdKey, succeededStatus.getId());
        doc.put(urlKey, fetchedImage.getUrl());
        collection.insertOne(doc);
    }

    public static void storeImageUrlToBlackList(Status reply) {
        MongoCollection<Document> imageUrlCollection = db.getCollection(imageUrlCollectionName);
        Document imageUrlDoc = imageUrlCollection.find(Filters.eq(statusIdKey, reply.getId())).first();
        if(imageUrlDoc!=null){
            String url = imageUrlDoc.getString(urlKey);
            MongoCollection<Document> blackListCollection = db.getCollection(blackListCollectionName);
            Document doc = new Document(urlKey,url);
            doc.put(reportedUserKey, reply.getUser().getScreenName());
            blackListCollection.insertOne(doc);
        }else{
            logger.log(Level.WARNING,"Image URL was not found in data collection");
        }
    }

    public static boolean isInBlackList(String link) {
        MongoCollection<Document> blackListCollection = db.getCollection(blackListCollectionName);
        return blackListCollection.find(Filters.eq(urlKey, link)).iterator().hasNext();
    }

    public static Status getLastStatus() {
        MongoCollection<Document> lastStatusCollection = db.getCollection(lastStatusCollectionName);
        return lastStatusCollection.find().first().get(lastStatusKey, Status.class);
    }

    public static void setLastStatus(Status status) {
        MongoCollection<Document> lastStatusCollection = db.getCollection(lastStatusCollectionName);
        Document document = new Document(lastStatusKey, status);
        lastStatusCollection.insertOne(document);
    }

}
