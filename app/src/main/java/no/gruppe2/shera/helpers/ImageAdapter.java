package no.gruppe2.shera.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by chris.forberg on 24.03.2015.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private Bitmap[] myPhotos;
    private ArrayList<Bitmap> photoList;
    private Activity activity;
    private static LayoutInflater inflater = null;

    public ImageAdapter(Context c, ArrayList<Bitmap> a) {
        mContext = c;
        //myPhotos =  v;
        photoList = a;
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
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(15, 40, 15, 40);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(photoList.get(position));
        return imageView;
    }
}