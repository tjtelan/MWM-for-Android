package org.metawatch.manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.metawatch.manager.MetaWatchService.Preferences;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapCache {
	
	private static String currentTheme = "";
	private static long themeTimeStamp = 0;
	
	private static HashMap<String, Object> cache = new HashMap<String,Object>();
	
	private static File getThemeFile(Context context, String themeName) {
		return new File(Utils.getExternalFilesDir(context, "Themes"), themeName+".zip");
	}
	
	public static synchronized Bitmap getBitmap(Context context, String path) {
		
		updateCache(context);
		
		if (cache.containsKey(path)) {
			Object obj = cache.get(path);
			return obj instanceof Bitmap ? (Bitmap)obj : null;
		}
		
		Bitmap bitmap = loadBitmapFromAssets(context, path);
		
		if (bitmap!=null) {
			cache.put(path, bitmap);
		}
		return bitmap;
		
	}

	
	public static synchronized Properties getProperties(Context context, String path) {
		
		updateCache(context);
		
		if (cache.containsKey(path)) {
			Object obj = cache.get(path);
			return obj instanceof Properties ? (Properties)obj : new Properties();
		}
		
		Properties properties = loadPropertiesFromAssets(context, path);
		
		if (properties!=null) {
			cache.put(path, properties);
			return properties;
		}
		return new Properties();
	}
	
	public static Bitmap getDefaultThemeBanner(Context context) {
		return getThemeBanner(context, null);
	}
	
	public static Bitmap getThemeBanner(Context context, String themeName) {
		if (themeName==null || themeName.isEmpty()) {
			return loadBitmapFromAssets(context, "theme_banner.png");
		}
		else {
			File themeFile = getThemeFile(context, themeName);
			if (themeFile.exists()) {
				HashMap<String, Object> themeData = readTheme(themeFile);
				if (Preferences.logging) Log.d(MetaWatch.TAG, "Theme "+themeName+" contains "+themeData.size()+" assets");
				if (themeData.containsKey("theme_banner.png")) {
					if (Preferences.logging) Log.d(MetaWatch.TAG, "Theme "+themeName+" has a banner");
					return (Bitmap)themeData.get("theme_banner.png");
				}
				else {
					if (Preferences.logging) Log.d(MetaWatch.TAG, "Theme "+themeName+" has no banner");
				}
			}
		}
		return null;
	}

	private static void updateCache(Context context) {
		File themeFile = getThemeFile(context, Preferences.themeName);
		
		if (Preferences.themeName != currentTheme || (themeFile.lastModified() != themeTimeStamp)) {
			currentTheme = Preferences.themeName;
			cache = new HashMap<String,Object>();
			themeTimeStamp = themeFile.lastModified();
			
			if (themeFile.exists()) {
				cache = readTheme(themeFile);
			}
		}
	}
	
	private static Bitmap loadBitmapFromAssets(Context context, String path) {
		
		try {
			InputStream inputStream = context.getAssets().open(path);
	        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
	        inputStream.close();
	        return bitmap;
		} catch (IOException e) {
			return null;
		}
	}
	
	private static Properties loadPropertiesFromAssets(Context context, String path) {
		
		try {
			InputStream inputStream = context.getAssets().open(path);
	        Properties properties = new Properties();
    		properties.loadFromXML(inputStream);       
	        inputStream.close();
	        return properties;
		} catch (IOException e) {
			return null;
		}
	}
	
	
	private static HashMap<String, Object> readTheme(File themeFile) {
		
		HashMap<String, Object> newCache = new HashMap<String,Object>();
		
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
            	int offset = 0;
            	int read=0;
            	do {
            		read = zis.read(buffer, offset, size-offset);
            		offset += read;
            	} while(read>0);
            	
            	if( entryName.toLowerCase().endsWith(".bmp") || entryName.toLowerCase().endsWith(".png")) {	            	
	            	Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, size);
	            	if (bitmap !=null) {
	            		//if (Preferences.logging) Log.d(MetaWatch.TAG, "Loaded "+ze.getName());
	            		newCache.put(entryName, bitmap);
	            	} else {
	            		if (Preferences.logging) Log.d(MetaWatch.TAG, "Failed to load "+ze.getName());
	            	}           	
            	}
            	else if( entryName.toLowerCase().endsWith(".xml")) {
            		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            		Properties properties = new Properties();
            		properties.loadFromXML(byteArrayInputStream);
            		
            		newCache.put(entryName, properties);
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
		
		return newCache;
	   
	}
	
}
