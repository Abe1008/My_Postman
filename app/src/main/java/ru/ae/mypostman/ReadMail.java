package ru.ae.mypostman;

/*
    Чтение почты

    https://stackoverflow.com/questions/42478637/how-to-read-emails-programmatically-in-android

    https://www.simplifiedcoding.net/android-email-app-using-javamail-api-in-android-studio/

 */

import android.os.AsyncTask;
import android.util.Log;

import java.util.*;
import java.util.regex.*;

import javax.mail.*;
import javax.mail.internet.MimeUtility;

public class ReadMail extends AsyncTask<Void, Void, Void> {

//    View        view;
    private MainActivity    activity;
    String      keySubj;       // ищем сообщение с такой темой

    Pattern     patternPhoneSMS;
    static String      host = _r.host;  // "imap.yandex.ru";
    static int         port = _r.port;  // 993;
    static String      user = _r.user;  // user login
    static String      pass = _r.pass;  // password

    ReadMail(MainActivity activity)
    {
        String pattern = "0";
        this.activity = activity;
        this.keySubj = activity.getResources().getString(R.string.keySubj);
//        this.patternPhoneSMS = Pattern.compile(
//                activity.getResources().getString(R.string.patternPhoneSMS),
//                Pattern.CASE_INSENSITIVE);
        this.patternPhoneSMS = Pattern.compile(pattern);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int cnt = 0;
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            javax.mail.Session session = javax.mail.Session.getInstance(props, null);
            javax.mail.Store store = session.getStore();
            store.connect(host, port, user, pass);
            javax.mail.Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);  // читать и писать Folder.READ_ONLY
            // получим массив сообщений
            Message[] messages = inbox.getMessages();
            for (Message msg : messages) {
//                Address[] in = msg.getFrom();
//                for (Address address : in) {
//                    System.out.println("FROM:" + address.toString());
//                }
                if(readPost(msg)) {
                    cnt++;
                };
            }
            //
            inbox.close(true);  // false
            store.close();
            Log.i("ReadMail", "отправлено SMS " + cnt);
            //
        } catch (Exception mex) {
            mex.printStackTrace();
            return null;
        }
        return null;
    }

    boolean readPost(Message msg)
    {
        boolean sendSMS = false;
        try {
            String subj = msg.getSubject();
            Date   dats = msg.getSentDate();

            // сравним тему с образцом
            if(compareStrings(keySubj, subj)) {
                Object contentMsg = msg.getContent();
                if (contentMsg instanceof javax.mail.Multipart) {
                    Multipart mp = (Multipart) contentMsg;
                    // прочитаем все вложения
                    int n = mp.getCount();
                    for (int i = 0; i < n; i++) {
                        BodyPart bp = mp.getBodyPart(i); // часть сообщения
                        String fileAttach = bp.getFileName();
                        if (fileAttach == null || fileAttach.length() < 2) {
                            Object content1 = bp.getContent();
                            if (content1 instanceof String) {
                                String txt = (String) content1;
                                // System.out.println("CONTENT: " + txt);
                                // разберем сообщение и найдем там номер и сообщение
                                String[] otv = parseMessage(txt);
                                if(otv != null) {
                                    System.out.println("SENT DATE:" + dats);
                                    System.out.println("SUBJECT:" + subj);
                                    //
                                    SendSMS sms = new SendSMS();
                                    if(sms.send(otv[0], otv[1])) {
                                        //  ///msg.setFlag(Flags.Flag.DELETED, true);
                                        sendSMS = true;
                                    }
                                }
                            }
                        } else {
                            // -----------------------------------------------
                            // имеем дело с частью - вложением файла
                            String attach = MimeUtility.decodeText(fileAttach);  // раскодируем на всякий случай имя файла
//                            System.out.println("Attachment: " + attach);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("?-Error readPost() " + ex.getMessage());
            return false;
        }
        return sendSMS;
    }

    /**
     * разобрать сообщение, выделить номер телефона и текст сообщения
     * PNONE: +79601648776  MESSAGE: Tekst soobjeniya
     * @param txtMessage    сообщение
     * @return  массив [0]-телефон, [1]-текст сообещения
     */
    String[]    parseMessage(String txtMessage)
    {
        String[]    otv = new String[2];
        String pattern = "(PHONE:)\\s+(\\+[0-9]+)\\s+(TEXT:)\\s*(.+)";
        patternPhoneSMS = Pattern.compile(pattern);
        Matcher mat = patternPhoneSMS.matcher(txtMessage);
        if(mat.find()) {
            int n = mat.groupCount();
            if (n >= 4) {
                System.out.println("phone " + mat.group(2));
                System.out.println("text  " + mat.group(4));
                otv[0] = mat.group(2);
                otv[1] = mat.group(4);
                return otv;
            }
            for(int i = 0; i <= n; i++)  System.out.println(i + ": " + mat.group(i));
        }
        return null;
    }

    /**
     * сравнить образец со строкой и выдать результат сравнения
     * @param sample  образец
     * @param str     строка
     * @return  true - совпадает, false - не совпадает
     */
    private boolean compareStrings(String sample, String str)
    {
        return str.contains(sample);
    }


}  // end of class
