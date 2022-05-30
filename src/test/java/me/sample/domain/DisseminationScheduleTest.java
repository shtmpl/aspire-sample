package me.sample.domain;

import org.junit.Test;
import org.quartz.CronExpression;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DisseminationScheduleTest {

    @Test
    public void shouldDefineScheduleForEveryMinute() throws Exception {
        String cronExpression = DisseminationSchedule.EVERY_MINUTE.getCronExpression();
        System.out.printf("Cron: %s%n", cronExpression);

        CronExpression expression = new CronExpression(cronExpression);

        LocalDateTime now = LocalDateTime.now();
        System.out.printf("Now: %s%n", now);

        nextFireTimes(expression, now)
                .limit(4)
                .forEach((LocalDateTime it) ->
                        System.out.printf("Next: %s%n", it));
    }

    @Test
    public void shouldDefineScheduleForEveryDayAtStartOfDay() throws Exception {
        String cronExpression = DisseminationSchedule.EVERY_DAY_AT_START_OF_DAY.getCronExpression();
        System.out.printf("Cron: %s%n", cronExpression);

        CronExpression expression = new CronExpression(cronExpression);

        LocalDateTime now = LocalDateTime.now();
        System.out.printf("Now: %s%n", now);

        nextFireTimes(expression, now)
                .limit(4)
                .forEach((LocalDateTime it) ->
                        System.out.printf("Next: %s%n", it));
    }

    @Test
    public void shouldDefineScheduleForEverySundayAtMiddleOfDay() throws Exception {
        String cronExpression = DisseminationSchedule.EVERY_SUNDAY_AT_MIDDLE_OF_DAY.getCronExpression();
        System.out.printf("Cron: %s%n", cronExpression);

        CronExpression expression = new CronExpression(cronExpression);

        LocalDateTime now = LocalDateTime.now();
        System.out.printf("Now: %s%n", now);

        nextFireTimes(expression, now)
                .limit(4)
                .forEach((LocalDateTime it) ->
                        System.out.printf("Next: %s%n", it));
    }

    @Test
    public void shouldDefineScheduleForEveryWorkdayAtMiddleOfDay() throws Exception {
        String cronExpression = DisseminationSchedule.EVERY_WORKDAY_AT_MIDDLE_OF_DAY.getCronExpression();
        System.out.printf("Cron: %s%n", cronExpression);

        CronExpression expression = new CronExpression(cronExpression);

        LocalDateTime now = LocalDateTime.now();
        System.out.printf("Now: %s%n", now);

        nextFireTimes(expression, now)
                .limit(4)
                .forEach((LocalDateTime it) ->
                        System.out.printf("Next: %s%n", it));
    }

    private static Stream<LocalDateTime> nextFireTimes(CronExpression expression, LocalDateTime reference) {
        LocalDateTime next = nextFireTime(expression, reference);
        if (next == null) {
            return Stream.empty();
        }

        return Stream.iterate(next, (LocalDateTime it) -> nextFireTime(expression, it));
    }

    private static LocalDateTime nextFireTime(CronExpression expression, LocalDateTime reference) {
        return toLocalDateTime(expression.getNextValidTimeAfter(Timestamp.valueOf(reference)));
    }

    private static LocalDateTime toLocalDateTime(Date date) {
        return new Timestamp(date.getTime()).toLocalDateTime();
    }
}
