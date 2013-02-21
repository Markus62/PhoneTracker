/*
Copyright (c) $today.year Markus Embacher.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package de.emri.PhoneTracker;

import android.app.Activity;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import javax.xml.transform.Result;
import java.net.URL;

public class MainActivity extends Activity implements ApplicationConstants{
  private static Context context;
  private TextView tv;
  private ToggleButton toggleButtonIncomingCalls, toggleButtonCheckBattery;
  private static String outgoingNumber=null;
  private static boolean sendGpsPosition =false,
          checkBattery=false, sendDataSms=false, sendTextSms=false, sendEmail=false;
  static final short DATA_SMS_PORT=15555;


  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    restorePreferences();
    setOutgoing();
    setButtonsAndCheckBoxes();
    addListeners();
    context=getApplicationContext();
    Log.i(DEBUG_TAG, "Main Activity running...");
  }

  /**
   * Liest die in den Shared Preferences gespeicherten Daten.
   */
  private void restorePreferences(){
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

    if(settings!=null) {
      outgoingNumber = settings.getString("outgoing_number", "");
      sendGpsPosition = settings.getBoolean(SEND_GPS_POSITION, false);
      checkBattery=settings.getBoolean(PREF_CHECK_BATTERY, false);
      sendDataSms=settings.getBoolean(PREF_SEND_DATA_SMS, false);
      sendTextSms=settings.getBoolean(PREF_SEND_TEXT_SMS, false);
      sendEmail=settings.getBoolean(PREF_SEND_EMAIL, false);
    }
  }


  /**
   * Speichert die Daten aus der UI in den Shared Preferences.
   */
  private void savePreferences() {
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString("outgoing_number", outgoingNumber);
    editor.putBoolean(SEND_GPS_POSITION, sendGpsPosition);
    editor.putBoolean(PREF_CHECK_BATTERY, checkBattery);
    editor.putBoolean(PREF_SEND_DATA_SMS, sendDataSms);
    editor.putBoolean(PREF_SEND_TEXT_SMS, sendTextSms);
    editor.putBoolean(PREF_SEND_EMAIL, sendEmail);
    editor.commit();
  }


  /**
   * Ruft alle Methoden auf, die die UI mit den richtigen Werten versorgt.
   */
  private void refreshUI(){
    setOutgoing();
    setButtonsAndCheckBoxes();
  }

  /**
   * Fuellt das Textfeld mit der Ziel-Telefonnummer.
   */
  private void setOutgoing() {
    TextView tv = (TextView)findViewById(R.id.id_text_targetnumber);
    if(outgoingNumber!=null){
      tv.setText(outgoingNumber);
    } else{
      outgoingNumber = tv.getText().toString();
    }
  }

  /**
   * Setzt den Status des ToggleButtons "SMS and Zielnummer senden".
   */
  private void setButtonsAndCheckBoxes()
  {
    ToggleButton tb = (ToggleButton)findViewById(R.id.tbSendPosition);
    ToggleButton tb1 = (ToggleButton)findViewById(R.id.tbSendAkkuStatus);
    RadioButton rb1 = (RadioButton)findViewById(R.id.rbDataSms);
    RadioButton rb2 = (RadioButton)findViewById(R.id.rbTextSms);
    RadioButton rb3 = (RadioButton)findViewById(R.id.rbEmail);
    tb.setChecked(sendGpsPosition);
    tb1.setChecked(checkBattery);
    rb1.setChecked(sendDataSms);
    rb2.setChecked(sendTextSms);
    rb3.setChecked(sendEmail);
  }

  /**
   * Erzeugt die Listener für die UI-Objekte.
   * Die statische Variable outgoingNumber wird aktualisiert.
   */
  private void addListeners(){
    EditText outgoing = (EditText)findViewById(R.id.id_text_targetnumber);
    outgoing.addTextChangedListener(new TextWatcher(){
      public void afterTextChanged(Editable s) {
        outgoingNumber = s.toString();
        savePreferences();
      }
      public void beforeTextChanged(CharSequence s, int start, int count, int after){}
      public void onTextChanged(CharSequence s, int start, int before, int count){}
    });

    ToggleButton tb = (ToggleButton)findViewById(R.id.tbSendPosition);
    tb.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendGpsPosition =((ToggleButton)view).isChecked();
        savePreferences();
      }
    });

    ToggleButton tb1 = (ToggleButton)findViewById(R.id.tbSendAkkuStatus);
    tb1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        checkBattery =((ToggleButton)view).isChecked();
        savePreferences();
      }
    });

    Button b = (Button)findViewById(R.id.request);
    b.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(DEBUG_TAG, "Request GPS-Position from: "+outgoingNumber);
        sendDataSMS(outgoingNumber, MSG_REQUEST);
      }
    });

    RadioButton rb1=(RadioButton)findViewById(R.id.rbDataSms);
    rb1.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendDataSms = ((RadioButton) v).isChecked();
        if(sendDataSms) {
          sendTextSms = false;
          sendEmail=false;
        }
        savePreferences();
      }
    });

    RadioButton rb2=(RadioButton)findViewById(R.id.rbTextSms);
    rb2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendTextSms=((RadioButton)v).isChecked();
        if(sendTextSms) {
          sendDataSms = false;
          sendEmail=false;
        }
        savePreferences();
      }
    });

    RadioButton rb3=(RadioButton)findViewById(R.id.rbEmail);
    rb3.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        sendEmail=((RadioButton)v).isChecked();
        if(sendEmail) {
          sendTextSms = false;
          sendDataSms=false;
        }
        savePreferences();
      }
    });
  }

  private  void sendDataSMS(String number, String smsText) {
    SmsManager smsManager=SmsManager.getDefault();

    smsManager.sendDataMessage(outgoingNumber, null, DATA_SMS_PORT, smsText.getBytes(), null, null);

// TEST:
// new SMSBroadCastReceiver().processDataSMS(context, "+491728996856", "Request GPS-Position");

// TEST:
// sendEmail("Test-Message");

  }


  /**
   * Methode zum Testen des Emailversands.
   * @param msg Body der Msg
   */
  public void sendEmail(String msg){
    new SendEmailTask().execute(new String[]{msg});
  }





}
