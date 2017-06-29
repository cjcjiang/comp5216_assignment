package comp5216.sydney.edu.au.assignment;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by yuhaocheng on 9/10/16.
 */

public class Note extends SugarRecord implements Serializable {

    private static final long serialVersionUID = 666;

    private String title;
    private String time;
    private String message;
    private String imageURI;
    private double latitude;
    private double longitude;

    private String alertTime;


    public Note(){

    }

    public Note(String title, String time, String alertTime, String message, String imageURI, double latitude, double longitude){
        this.title = title;
        this.time = time;
        this.alertTime = alertTime;
        this.message = message;
        this.imageURI = imageURI;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public String getTitle(){
        return this.title;
    }

    public String getTime(){
        return this.time;
    }

    public String getMessage(){
        return this.message;
    }

    public String getImageURI(){
        return this.imageURI;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public String getAlertTime(){
        return this.alertTime;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setTime(String time){
        this.time = time;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setImageURI(String imageURI){
        this.imageURI = imageURI;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public void setAlertTime(String alertTime){
        this.alertTime = alertTime;
    }


}
