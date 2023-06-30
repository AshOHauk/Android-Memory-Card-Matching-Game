package com.sa4108.draftca;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;
import java.util.ArrayList;

//Adapts an array of Strings to be displayed in a GridView
public class CustomListAdapter extends ArrayAdapter<String> {
    CustomListAdapter(Context context, ArrayList<String> items){
        super(context, R.layout.grid_item, items);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            //Inflate a new view from its layout file, if one has not been created previously to recycle
            //using LayoutInflater.from() is a convenience method that calls getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }

        //Obtain the reference to the View in the layout
        ImageView imageView = convertView.findViewById(R.id.gridImageItem);

        //Fill the View with data
        Picasso.get().load(getItem(position)).into(imageView);

        //Return the completed view to render on screen
        return convertView;
    }

}