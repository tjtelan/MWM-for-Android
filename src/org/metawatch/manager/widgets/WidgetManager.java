package org.metawatch.manager.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metawatch.manager.Idle;
import org.metawatch.manager.MetaWatch;
import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.widgets.WidgetRow;
import org.metawatch.manager.widgets.InternalWidget.WidgetData;
import org.metawatch.manager.widgets.GmailWidget;
import org.metawatch.manager.widgets.K9Widget;
import org.metawatch.manager.widgets.MissedCallsWidget;
import org.metawatch.manager.widgets.SmsWidget;
//import org.metawatch.manager.widgets.TestWidget;
import org.metawatch.manager.widgets.WeatherWidget;
import org.metawatch.manager.widgets.CalendarWidget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

public class WidgetManager {
	static List<InternalWidget> widgets = new ArrayList<InternalWidget>();
	static Map<String,WidgetData> dataCache;
	static Object lock = new Object();
	
	public static void initWidgets(Context context, ArrayList<CharSequence> widgetsDesired) {
		
		if(widgets.size()==0) {
			widgets.add(new MissedCallsWidget());
			widgets.add(new SmsWidget());
			widgets.add(new K9Widget());
			widgets.add(new GmailWidget());
			widgets.add(new WeatherWidget());
			widgets.add(new CalendarWidget());
			widgets.add(new PhoneStatusWidget());
			widgets.add(new PictureWidget());
			//widgets.add(new TestWidget());
		}
		
		for(InternalWidget widget : widgets) {
			widget.init(context, widgetsDesired);
		}
	}
	
	public static Map<String,WidgetData> refreshWidgets(Context context, ArrayList<CharSequence> widgetsDesired) {
		synchronized (lock) {
					
			if(dataCache==null)
				dataCache = new HashMap<String,WidgetData>();
			
			for(InternalWidget widget : widgets) {
				widget.refresh(widgetsDesired);
				widget.get(widgetsDesired, dataCache);
			}
			
			Intent intent = new Intent("org.metawatch.manager.REFRESH_WIDGET_REQUEST");
			Bundle b = new Bundle();
			if(widgetsDesired==null)
				b.putBoolean("org.metawatch.manager.get_previews", true);
			else {
				String[] temp = widgetsDesired.toArray(new String[widgetsDesired.size()]);
				b.putStringArray("org.metawatch.manager.widgets_desired", temp);
			}
		
			intent.putExtras(b);
			
			context.sendBroadcast(intent);
			
			return dataCache;
		
		}
	}
	
	public static Map<String,WidgetData> getCachedWidgets(Context context, ArrayList<CharSequence> widgetsDesired) {
		if(dataCache==null)
			return refreshWidgets(context, widgetsDesired);
		
		return dataCache;
	}	
	
	public static List<WidgetRow> getDesiredWidgetsFromPrefs() {
		
		String[] rows = Preferences.widgets.split("\\|");
		
		List<WidgetRow> result = new ArrayList<WidgetRow>();
		
		for(String line : rows) {
			WidgetRow row = new WidgetRow();
			String[] widgets = line.split(",");
			for(String widget : widgets) {
				row.add(widget);
			}
			result.add(row);
		}
		
		return result;
	}
	
	public static void getFromIntent(Context context, Intent intent) {
		
		Log.d(MetaWatch.TAG, "WidgetManager.getFromIntent()");
		
		synchronized (lock) {
						
			WidgetData widget = new WidgetData();
		
			Bundle b = intent.getExtras();
			
			if (!b.containsKey("id") ||
				!b.containsKey("desc") ||
				!b.containsKey("width") ||
				!b.containsKey("height") ||
				!b.containsKey("priority") ||
				!b.containsKey("array") ) {
				if (Preferences.logging) Log.d(MetaWatch.TAG, "Malformed WIDGET_UPDATE intent");
				return;
			}
			
			widget.id = b.getString("id");
			widget.description = b.getString("desc");
			widget.width = b.getInt("width");
			widget.height = b.getInt("height");
			widget.priority = b.getInt("priority");
			
			int[] buffer = b.getIntArray("array");
			
			widget.bitmap = Bitmap.createBitmap(widget.width, widget.height, Bitmap.Config.RGB_565);
			widget.bitmap.setPixels(buffer, 0, widget.width, 0, 0, widget.width, widget.height);
	
			if(dataCache==null)
				dataCache = new HashMap<String,WidgetData>();
			
			dataCache.put(widget.id, widget);
			Log.d(MetaWatch.TAG, "Received widget "+widget.id+ "successfully");
			
			Idle.updateIdle(context);
			
		}
	}
}
