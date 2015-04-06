package fujimiyaBot;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

@SuppressWarnings("serial")
public abstract class AbstractCron extends HttpServlet{

    static Logger logger = Logger.getLogger("Fujimiya");
    
    static String consumerKey = System.getenv("consumerKey");
    static String consumerSecret = System.getenv("consumerSecret");
    static String accessToken = System.getenv("accessToken");
    static String accessTokenSecret = System.getenv("accessTokenSecret");
    static String customSearchCx = System.getenv("customSearchCx");
    static String customSearchKey = System.getenv("customSeerchKey");

    static Twitter twitter;
    static Customsearch.Builder builder = new Customsearch.Builder(new NetHttpTransport(), new JacksonFactory(), null).setApplicationName("Google"); //$NON-NLS-1$
    static Customsearch search = builder.build();
    
    public AbstractCron() {
        logger.setLevel(Level.FINE);
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        //http://twitter4j.org/ja/configuration.html
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
            .setOAuthAccessToken(accessToken)
            .setOAuthAccessTokenSecret(accessTokenSecret)
            .setOAuthConsumerKey(consumerKey)
            .setOAuthConsumerSecret(consumerSecret);
        twitter = new TwitterFactory(cb.build()).getInstance();
        twitterCron();
    }

    /**
     * Search fujimiya-san's image and return the url.
     * The return is randomly picked up from the 100 result of google image search.
     * @param query
     * @return
     */
    FetchedImage getFujimiyaUrl(String query){
        return getFujimiyaUrl(query,100);
    }
    /**
     * Search fujimiya-san's image and return the url.
     * The return is randomly picked up from the maxRankOfResult result of google image search.
     * @param query
     * @param maxRankOfResult
     * @return
     */
    FetchedImage getFujimiyaUrl(String query,int maxRankOfResult){
        try{
            //Get SearchResult
            Search search = getSearchResult(query, maxRankOfResult);
            List<Result> items = search.getItems();
            for(int i=0;i<10;i++){
                Result result = items.get(i);
                logger.log(Level.INFO,"query: " + query + " URL: "+result.getLink());
                logger.log(Level.INFO,"page URL: "+result.getImage().getContextLink());
                HttpURLConnection connection = (HttpURLConnection)(new URL(result.getLink())).openConnection();
                if(DBConnection.isInBlackList(result.getLink())){
                    continue;
                }
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                if(connection.getResponseCode()==200){
                    return new FetchedImage(connection.getInputStream(),result.getLink());
                }else{
                    continue;
                }
            }
            //If execution comes here, connection has failed 10 times.
            throw new ConnectException("Connection failed 10 times");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.log(Level.SEVERE,e.toString());
            e.printStackTrace();
        }
        return null;
}
    
    static private int pageSize = 10;
    private Search getSearchResult(String query, int maxRankOfResult) throws IOException {
        if(maxRankOfResult>100-pageSize+1)
            maxRankOfResult=100-pageSize+1;
        Customsearch.Cse.List list = search.cse().list(query);
        
        list.setCx(customSearchCx);
        list.setKey(customSearchKey);
        list.setSearchType("image");
        list.setNum((long)pageSize);
        list.setImgSize("huge").setImgSize("large").setImgSize("medium").setImgSize("xlarge").setImgSize("xxlarge");
        
        long rand = (long)(Math.random()*maxRankOfResult+1);
        list.setStart(rand);
        logger.log(Level.INFO,"rand: "+rand);
        return list.execute();
    }

    protected void updateStatusWithMedia(StatusUpdate update, String query, int maxRankOfResult){
        FetchedImage fetchedImage = getFujimiyaUrl(query,maxRankOfResult);
        update.media("fujimiya.jpg",fetchedImage.getInputStream());
        for(int i=0;i<10;i++){
            try{
                Status succeededStatus = twitter.updateStatus(update);
                logger.log(Level.INFO,"Successfully tweeted: "+succeededStatus.getText());
                DBConnection.storeImageUrl(succeededStatus,fetchedImage);
                return;
            }catch(TwitterException e){
                logger.log(Level.INFO,"updateStatusWithMedia failed. try again. "+ e.getErrorMessage());
            }
        }
        logger.log(Level.SEVERE,"updateStatusWithMedia failed 10 times. Stop.");
    }
    
    
    abstract protected void twitterCron();
}

class FetchedImage{
    private InputStream in;
    private String url;
    public FetchedImage(InputStream in, String url) {
        this.in = in;
        this.url = url;
    }
    public InputStream getInputStream() {
        return in;
    }
    public String getUrl() {
        return url;
    }
}