package ru.aacidov.distalkpro;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by aacidov on 25.10.16.
 */
public class FileStorage {
    private File root;
    private File currentDirectory;
    private Context context;
    private String[] titles;
    private String[] currentTexts;
    private String[] files;
    private static FileStorage instance;

    public FileStorage(){
        context = MainActivity.context;
        titles= context.getResources().getStringArray(R.array.pictitles);


        root = new File(Environment.getExternalStorageDirectory(), "DisTalk/");
        currentDirectory=root;
        if (!root.exists()) {
            createDefaultDirectory();
        }
    }

    public static FileStorage getInstance() {
        if (instance==null){
            instance=new FileStorage();
        }
        return instance;
    }

    private void createDefaultDirectory() {
        boolean success = root.mkdirs();

        if (!success){
            return;
        }
        AssetManager am = context.getAssets();

        try {

            String files[] = new String[12];
            for (int i = 0; i < 12; i++) {
                files[i] = i+".jpg";
            }
            int i = 0;
            for (String filename : files){
                InputStream in = am.open(filename);
                String extension = getExtension(filename);
                String newFileName = root + "/" + titles[i]+"."+extension;
                OutputStream out = new FileOutputStream(newFileName);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
                i++;
            }
        } catch (IOException e) {
            e.fillInStackTrace();
        }
    }

    private String getExtension(String filename) {
        String filenameArray[] = filename.split("\\.");
        return filenameArray[filenameArray.length-1];
    }


    public ImageItem[] getImages() {
        files = currentDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.contains(".");
            }
        });
        if (files==null) return new ImageItem[]{};
        currentTexts = new String[files.length];
        ImageItem[] images = new ImageItem[files.length];
        for (int i =0 ; i<files.length; i++) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentDirectory + "/" + files[i], options);

            String title = files[i].substring(0, files[i].lastIndexOf("."));
            images[i]  = new ImageItem(bitmap, title);
            currentTexts[i] = title;
        }
        return images;
    }

    public String getText(int id) {
        return currentTexts[id];
    }

    public String[] getDirectories (){

        return root.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return !s.contains(".");
            }
        });

    }
    public void setCurrentDirectory (String directory){
        currentDirectory = new File(root, directory);
    }

    public void copyFile(InputStream in, String title) throws IOException {
        String newFileName = currentDirectory + "/" + title+".png";
        OutputStream out = new FileOutputStream(newFileName);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;
    }

    public void delete(int id) {
        File file = getFileById(id);
        file.delete();
    }
    public void rename (int id, String newName){
        File file = getFileById(id);
        file.renameTo(new File(currentDirectory, newName+"."+getExtension(file.getAbsolutePath())));
    }

    private File getFileById(int id) {
        return  new File(currentDirectory, files[id]);
    }

    public void createDirectory(String title) {
        File directory = new File(root, title+"/");
        directory.mkdirs();
    }
}
