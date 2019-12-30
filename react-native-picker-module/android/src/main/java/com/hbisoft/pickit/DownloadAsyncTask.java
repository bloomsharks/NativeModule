package com.hbisoft.pickit;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

class DownloadAsyncTask extends AsyncTask<Uri, Integer, String> {
    private Uri mUri;
    private CallBackTask callback;
    private WeakReference<Context> mContext;
    private String pathPlusName;
    private File folder;
    private String filename;
    private Cursor returnCursor;
    private InputStream is = null;
    private String extension;
    private String errorReason = "";
    private String originalFileName = "";
    int size = -1;

    DownloadAsyncTask(Uri uri, Context context, CallBackTask callback, String filename) {
        this.mUri = uri;
        mContext = new WeakReference<>(context);
        this.callback = callback;
        this.filename = filename;
    }

    @Override
    protected void onPreExecute() {
        callback.PickiTonPreExecute();
        Context context = mContext.get();
        if (context != null) {
            folder = context.getCacheDir();
            returnCursor = context.getContentResolver().query(mUri, null, null, null, null);
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(mUri));
            try {
                is = context.getContentResolver().openInputStream(mUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int post = values[0];
        callback.PickiTonProgressUpdate(post);
    }

    @Override
    protected String doInBackground(Uri... params) {
        File file = null;


        try {
            try {
                if (returnCursor != null && returnCursor.moveToFirst()){
                    if (mUri.getScheme() != null)
                    if (mUri.getScheme().equals("content")) {
                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        size = (int) returnCursor.getLong(sizeIndex);
                        originalFileName = returnCursor.getString(nameIndex);
                    }else if (mUri.getScheme().equals("file")) {
                        File ff = new File(mUri.getPath());
                        size = (int) ff.length();
                    }
                }
            }
            finally {
                if (returnCursor != null)
                returnCursor.close();
            }

            if (extension == null){
                pathPlusName = folder + "/" + filename;
                file = new File(folder + "/" + filename);
            }else {
                pathPlusName = folder + "/" + filename + "." + extension;
                file = new File(folder + "/" + filename + "." + extension);
            }

            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(file);


            byte[] data = new byte[1024];
            long total = 0;
            int count;
            while ((count = bis.read(data)) != -1) {
                if (!isCancelled()) {
                    total += count;
                    if (size != -1) {
                        publishProgress((int) ((total * 100) / size));
                    }
                    fos.write(data, 0, count);
                }
            }
            fos.flush();
            fos.close();

        } catch (IOException e) {
            Log.e("Pickit IOException = ", e.getMessage());
            errorReason = e.getMessage();
        }

        return file.getAbsolutePath();

    }

    protected void onPostExecute(String result) {
        if(result == null){
            callback.PickiTonPostExecute(pathPlusName, true, false, originalFileName, size, errorReason);
        }else {
            callback.PickiTonPostExecute(pathPlusName, true, true, originalFileName, size, "");
        }
    }
}