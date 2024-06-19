package com.virtusee.helper;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FileHelper {
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	public static String getImageFolder(){
    	if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) 
    		return null;

	    //File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Selma");
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "Virtusee");

        //Log.e("VS", mediaStorageDir.toString());

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            //Log.e("VS", "failed to create directory");
	            return null;
	        }
	    }
	    
	    return mediaStorageDir.getPath();

	}

	public static File getPrivateImageFolder(Context context){
        File dir = new File(context.getFilesDir(), "images");
        if(!dir.exists()) dir.mkdirs();

        Log.e("path",dir.getAbsolutePath());

        File nomedia = new File(dir,".nomedia");

        try{
            if(!nomedia.exists()) nomedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }

    public static File getPrivateAudioFolder(Context context){
        File dir = new File(context.getFilesDir(), "audio");
        if(!dir.exists()) dir.mkdirs();

        Log.e("path",dir.getAbsolutePath());

        File nomedia = new File(dir,".nomedia");

        try{
            if(!nomedia.exists()) nomedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }

    public static File getPrivateImageFile(Context context, String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "VSIMG_"+ prefix + "_" +timeStamp;
        File storageDir = FileHelper.getPrivateImageFolder(context);

        File mediaFile = null;
        try {
            mediaFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mediaFile;
    }

    public static File getPrivateTtdFile(Context context, String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "VSTTD_"+ prefix + "_" +timeStamp;
        File storageDir = FileHelper.getPrivateImageFolder(context);

        File mediaFile = null;
        try {
            mediaFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mediaFile;
    }

    public static File getPrivateAudioFile(Context context, String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "VSAUDIO_"+ prefix + "_" +timeStamp;
        File storageDir = FileHelper.getPrivateAudioFolder(context);

        File mediaFile = null;
        try {
            mediaFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".3gp",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mediaFile;
    }

    public static void clearAllPhotos(Context context){
        File dir = new File(context.getFilesDir(), "images");
        if(!dir.exists()) return;

        File[] files = dir.listFiles();
        if(files != null){
            for(File file : files) {
                String fname = file.getName();
                if(fname.equals(".nomedia")) continue;
                file.delete();
            }
        }
    }

    public static File getPrivateOutputMediaFile(Context context,String prefix, int type){
        File path = FileHelper.getPrivateImageFolder(context);

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(path,"VSIMG_"+ prefix + "_" +timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(path,"VSVID_"+ prefix + "_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;

    }


	/** Create a File for saving an image or video */
	public static File getOutputMediaFile(String path,String prefix, int type){
    	if (path == "") return null;

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(path + File.separator + "VSIMG_"+ prefix + "_" +timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) { 
	        mediaFile = new File(path + File.separator + "VSVID_"+ prefix + "_" + timeStamp + ".mp4");
	    } else {
	        return null;
	    }
	    
	    return mediaFile;

	}

    public static String implode(String[] str,String delim){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length; i++) {
            sb.append(str[i]);
            if (i != str.length - 1) {
                sb.append(delim);
            }
        }
        String joined = sb.toString();
        return joined;
    }

    public static String convertStreamToString(FileInputStream is) throws IOException {
        // http://www.java2s.com/Code/Java/File-Input-Output/ConvertInputStreamtoString.htm
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        Boolean firstLine = true;
        while ((line = reader.readLine()) != null) {
            if(firstLine){
                sb.append(line);
                firstLine = false;
            } else {
                sb.append("\n").append(line);
            }
        }
        reader.close();
        return sb.toString();
    }



    public static void setLog(Context context, String msg){
        String filename = "virtusee.err.log";
        FileOutputStream outputStream;

        Calendar nowcal = Calendar.getInstance();
        SimpleDateFormat nowformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String errmsg = nowformat.format(nowcal.getTime()) + "\n" + msg + "\n";

        errmsg += "----------------------------------------\n\n";

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(errmsg.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void clearLog(Context context){
        String filename = "virtusee.err.log";
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(null);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
