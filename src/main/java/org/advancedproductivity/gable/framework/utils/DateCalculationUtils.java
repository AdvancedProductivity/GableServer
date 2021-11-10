package org.advancedproductivity.gable.framework.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author zzq
 */
public class DateCalculationUtils {

    /**
     * @param weekOffset week offset.this week is 0,last week is -1,next week is 1
     * @param dayOfWeek  the dayCount
     */
    public static Date weekReset(int weekOffset, int dayOfWeek) {
        if (dayOfWeek > Calendar.SATURDAY || dayOfWeek < Calendar.SUNDAY) {
            dayOfWeek = 1;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.WEEK_OF_MONTH, weekOffset);
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        return calendar.getTime();
    }
    public static Date weekCalculate(int weekOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.WEEK_OF_MONTH, weekOffset);
        return calendar.getTime();
    }

    public static Date dayCalculate(int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, dayOffset);
        return calendar.getTime();
    }

    public static Date monthCalculate(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, monthOffset);
        return calendar.getTime();
    }
}
