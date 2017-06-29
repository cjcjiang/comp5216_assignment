package comp5216.sydney.edu.au.assignment;

/**
 * Created by yuhaocheng on 9/10/16.
 */

public class NoteDistanceCompare{
    private Note note;
    private double distance;

    public NoteDistanceCompare(Note note, double distance){
        this.note = note;
        this.distance = distance;
    }

    public NoteDistanceCompare(Note note, double currentLatitude, double currentLongitude){
        this.note = note;
        this.distance = calDistance(currentLatitude, currentLongitude, note);
    }

    public Note getNote(){
        return this.note;
    }

    public double getDistance(){
        return this.distance;
    }

    //Calculate the the distance from the current location to the location recorded in the Note *in meters*
    private double calDistance(double currentLatitude, double currentLongitude, Note note){
        double x,y,distance;
        double R = 6371229;
        x=(note.getLongitude()-currentLongitude)*Math.PI*R*Math.cos( ((currentLatitude+note.getLatitude())/2) *Math.PI/180)/180;
        y=(note.getLatitude()-currentLatitude)*Math.PI*R/180;
        distance=Math.hypot(x,y);
        return distance;
    }


}
