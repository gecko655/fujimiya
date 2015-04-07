package jp.gecko655.fujimiya.bot;

import twitter4j.StatusUpdate;

@SuppressWarnings("serial")
public class FujimiyaLunch extends AbstractCron{

    @Override
    protected void twitterCron() {
        //Twitterに書き出し
        StatusUpdate status =new StatusUpdate(" "); //$NON-NLS-1$
        updateStatusWithMedia(status, "藤宮さん 昼", 100);//$NON-NLS-1$
        
    }

}
