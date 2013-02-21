package de.emri.PhoneTracker;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.AnalogClock;

/**
 * Projekt: PhoneTracker
 * Package: de.emri.PhoneTracker
 * Autor: Markus Embacher
 * Date: 21.02.13
 * Time: 12:15
 */
public class SendEmailTask  extends AsyncTask<String, Void, Void> implements ApplicationConstants {

    @Override
    protected Void doInBackground(String... msg) {
      Log.i(DEBUG_TAG, "AsyncSendEmail running in background.");
      if(msg.length>0) {
        sendEmail(msg[0]);
      }
      return null;
    }

    private void sendEmail(String msg){
      Log.i(DEBUG_TAG, "Sending Email...");
      String[] toArr = {"markus@embacher-ries.de", "markus.embacher@asseco.de"};

      Mail m = new Mail("markus.embacher@googlemail.com", "ppqrtuv_41101");
      m.setFrom("markus.embacher@googlemail.com");

//      Mail m = new Mail("m0232cdc", "secret7.");
//      m.setFrom("mail@embacher-ries.de");
      m.setTo(toArr);
      m.setSubject("GPS-Position");
      Log.i(DEBUG_TAG, "Message:" + msg);
      m.setBody(msg);

      try {
        m.send();
      } catch(Exception e) {
        Log.e(DEBUG_TAG, "Could not send email", e);
      }
    }
  }
