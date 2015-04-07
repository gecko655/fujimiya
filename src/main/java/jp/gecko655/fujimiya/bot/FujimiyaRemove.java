package jp.gecko655.fujimiya.bot;

import twitter4j.PagableResponseList;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.User;

@SuppressWarnings("serial")
public class FujimiyaRemove extends AbstractCron {

    @Override
    protected void twitterCron() {
        try {
            int friendsCount= twitter.verifyCredentials().getFriendsCount();
            if(friendsCount==0){
                return;
            }
            long cursor = -1L;
            while(cursor!=0L){
                PagableResponseList<User> followers = twitter.getFriendsList(twitter.getId(), cursor);
                for(User follower: followers){
                    twitter.destroyFriendship(follower.getId());
                    twitter.updateStatus(new StatusUpdate("@"+follower.getScreenName()+" あなた、誰？"));
                    Thread.sleep(5*60*1000/friendsCount);
                }
                cursor = followers.getNextCursor();
            }
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TwitterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
