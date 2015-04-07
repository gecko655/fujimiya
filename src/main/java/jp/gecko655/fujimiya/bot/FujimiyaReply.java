package jp.gecko655.fujimiya.bot;


import java.text.DateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Pattern;

import twitter4j.Paging;
import twitter4j.Relationship;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

@SuppressWarnings("serial")
public class FujimiyaReply extends AbstractCron {
    
    static final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
    private static final Pattern keishouPattern = Pattern.compile("(くん|さん|君|ちゃん)$");
    private static final Pattern whoPattern = Pattern.compile("( 誰$| だれ$|誰[^だで]|だれ[^だで]|誰だ[^と]?|だれだ[^と]?| 違う$)");

    public FujimiyaReply() {
        format.setTimeZone(TimeZone.getDefault());
    }

    @Override
    protected void twitterCron() {
        try {
            Status lastStatus = DBConnection.getLastStatus();
            List<Status> replies = twitter.getMentionsTimeline((new Paging()).count(20));
            DBConnection.setLastStatus(replies.get(0));
             if(lastStatus == null){
                 logger.log(Level.INFO,"memcache saved. Stop. "+replies.get(0).getUser().getName()+"'s tweet at "+format.format(replies.get(0).getCreatedAt()));
                 return;
             }
            
            for(Status reply : replies){
                if(isOutOfDate(reply, lastStatus))
                    break;
                Relationship relation = twitter.friendsFollowers().showFriendship(twitter.getId(), reply.getUser().getId());
                
                if(!relation.isSourceFollowingTarget()){
                    followBack(reply);
                }else if(whoPattern.matcher(reply.getText()).find()){
                    // put latest image URL to black-list
                    who(reply);    
                }else{
                    //auto reply (when fujimiya-san follows the replier)
                    StatusUpdate update= new StatusUpdate("@"+reply.getUser().getScreenName()+" ");
                    update.setInReplyToStatusId(reply.getId());
                    updateStatusWithMedia(update, "藤宮香織 かわいい 一週間フレンズ。",100);
                }
            }
        } catch (TwitterException e) {
            logger.log(Level.WARNING,e.toString());
            e.printStackTrace();
        }
    }

    private boolean isOutOfDate(Status reply, Status lastStatus) {
        if(reply.getCreatedAt().getTime()-lastStatus.getCreatedAt().getTime()<=0){
            logger.log(Level.INFO, reply.getUser().getName()+"'s tweet at "+format.format(reply.getCreatedAt()) +" is out of date");
            return true;
        }
        return false;
    }

    private void followBack(Status reply) throws TwitterException {
        twitter.createFriendship(reply.getUser().getId());
        String userName = reply.getUser().getName();
        if(!keishouPattern.matcher(userName).find()){
            userName = userName + "くん";
        }
        StatusUpdate update= new StatusUpdate("@"+reply.getUser().getScreenName()+" もしかして、あなたが"+userName+"？");
        update.setInReplyToStatusId(reply.getId());
        twitter.updateStatus(update);
    }

    private void who(Status reply) {
        DBConnection.storeImageUrlToBlackList(reply);
    }
}
