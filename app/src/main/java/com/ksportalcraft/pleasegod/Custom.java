package com.ksportalcraft.pleasegod;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.InputStream;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Custom extends BaseAdapter {
    private List<Model> modelList;
    private Context context;
    private int layout;
    private OkHttpClient customOkHttpClient;
    private String cookie;
    private String userAgent;

    public Custom(List<Model> modelList, Context context, int layout) {
        this.modelList = modelList;
        this.context = context;
        this.layout = layout;
        this.customOkHttpClient = new OkHttpClient();
        this.cookie = "__test=fcc21eac01ffba8302cc093670e6d98c";
        this.userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240";
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
            // Load and display the image using AsyncTask with resizing
            new ImageLoaderTask(viewHolder.imageView).execute(model.getImageUrl());
        } else {
            // Handle cases where imageUrl is not available (e.g., set a placeholder image)
            viewHolder.imageView.setImageDrawable(null);
        }

        return convertView;
    }

    private class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView;

        public ImageLoaderTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            try {
                Request imageRequest = new Request.Builder()
                        .url(imageUrl)
                        .addHeader("Cookie", cookie)
                        .addHeader("User-Agent", userAgent)
                        .build();

                Response response = customOkHttpClient.newCall(imageRequest).execute();
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        InputStream imageStream = responseBody.byteStream();

                        // Decode the image with custom width and height
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4; // Adjust the sample size as needed for your desired size
                        Bitmap resizedBitmap = BitmapFactory.decodeStream(imageStream, null, options);

                        return resizedBitmap;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                // Handle image loading failure
                imageView.setImageDrawable(null);
            }
        }
    }
}
