package comp5216.sydney.edu.au.assignment;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

/**
 * Created by yuhaocheng on 9/10/16.
 */
public class UsersAdapter extends ArrayAdapter<Note> {
    public UsersAdapter(Context context, ArrayList<Note> notes) {
        super(context, 0, notes);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Note note = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_note, parent, false);
        }
        // Lookup view for data population
        TextView note_messageTextView = (TextView) convertView.findViewById(R.id.note_messageTextView);
        TextView note_timeTextView = (TextView) convertView.findViewById(R.id.note_timeTextView);
        TextView note_titleTextView = (TextView) convertView.findViewById(R.id.note_titleTextView);
        TextView alertTimeTextView = (TextView) convertView.findViewById(R.id.alertTimeTextView);

        //Get the image view
        SimpleDraweeView draweeView = (SimpleDraweeView) convertView.findViewById(R.id.note_image_view);

        // Populate the data into the template view using the data object
        note_messageTextView.setText(note.getMessage());
        note_timeTextView.setText("Last Edting time: "+note.getTime());
        note_titleTextView.setText(note.getTitle());
        alertTimeTextView.setText("Alert Time: " + note.getAlertTime());

        //Show the stored image in the note
        Uri uri = Uri.parse(note.getImageURI());
        draweeView.setImageURI(uri);

        // Return the completed view to render on screen
        return convertView;
    }
}
