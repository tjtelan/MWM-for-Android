package org.metawatch.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.metawatch.manager.MetaWatchService.Preferences;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapCache {
	
	private static String currentTheme = "";
	private static long themeTimeStamp = 0;
	
	private static HashMap<String, Bitmap> cache = new HashMap<String,Bitmap>();
	
	public static Bitmap getBitmap(Context context, String path) {
		
		File themeFile = new File(Utils.getExternalFilesDir(context, "Themes"), Preferences.themeName+".zip");
		
		if (Preferences.themeName != currentTheme || (themeFile.lastModified() != themeTimeStamp)) {
			currentTheme = Preferences.themeName;
			cache = new HashMap<String,Bitmap>();
			themeTimeStamp = themeFile.lastModified();
			
			if (themeFile.exists()) {
				readTheme(themeFile);
			}
		}
		
		if (cache.containsKey(path))
			return cache.get(path);
		
		Bitmap bitmap = loadBitmapFromAssets(context, path);
		
		if (bitmap!=null) {
			cache.put(path, bitmap);
		}
		return bitmap;
		
	}
	
	private static Bitmap loadBitmapFromAssets(Context context, String path) {
		
		try {
			InputStream inputStream = context.getAssets().open(path);
	        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
	        inputStream.close();
	        //if (Preferences.logging) Log.d(MetaWatch.TAG, "ok");
	        return bitmap;
		} catch (IOException e) {
			//if (Preferences.logging) Log.d(MetaWatch.TAG, e.toString());
			return null;
		}
	}
	
	private static synchronized void readTheme(File themeFile) {
		
		FileInputStream fis = null;
		ZipInputStream zis = null;
		
		try {
		
		    fis = new FileInputStream(themeFile);
			zis = new ZipInputStream(fis);
					
			ZipEntry ze = zis.getNextEntry();
            while (ze != null) 
            { 
                String entryName = ze.getName();
                
            	final int size = (int) ze.getSize();
            	
            	// Need to copy into a buffer rather than decoding directly from zis
            	// as BitmapFactory seems unable to read a .bmp file from a
            	// ZipInputStream :-\
            	byte[] buffer = new byte[size];
            	zis.read(buffer);
            	Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, size);
            	if (bitmap !=null) {
            		cache.put(entryName, bitmap);
            	}

                zis.closeEntry();
                ze = zis.getNextEntry();

            }
			
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		finally {
			try {
				if (zis!=null)
					zis.close();
				if (fis!=null)
					fis.close();
			} catch (IOException e) {
			}
		}
	   
	}
	
}
