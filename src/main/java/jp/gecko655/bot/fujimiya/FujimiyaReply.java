package jp.gecko655.bot.fujimiya;


import java.text.DateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jp.gecko655.bot.AbstractCron;
import jp.gecko655.bot.DBConnection;
import twitter4j.Paging;
import twitter4j.Relationship;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;

public class FujimiyaReply extends AbstractCron {
    
    static final DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
    private static final Pattern keishouPattern = Pattern.compile("(くん|さん|君|ちゃん)$");
    private static final Pattern whoPattern = Pattern.compile("( 誰$| だれ$|誰[^だで]|だれ[^だで]|誰だ[^と]?|だれだ[^と]?| 違う| ちがう)");

    public FujimiyaReply() {
        format.setTimeZone(TimeZone.getDefault());
    }

    @Override
    protected void twitterCron() {
        try {
            Status lastStatus = DBConnection.getLastStatus();
            List<Status> replies = twitter.getMentionsTimeline((new Paging()).count(20))
                    .stream().filter(reply -> !isOutOfDate(reply, lastStatus)).collect(Collectors.toList());
            if(replies.isEmpty()){
                logger.log(Level.FINE, "Not yet replied. Stop.");
                return;
            }
            DBConnection.setLastStatus(replies.get(0));
             if(lastStatus == null){
                 logger.log(Level.INFO,"memcache saved. Stop. "+replies.get(0).getUser().getName()+"'s tweet at "+format.format(replies.get(0).getCreatedAt()));
                 return;
             }
            
            for(Status reply : replies){
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
                    if(((int) (Math.random()*10))==1){//10%
                        updateStatusWithMedia(update, "山岸沙希 かわいい 一週間フレンズ。", 100);
                    }else{
                        updateStatusWithMedia(update, "藤宮香織 かわいい 一週間フレンズ。",100);
                    }
                }
            }
        } catch (TwitterException e) {
            logger.log(Level.WARNING,e.toString());
            e.printStackTrace();
        }
    }

    private boolean isOutOfDate(Status reply, Status lastStatus) {
        if(lastStatus==null) return false;
        return reply.getCreatedAt().before(lastStatus.getCreatedAt())
                ||reply.getCreatedAt().equals(lastStatus.getCreatedAt());
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
        //Store the url to the black list.
        DBConnection.storeImageUrlToBlackList(reply.getInReplyToStatusId(),reply.getUser().getScreenName());

        try{
            //Delete the reported tweet.
            twitter.destroyStatus(reply.getInReplyToStatusId());
            
            //Apologize to the report user.
            StatusUpdate update= new StatusUpdate("@"+reply.getUser().getScreenName()+" 間違えちゃった。ごめんね！");
            update.setInReplyToStatusId(reply.getId());
            twitter.updateStatus(update);
        }catch(TwitterException e){
            e.printStackTrace();
        }
    }
}
