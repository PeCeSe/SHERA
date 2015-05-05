package no.gruppe2.shera.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

import no.gruppe2.shera.R;

/*
This class contains a custom ImageAdapter that shows the incoming images in a grid-view.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Bitmap> photoList;
    private boolean fromCreate;

    public ImageAdapter(Context c, ArrayList<Bitmap> a, boolean b) {
        mContext = c;
        photoList = a;
        fromCreate = b;
    }

    public int getCount() {
        return photoList.size();
    }

    public Object getItem(int position) {
        return photoList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            if (position == 0 && !fromCreate)
                imageView.setBackgroundResource(R.drawable.host_border_layout);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(15, 40, 15, 40);

        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(photoList.get(position));
        return imageView;
    }
}