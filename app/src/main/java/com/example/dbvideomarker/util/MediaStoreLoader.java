package com.example.dbvideomarker.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.dbvideomarker.database.entitiy.Media;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MediaStoreLoader {

    private static int id;
    private int MS_ONE_HOUR = 3600000;
    private static ArrayList<Integer> mediaIdList = new ArrayList<>();

    public static List<Media> getContent(Context context) {
        List<Media> mediaList = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();
        MediaStoreLoader loader = new MediaStoreLoader();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String projections[] = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATA
        };
        Cursor c = resolver.query(uri, projections, null, null, null);

        if (c != null) {
            while (c.moveToNext()) {
                int index = c.getColumnIndex(projections[0]);
                id = c.getInt(index);

                index = c.getColumnIndex(projections[1]);
                String name = c.getString(index);

                index = c.getColumnIndex(projections[2]);
                String millisDur = c.getString(index);
                long millis = Long.parseLong(millisDur);

                index = c.getColumnIndex(projections[3]);
                String bytesize = c.getString(index);
                int size = Integer.parseInt(bytesize);

                index = c.getColumnIndex(projections[4]);
                String mime = c.getString(index);

                index = c.getColumnIndex(projections[5]);
                String added = c.getString(index);

                index = c.getColumnIndex(projections[6]);
                String path = c.getString(index);

                Media data = new Media();

                data.setResId(id);
                data.setName(name);

                data.setDur(millis);

                String changedSize = loader.getReadableFileSize(size);
                data.setSize(changedSize);

                data.setMime(mime);
                data.setAdded(added);

                String ContentUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id)).toString();
                data.setContentUri(ContentUri);

                data.setPath(path);

                mediaList.add(data);
            }
        }
        c.close();
        return mediaList;
    }

    public String getReadableDuration(long millis) {
        String dur = null;
        if(millis > MS_ONE_HOUR) {
            dur = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        } else {
            dur = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        }
        return dur;
    }

    public String getReadableFileSize(int size) {
        final int BYTES_IN_KILOBYTES = 1024;
        final DecimalFormat dec = new DecimalFormat("###.#");
        final String KILOBYTES = "KB";
        final String MEGABYTES = "MB";
        final String GIGABYTES = "GB";
        float fileSize = 0;
        String suffix = KILOBYTES;

        if (size > BYTES_IN_KILOBYTES) {
            fileSize = size / BYTES_IN_KILOBYTES;
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES;
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES;
                    suffix = GIGABYTES;
                } else {
                    suffix = MEGABYTES;
                }
            }
        }
        return String.valueOf(dec.format(fileSize) + suffix);
    }

    public Bitmap getThumbnail(String path, long where) {

        Bitmap bitmap;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        retriever.setDataSource(path);

        bitmap = retriever.getFrameAtTime(where, MediaMetadataRetriever.OPTION_NEXT_SYNC);
        Log.d("Thumb", "thmbnail picked at" + where);
        retriever.release();
        return bitmap;
    }

    //TODO: 30이상에서 문제가 발생할 수 있음
    public void deleteFile(Context context, int id) {
        String mSelection = MediaStore.Video.Media._ID + "=?";
        String[] mSelectionsArgs = new String[] {String.valueOf(id)};
        Uri contentUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(contentUri, mSelection, mSelectionsArgs);
    }

    //TODO: 30이상에서 문제가 발생할 수 있음
    public void updateFile(Context context, int id, String text) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, text);
        Uri contentUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.update(contentUri, values, null, null);
    }
}
