package com.ksportalcraft.pleasegod;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Custom extends BaseAdapter {
    private List<Model> modelList;
    private Context context;
    private int layout;

    public Custom(List<Model> modelList, Context context, int layout) {
        this.modelList = modelList;
        this.context = context;
        this.layout = layout;
    }

    // Helper method to check if an image is in the local storage
    private File getLocalImageFile(String imageUrl) {
        File localImageFile = new File(context.getFilesDir(), "image_cache_directory");
        if (!localImageFile.exists()) {
            localImageFile.mkdirs();
        }

        String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        return new File(localImageFile, fileName);
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

    private static class ViewHolder {
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
            File imageFile = getLocalImageFile(model.getImageUrl());

            if (imageFile.exists()) {
                // Load the image from local storage
                Picasso.get().load(imageFile).into(viewHolder.imageView);
            } else {
                // Image not found in local storage, download and save it
                Picasso.get()
                        .load(model.getImageUrl())
                        .resize(500, 500) // Adjust dimensions as needed
                        .centerInside()
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(android.graphics.Bitmap bitmap, Picasso.LoadedFrom from) {
                                // Save the image to local storage
                                File localImageFile = getLocalImageFile(model.getImageUrl());
                                try {
                                    FileOutputStream outputStream = new FileOutputStream(localImageFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                    outputStream.flush();
                                    outputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // Set the loaded image to the ImageView
                                viewHolder.imageView.setImageBitmap(bitmap);
                            }

                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                // Handle failure to load the image
                            }

                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                // Prepare for image loading
                            }
                        });
            }
        } else {
            viewHolder.imageView.setImageDrawable(null);
        }

        return convertView;
    }
}
