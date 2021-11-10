package org.advancedproductivity.gable.framework.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzq
 */
public class DateFormatHolder {
    private static final ConcurrentHashMap<String, SimpleDateFormat> HOLDER = new ConcurrentHashMap<>();

    public static SimpleDateFormat getInstance(String format){
        if (StringUtils.isEmpty(format)) {
            return null;
        }
        SimpleDateFormat format1 = HOLDER.get(format);
        if (format1 == null) {
            format1 = new SimpleDateFormat(format);
            HOLDER.put(format, format1);
            return format1;
        }
        return format1;
    }
}
