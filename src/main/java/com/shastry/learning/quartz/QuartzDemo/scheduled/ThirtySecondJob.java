package com.shastry.learning.quartz.QuartzDemo.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import java.time.OffsetDateTime;

@Slf4j
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class ThirtySecondJob implements Job {

    /**
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing ThirtySecondJob at {}", OffsetDateTime.now().toInstant());
    }
}
