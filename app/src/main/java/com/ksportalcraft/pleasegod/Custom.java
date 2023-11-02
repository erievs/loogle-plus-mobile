package com.ksportalcraft.pleasegod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksportalcraft.pleasegod.CustomOkHttpClient;
import com.ksportalcraft.pleasegod.Model;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import okhttp3.OkHttpClient;

import java.util.List;

public class Custom extends BaseAdapter {
    private List<Model> modelList;
    private Context context;
    private int layout;

    public Custom(List<Model> modelList, Context context, int layout) {
        this.modelList = modelList;
        this.context = context;
        this.layout = layout;

        // Initialize Picasso in the constructor
        Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(CustomOkHttpClient.getClient()))
                .build();

        // Preload and cache all images in the modelList
        for (Model model : modelList) {
            if (model.getImageUrl() != null) {
                picasso.load(model.getImageUrl())
                        .resize(500, 500) // Adjust dimensions as needed
                        .centerInside()
                        .fetch();
            }
        }
    }

    @Override
    public int getCount() {
        return modelList.size();
    }

    @Override
    public Object getItem(int position) {
        return modelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        ImageView imageView;
        TextView idtxt;
        TextView titletxt;
        TextView bodytxt;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(layout, parent, false);
            viewHolder.imageView = convertView.findViewById(R.id.imageView);
            viewHolder.idtxt = convertView.findViewById(R.id.idtxt);
            viewHolder.titletxt = convertView.findViewById(R.id.titletxt);
            viewHolder.bodytxt = convertView.findViewById(R.id.bodytxt);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Model model = modelList.get(position);

        viewHolder.idtxt.setText(model.getId());
        viewHolder.titletxt.setText(model.getTitle());
        viewHolder.bodytxt.setText(model.getBody());

        if (model.getImageUrl() != null) {
            Picasso picasso = new Picasso.Builder(context)
                    .downloader(new OkHttp3Downloader(CustomOkHttpClient.getClient()))
                    .build();

            // Load the image with caching
            picasso.load(model.getImageUrl())
                    .resize(500, 500) // Adjust dimensions as needed
                    .centerInside()
                    .memoryPolicy(MemoryPolicy.NO_CACHE) // Disable memory cache
                    .networkPolicy(NetworkPolicy.NO_CACHE) // Disable network cache
                    .into(viewHolder.imageView);
        } else {
            viewHolder.imageView.setImageDrawable(null);
        }

        return convertView;
    }
}
