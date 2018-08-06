package com.idleroom.web.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 此处对util.Json中的源码进行修改，使其能够解析Date类型数据
 * 修改部分为 1552 行； 1618行
 * 注：从JSONObject生成的Date需要用此工具转换
 * @author Unrestraint
 */
public class DateUtil {
    public static Date transferStringToDate(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("请使用该工具转换的日期格式:",e);
        }
    }
    public static String transferDateToString(Date date){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
