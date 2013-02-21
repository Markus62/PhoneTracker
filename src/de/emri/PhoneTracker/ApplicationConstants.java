package de.emri.PhoneTracker;

/**
 * Projekt: PhoneTracker
 * Package: de.emri.PhoneTracker
 * Autor: Markus Embacher
 * Date: 19.02.13
 * Time: 13:15
 */
public interface ApplicationConstants {
  static final String DEBUG_TAG = "PhoneTracker.SMSBroadcastReceiver";
  static final String PREFS_NAME = "PhoneTrackerData";
  static final String SEND_GPS_POSITION = "send_gps_position";
  static final String PREF_CHECK_BATTERY = "check_battery";
  static final String PREF_SEND_DATA_SMS ="send_data_sms";
  static final String PREF_SEND_TEXT_SMS ="send_text_sms";
  static final String PREF_SEND_EMAIL ="send_email";
  static final String MSG_REQUEST="Gps";
  static final String ACTION_SEND_EMAIL="de.emri.intent.action.SEND_EMAIL";

}
