package comp5216.sydney.edu.au.assignment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.POWER_SERVICE;

/**
 * Created by JIANG on 2016/10/13.
 */

public class AlertTime extends BroadcastReceiver {

    private static final String TAG = "AlertTime";

    private ArrayList<Note> arrayOfNotes;

    private Calendar noteAlertTime;
    private Calendar timeOfCreation;

    private int alertTimeNotifyID = 694;

    private NotificationManager notificationManager;
    private PendingIntent pIntent;

    private int alertTimeCompareResult = 1;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Alert time class access success by 5216 AS");

        arrayOfNotes = new ArrayList<Note>();
        readNotesFromDatabase();

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        //Date currentTime = new Date();
        timeOfCreation = Calendar.getInstance();
        noteAlertTime = Calendar.getInstance();
        timeOfCreation.set(Calendar.SECOND, 0);
        timeOfCreation.set(Calendar.MILLISECOND, 0);

        Iterator<Note> itr = arrayOfNotes.iterator();
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), notifyIntent, 0);
        //alertTimeNotifyID = 694;

        while (itr.hasNext()) {
            Note note = itr.next();
            try{
                //noteAlertTime = f.parse(note.getAlertTime());
                noteAlertTime.setTime(f.parse(note.getAlertTime()));
                Log.d(TAG, "Alert time format parse success by 5216 AS");
                Log.d(TAG, "The note alert time is: " + f.format(noteAlertTime.getTime()));
                Log.d(TAG, "Current time is: " + f.format(timeOfCreation.getTime()));
            }catch(Exception e){
                Log.d(TAG, "Alert time format parse fail by 5216 AS");
            }
            alertTimeCompareResult = noteAlertTime.compareTo(timeOfCreation);
            Log.d(TAG, "" + alertTimeCompareResult);

            if(alertTimeCompareResult == 0){

                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mytag");
                mWakeLock.acquire();

                String notifyTitle = note.getTitle();
                String notifyMessage = note.getMessage();
                Notification n  = new Notification.Builder(context)
                        .setContentTitle(notifyTitle)
                        .setContentText(notifyMessage)
                        .setSmallIcon(R.drawable.notificationicon)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(Notification.PRIORITY_MAX)
                        .build();
                //notificationManager.notify(alertTimeNotifyID, n);
                notificationManager.notify((int)System.currentTimeMillis(), n);
                alertTimeNotifyID = alertTimeNotifyID + 1;

                Log.d(TAG, "Alert time notify success by 5216 AS");

            }else{
                Log.d(TAG, "Alert time not detected by 5216 AS");
            }
        }
        if(alertTimeNotifyID > 1000){
            alertTimeNotifyID = 694;
        }
    }

    //read saved notes from the database
    private void readNotesFromDatabase() {
        List<Note> notesFromORM = Note.listAll(Note.class);
        ArrayList<Note> arrayOfNotes2 = new ArrayList<Note>();
        //arrayOfNotes = new ArrayList<Note>();
        arrayOfNotes.clear();
        if (notesFromORM != null & notesFromORM.size() > 0) {
            for (Note note : notesFromORM) {
                arrayOfNotes2.add(note);
            }
        }
        arrayOfNotes.addAll(arrayOfNotes2);
    }


}
