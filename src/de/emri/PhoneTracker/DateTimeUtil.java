package de.emri.PhoneTracker;

import java.util.Calendar;

/**
 * Projekt: PhoneTracker
 * Package: de.emri.PhoneTracker
 * Autor: Markus Embacher
 * Date: 21.02.13
 * Time: 16:19
 */
public class DateTimeUtil {


  public static String getCurrentDateTimeAsString(){
    StringBuilder date=new StringBuilder();
    Calendar c=Calendar.getInstance();
    date.append(DateTimeUtil.getTwoDigitString(c.get(Calendar.DAY_OF_MONTH)));
    date.append(".").append(DateTimeUtil.getTwoDigitString(c.get(Calendar.MONTH)));
    date.append(".").append(c.get(Calendar.YEAR));
    date.append(" ").append(DateTimeUtil.getTwoDigitString(c.get(Calendar.HOUR_OF_DAY)));
    date.append(":").append(DateTimeUtil.getTwoDigitString(c.get(Calendar.MINUTE)));
    date.append(":").append(DateTimeUtil.getTwoDigitString(c.get(Calendar.SECOND)));
    return date.toString();
  }


  private static String getTwoDigitString(int digit){
    String str=String.valueOf(digit);
    if(str.length()==1) {
      str = "0"+str;
    }
    return str;
  }

}
