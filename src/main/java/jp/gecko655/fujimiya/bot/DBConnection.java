package jp.gecko655.fujimiya.bot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
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
import com.mongodb.client.model.UpdateOptions;

public class DBConnection {

    private static final Logger logger;
    static{
        logger = Logger.getLogger(DBConnection.class.getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(new StreamHandler(){{setOutputStream(System.out);}});
    }

    private static final MongoClientURI mongoClientURI = new MongoClientURI(System.getenv("MONGOLAB_URI"));
    private static final MongoClient client = new MongoClient(mongoClientURI);
    private static final MongoDatabase db = client.getDatabase(mongoClientURI.getDatabase());
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
        Document imageUrlDoc = imageUrlCollection.find(Filters.eq(statusIdKey, reply.getInReplyToStatusId())).first();
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
        Document doc = lastStatusCollection.find(Filters.exists(lastStatusKey)).first();
        if(doc==null)
            return null;
        return fromBase64(doc.getString(lastStatusKey));
    }

    public static void setLastStatus(Status status) {
        MongoCollection<Document> lastStatusCollection = db.getCollection(lastStatusCollectionName);
        Document document = new Document(lastStatusKey, toBase64(status));
        lastStatusCollection.replaceOne(Filters.exists(lastStatusKey), document, new UpdateOptions().upsert(true));
    }

    private static String toBase64(Status status) {
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(status);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        }catch(IOException e){
            throw new Error();
        }

    }
    
    private static Status fromBase64(String s) {
        try{
            byte[] bArray = Base64.getDecoder().decode(s);
            ByteArrayInputStream bis = new ByteArrayInputStream(bArray);
            ObjectInputStream in = new ObjectInputStream(bis);
            return (Status) in.readObject();
        }catch(IOException | ClassNotFoundException e){
            throw new Error();
        }
        
    }

}
