package de.emri.PhoneTracker;

import android.content.*;
import android.location.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.*;
import android.util.Log;

import java.util.Calendar;


/**
 * Projekt: PhoneTracker
 * Package: de.emri.PhoneTracker
 * Autor: Markus Embacher
 * Date: 18.02.13
 * Time: 12:49
 *
 * Receiver-Klasse zum Empfang von Daten-SMS
 */
public class SMSBroadCastReceiver extends BroadcastReceiver implements ApplicationConstants {

  static final String DATA_SMS_RECEIVED="android.intent.action.DATA_SMS_RECEIVED";
  static final String SMS_RECEIVED="android.intent.action.SMS_RECEIVED";
  static final short DATA_SMS_PORT=15555;
  private Context context;
  private LocationManager locationManager;
  private static String outgoingNumber=null;
  private static boolean sendGpsPosition =false,
          checkBattery=false, sendDataSms=false, sendTextSms=false, sendEmail=false;
  private String originatingAddress;

  /*-----------------------------------------------------------------------
   * Ueberschriebene Receive-Methode
   *-----------------------------------------------------------------------*/
  @Override
  public void onReceive(final Context context, final Intent intent) {
    Log.i(DEBUG_TAG, "entering SMS-Receiver");
    final int port;
    this.context = context;
    if(intent.getAction().equals(DATA_SMS_RECEIVED)){
      port=getPort(intent.getDataString());
      Log.i(DEBUG_TAG, "Data SMS received.");
      Log.i(DEBUG_TAG, "Port: "+port);
      if(port == DATA_SMS_PORT) {
        processPdus(context, intent);
      }
    }else{
      Log.i(DEBUG_TAG, "Text-SMS received.");
      processPdus(context, intent);
    }
  }

  /**
   * Ermittelt den Port, an den die SMS gesendet wurde
   * @param uriContent Content
   * @return Portnummer
   */
  private int getPort(String uriContent) {
    final String[] str=uriContent.split(":");
    final String strPort=str[str.length-1];
    return(Integer.parseInt(strPort));
  }


  /**
   * Verarbeitet eine (oder mehrere) Protocol Description Units
   * @param context aktueller Kontext
   * @param intent  Intent
   */
  private void processPdus(final Context context, final Intent intent) {
    final Bundle bundle;
    final Object[] pduList;
    String smsText;

    SmsMessage smsMessage;
    bundle=intent.getExtras();

    Log.i(DEBUG_TAG, "processing pdus");
    if(bundle!=null){
      pduList=(Object[])bundle.get("pdus");
      for(Object pduObject : pduList) {
          smsMessage = SmsMessage.createFromPdu((byte[])pduObject);
        if(smsMessage!=null){
          originatingAddress = smsMessage.getOriginatingAddress();
          Log.i(DEBUG_TAG, "incoming number:"+originatingAddress);
          restorePreferences();
          if(sendGpsPosition) {
            if(intent.getAction().equals(DATA_SMS_RECEIVED)) {
              processDataSMS(smsMessage);
            } else{
              processTextSMS(smsMessage);
            }
          }else{
            Log.i(DEBUG_TAG, "Sending GPS-Position is off!");
          }
        }
      } // end for
    }
  }

  /**
   * Verarbeitet eine Daten-SMS
   * @param smsMessage SMS
   */
  public void processDataSMS(SmsMessage smsMessage) {
    Log.i(DEBUG_TAG, "processing Data-SMS...");
    String msgText = new String(smsMessage.getUserData());
    Log.i(DEBUG_TAG, "Message: "+msgText);
    if(msgText!=null){
      if(msgText.equals(MSG_REQUEST)) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Log.i(DEBUG_TAG, "Location Manager requesting update");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

      }
    }
  };

  /**
   * Verarbeitet eine Text-SMS
   * @param smsMessage SMS
   */
  public void processTextSMS(SmsMessage smsMessage) {
    Log.i(DEBUG_TAG, "processing Text-SMS...");
    String subject=new String(smsMessage.getPseudoSubject());
    Log.i(DEBUG_TAG, "Betreff:"+subject);
    String msgText=new String(smsMessage.getMessageBody());
    Log.i(DEBUG_TAG, "Message: "+msgText);

    if(msgText!=null && msgText.equals(MSG_REQUEST) || subject!=null & subject.equals(MSG_REQUEST)){
        sendDataSms=false;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Log.i(DEBUG_TAG, "Location Manager requesting update");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
  };

  /**
   * Listener, der auf Änderungen im LocationManager reagiert und ggf. SMS/E-Mail versendet.
   */
  private LocationListener locationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      String message;
      Log.i(DEBUG_TAG, "Location changed");
      Log.i(DEBUG_TAG, "Lng: " + location.getLongitude() + "Lat: " + location.getLatitude());

      restorePreferences();
      locationManager.removeUpdates(locationListener);
      SmsManager smsManager=SmsManager.getDefault();
      message = buildMessageText(location);

      if(sendDataSms) {
        Log.i(DEBUG_TAG, "Sending Data SMS.");
        smsManager.sendDataMessage(outgoingNumber, null, DATA_SMS_PORT, message.getBytes(), null, null);
      } else if(sendTextSms){
        Log.i(DEBUG_TAG, "Sending Text SMS.");
        smsManager.sendTextMessage(originatingAddress, null, message, null, null);
      } else{
        Log.i(DEBUG_TAG, "Sending E-Mail");
        sendEmail(message);
      }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      Log.i(DEBUG_TAG, "Status changed");
    }

    @Override
    public void onProviderEnabled(String provider) {
      Log.i(DEBUG_TAG, "Provider enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
      Log.i(DEBUG_TAG, "Provider disabled");
      locationManager.removeUpdates(locationListener);
      SmsManager smsManager=SmsManager.getDefault();
      String message="GPS ist ausgeschaltet.";
      if(sendDataSms) {
        Log.i(DEBUG_TAG, "Sending Data SMS.");
        smsManager.sendDataMessage(outgoingNumber, null, DATA_SMS_PORT, message.getBytes(), null, null);
      } else if(sendTextSms){
        Log.i(DEBUG_TAG, "Sending Text SMS.");
        smsManager.sendTextMessage(originatingAddress, null, message, null, null);
      } else{
        Log.i(DEBUG_TAG, "Sending E-Mail");
        sendEmail(message);
      }
    }
  };


  /**
   * Setzt aus den Information im Location-Objekt den SMS zusammen
   * @param location Aktuelles Positions-Objekt
   * @return SMS-Text als String
   */
  protected static String buildMessageText(Location location) {
    StringBuilder msg=new StringBuilder();

    msg.append(DateTimeUtil.getCurrentDateTimeAsString());
    msg.append("\nPosition:\nhttps://maps.google.de/maps?q=");
    msg.append(location.getLatitude());
    msg.append(",").append(location.getLongitude());
    msg.append("&v=k&z=16\nGenauigkeit: ");
    msg.append(location.getAccuracy()).append("\nGeschwindigkeit: ");
    msg.append(location.getSpeed()).append(" km/h\nHöhe: ");
    msg.append(location.getAltitude()).append(" m");

    return msg.toString();
  }


  public void sendEmail(String msg){
    new SendEmailTask().execute(new String[] {msg});
  }


  private void restorePreferences(){
    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

    if(settings!=null) {
      outgoingNumber = settings.getString("outgoing_number", "");
      sendGpsPosition = settings.getBoolean(SEND_GPS_POSITION, false);
      checkBattery=settings.getBoolean(PREF_CHECK_BATTERY, false);
      sendDataSms=settings.getBoolean(PREF_SEND_DATA_SMS, false);
      sendTextSms=settings.getBoolean(PREF_SEND_TEXT_SMS, false);
      sendEmail=settings.getBoolean(PREF_SEND_EMAIL, false);
      if(sendTextSms){
        Log.i(DEBUG_TAG, "send Text-SMS ist active.");
      }
//      sendDataSms=!sendTextSms&&!sendEmail;
      if(sendDataSms){
        Log.i(DEBUG_TAG, "send Data-SMS ist active.");
      }
      if(sendEmail){
        Log.i(DEBUG_TAG, "send E-Mail ist active.");
      }
    }

  }
}
