package jp.gecko655.fujimiya.bot;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever;
import static org.quartz.TriggerBuilder.newTrigger;

public class SchedulerMain {
    private static Scheduler scheduler;
    public static void main(String[] args) throws SchedulerException {
        System.out.println("Scheduler Started!!!");
        scheduler = StdSchedulerFactory.getDefaultScheduler();

        scheduler.start();
        setSchedule(FujimiyaReply.class, 60*5);
        //setSchedule(FujimiyaRemove.class, 60*5);

    }
    private static void setSchedule(Class<? extends Job> classForExecute, int intervalSeconds) throws SchedulerException {
        JobDetail jobDetail = newJob(classForExecute).build();

        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(repeatSecondlyForever(intervalSeconds))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        System.out.println(classForExecute.getName()+" has been scheduled in interval: "+intervalSeconds+" [s]");
        
    }

}