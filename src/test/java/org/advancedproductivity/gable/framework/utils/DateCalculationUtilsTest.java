package org.advancedproductivity.gable.framework.utils;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class DateCalculationUtilsTest {

    @Test
    public void testWeekReset(){
        SimpleDateFormat format = DateFormatHolder.getInstance("yyyy-MM-dd HH:mm:ss");
        Date dateLastTwoWeek = DateCalculationUtils.weekReset(-2, 2);
        Date dateLastOneWeek = DateCalculationUtils.weekReset(-1, 2);
        Date dateCurrentWeek = DateCalculationUtils.weekReset(0, 2);
        Date dateNextOneWeek = DateCalculationUtils.weekReset(1, 2);
        Date dateNextTwoWeek = DateCalculationUtils.weekReset(2, 2);
        System.out.println("Last Two Week: " + format.format(dateLastTwoWeek));
        System.out.println("Last One Week: " + format.format(dateLastOneWeek));
        System.out.println("Current Week: " + format.format(dateCurrentWeek));
        System.out.println("Next One Week: " + format.format(dateNextOneWeek));
        System.out.println("Next Two Week: " + format.format(dateNextTwoWeek));
    }

    @Test
    public void testWeekCalculate(){
        SimpleDateFormat format = DateFormatHolder.getInstance("yyyy-MM-dd HH:mm:ss");
        Date dateLastTwoWeek = DateCalculationUtils.weekCalculate(-2);
        Date dateLastOneWeek = DateCalculationUtils.weekCalculate(-1);
        Date dateCurrentWeek = DateCalculationUtils.weekCalculate(0);
        Date dateNextOneWeek = DateCalculationUtils.weekCalculate(1);
        Date dateNextTwoWeek = DateCalculationUtils.weekCalculate(2);
        System.out.println("Last Two Week: " + format.format(dateLastTwoWeek));
        System.out.println("Last One Week: " + format.format(dateLastOneWeek));
        System.out.println("Current Week: " + format.format(dateCurrentWeek));
        System.out.println("Next One Week: " + format.format(dateNextOneWeek));
        System.out.println("Next Two Week: " + format.format(dateNextTwoWeek));
    }

    @Test
    public void testDayCalculate(){
        SimpleDateFormat format = DateFormatHolder.getInstance("yyyy-MM-dd HH:mm:ss");
        Date dateMinTwoDay = DateCalculationUtils.dayCalculate(-2);
        Date dateMinOneDay = DateCalculationUtils.dayCalculate(-1);
        Date dateCurrentDay = DateCalculationUtils.dayCalculate(0);
        Date dateNextOneDay = DateCalculationUtils.dayCalculate(1);
        Date dateNextTwoDay = DateCalculationUtils.dayCalculate(2);
        System.out.println("Last Two Day: " + format.format(dateMinTwoDay));
        System.out.println("Last One Day: " + format.format(dateMinOneDay));
        System.out.println("Current Day: " + format.format(dateCurrentDay));
        System.out.println("Next One Day: " + format.format(dateNextOneDay));
        System.out.println("Next Two Day: " + format.format(dateNextTwoDay));
    }

    @Test
    public void testMonthCalculate(){
        SimpleDateFormat format = DateFormatHolder.getInstance("yyyy-MM-dd HH:mm:ss");
        Date dateMinTwoMonth = DateCalculationUtils.monthCalculate(-2);
        Date dateMinOneMonth = DateCalculationUtils.monthCalculate(-1);
        Date dateCurrentMonth = DateCalculationUtils.monthCalculate(0);
        Date dateNextOneMonth = DateCalculationUtils.monthCalculate(1);
        Date dateNextTwoMonth = DateCalculationUtils.monthCalculate(2);
        System.out.println("Last Two Month: " + format.format(dateMinTwoMonth));
        System.out.println("Last One Month: " + format.format(dateMinOneMonth));
        System.out.println("Current Month: " + format.format(dateCurrentMonth));
        System.out.println("Next One Month: " + format.format(dateNextOneMonth));
        System.out.println("Next Two Month: " + format.format(dateNextTwoMonth));
    }

}