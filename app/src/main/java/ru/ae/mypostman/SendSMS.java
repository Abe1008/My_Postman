package ru.ae.mypostman;

/*
   Отправка SMS

   https://technotalkative.com/android-sending-sms-from-android-application/

 */

import android.telephony.SmsManager;
import android.util.Log;

public class SendSMS {

//    View    view;
    MainActivity    activity;

    SendSMS()
    {

    }

    SendSMS(MainActivity activity)
    {
        this.activity = activity;
    }

    boolean     send(String phone, String text)
    {
        String outtxt = text.substring(0, Math.min(80, text.length()));
        //System.out.println("Send SMS - PHONE: " + phone + "   TEXT: " + outtxt);
        //

        Log.i("Send SMS ", phone);
//        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
//
//        smsIntent.setData(Uri.parse("smsto:"));
//        smsIntent.setType("vnd.android-dir/mms-sms");
//        smsIntent.putExtra("address"  , new String (phone));
//        smsIntent.putExtra("sms_body"  , outtxt);
        try {
            SmsManager sm = SmsManager.getDefault();
            String number = phone;
            String msg = outtxt;
            sm.sendTextMessage(number, null, msg, null, null);
            return true;
        } catch (Exception ex) {
            //Toast.makeText(activity,"SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
            System.out.println("SMS faild: " +  ex.getMessage());
        }
        return false;
    }


} // end of class
