/*
 *  Copyright (c) 2021 AdvancedProductivity
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
