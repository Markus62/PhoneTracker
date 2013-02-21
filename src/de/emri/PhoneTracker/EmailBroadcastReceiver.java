package de.emri.PhoneTracker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Projekt: PhoneTracker
 * Package: de.emri.PhoneTracker
 * Autor: Markus Embacher
 * Date: 21.02.13
 * Time: 11:14
 */
public class EmailBroadcastReceiver extends SMSBroadCastReceiver implements ApplicationConstants{

  /*-----------------------------------------------------------------------
 * Ueberschriebene Receive-Methode
 *-----------------------------------------------------------------------*/
  @Override
  public void onReceive(final Context context, final Intent intent) {
    Log.i(DEBUG_TAG, "entering Email-Receiver");

    if(intent.getAction().equals(ACTION_SEND_EMAIL)){
      sendEmail(intent.getStringExtra("message"));
    }
  }

  public void sendEmail(String msg){
    Log.i(DEBUG_TAG, "Sending Email...");
    String adresse = "markus.embacher@googlemail.com";
    Mail m = new Mail(adresse, "ppqrtuv_41101");
    String[] toArr = {adresse, "markus.embacher@asseco.de"};

    m.setTo(toArr);
    m.setFrom("markus.embacher@googlemail.com");
    m.setSubject("GPS-Position");
    Log.i(DEBUG_TAG, "Message:" + msg);
    m.setBody("testmessage");

    try {
      m.send();
    } catch(Exception e) {
      Log.e(DEBUG_TAG, "Could not send email", e);
    }
  }
}
