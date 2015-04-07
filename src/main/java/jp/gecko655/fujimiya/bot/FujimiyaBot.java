package jp.gecko655.fujimiya.bot;

import twitter4j.StatusUpdate;

@SuppressWarnings("serial")
public class FujimiyaBot extends AbstractCron{

    @Override
    protected void twitterCron() {
        //Twitterに書き出し
        StatusUpdate status = new StatusUpdate(" "); //$NON-NLS-1$
        if(((int) (Math.random()*10))==1){//10%
            updateStatusWithMedia(status, "山岸さん 一週間フレンズ。", 100);
        }else{
            updateStatusWithMedia(status, "藤宮さん 一週間フレンズ。", 100);
        }
        
    }

}
