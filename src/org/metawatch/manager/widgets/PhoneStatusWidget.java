package org.metawatch.manager.widgets;

import java.util.ArrayList;
import java.util.Map;

import org.metawatch.manager.FontCache;
import org.metawatch.manager.Monitors;
import org.metawatch.manager.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.text.TextPaint;

public class PhoneStatusWidget implements InternalWidget {

	public final static String id_0 = "phoneStatus_24_32";
	final static String desc_0 = "Phone Battery Status (24x32)";
	
	public final static String id_1 = "phoneStatus_19_16";
	final static String desc_1 = "Phone Battery Status (19x16)";
	
	private Context context;
	private TextPaint paintSmall;
	private TextPaint paintSmallNumerals;
		
	public void init(Context context, ArrayList<CharSequence> widgetIds) {
		this.context = context;
		
		paintSmall = new TextPaint();
		paintSmall.setColor(Color.BLACK);
		paintSmall.setTextSize(FontCache.instance(context).Small.size);
		paintSmall.setTypeface(FontCache.instance(context).Small.face);
		paintSmall.setTextAlign(Align.CENTER);
		
		paintSmallNumerals = new TextPaint();
		paintSmallNumerals.setColor(Color.BLACK);
		paintSmallNumerals.setTextSize(FontCache.instance(context).SmallNumerals.size);
		paintSmallNumerals.setTypeface(FontCache.instance(context).SmallNumerals.face);
		paintSmallNumerals.setTextAlign(Align.CENTER);

	}

	public void shutdown() {
		paintSmall = null;
	}

	public void refresh(ArrayList<CharSequence> widgetIds) {
	}

	public void get(ArrayList<CharSequence> widgetIds, Map<String,WidgetData> result) {

		if(widgetIds == null || widgetIds.contains(id_0)) {		
			result.put(id_0, GenWidget(id_0));
		}
		
		if(widgetIds == null || widgetIds.contains(id_1)) {		
			result.put(id_1, GenWidget(id_1));
		}
	}
	
	private InternalWidget.WidgetData GenWidget(String widget_id) {
		InternalWidget.WidgetData widget = new InternalWidget.WidgetData();
		
		String iconFile="";
		if( widget_id == id_0 ) {
			widget.id = id_0;
			widget.description = desc_0;
			widget.width = 24;
			widget.height = 32;
			iconFile = "idle_phone_status.bmp";
		}
		else if( widget_id == id_1 ) {
			widget.id = id_1;
			widget.description = desc_1;
			widget.width = 19;
			widget.height = 16; 
			iconFile = "idle_phone_status_10.bmp";
		}
		
		Bitmap icon = Utils.loadBitmapFromAssets(context, iconFile);

		int level = Monitors.BatteryData.level;
		String count = level==-1 ? "-" : level+"%";

		widget.priority = level==-1 ? 0 : 1;		
		widget.bitmap = Bitmap.createBitmap(widget.width, widget.height, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(widget.bitmap);
		canvas.drawColor(Color.WHITE);
		
		if (widget_id == id_0 ) {
			canvas.drawBitmap(icon, 0, 3, null);
			canvas.drawText(count, 12, 30,  paintSmall);
		
			if(level>-1)
				canvas.drawRect(13, 8 + ((100-level)/10), 19, 18, paintSmall);
		}
		else if (widget_id == id_1 ) {
			canvas.drawBitmap(icon, 4, 0, null);
			canvas.drawText(count, 10, 15,  paintSmallNumerals);
		
			if(level>-1)
				canvas.drawRect(11, 1 + ((100-level)/12), 14, 8, paintSmall);	
		}
			
		
		return widget;
	}
}
	