package org.metawatch.manager.apps;

import java.util.ArrayList;
import java.util.List;

import org.metawatch.manager.FontCache;
import org.metawatch.manager.Notification;
import org.metawatch.manager.Protocol;
import org.metawatch.manager.Notification.NotificationType;
import org.metawatch.manager.Utils;
import org.metawatch.manager.MetaWatchService.WatchType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelXorXfermode;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class ActionsApp implements InternalApp {

	public interface Action {
		public String getName();
		public void performAction(Context context);
	}
	
	static AppData appData = new AppData() {{
		id = "org.metawatch.manager.apps.ActionsApp";
		name = "Actions";
		
		supportsAnalog = true;
		supportsDigital = true;
	}};
	
	
	public final static byte ACTION_NEXT = 30;
	public final static byte ACTION_PERFORM = 31;
	
	public AppData getInfo() {
		return appData;
	}
	
	List<Action> actions;
	int currentSelection = 0;

	public void activate(int watchType) {
		if (watchType == WatchType.DIGITAL) {
			Protocol.enableButton(1, 1, ACTION_NEXT, 1); // right middle - press
			Protocol.enableButton(2, 1, ACTION_PERFORM, 1); // right middle - press
		}
		else if (watchType == WatchType.ANALOG) {
			Protocol.enableButton(0, 1, ACTION_NEXT, 1); // top - press
			Protocol.enableButton(2, 1, ACTION_PERFORM, 1); // bottom - press			
		}
	}

	public void deactivate(int watchType) {
		if (watchType == WatchType.DIGITAL) {
			Protocol.disableButton(1, 1, 1);
			Protocol.disableButton(2, 1, 1);
		}
		else if (watchType == WatchType.ANALOG) {
			Protocol.disableButton(0, 1, 1); 
			Protocol.disableButton(2, 1, 1); 				
		}
		
	}

	public Bitmap update(final Context context, int watchType) {
		TextPaint paint = new TextPaint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(FontCache.instance(context).Get().size);
		paint.setTypeface(FontCache.instance(context).Get().face);
		
		Paint paintXor = new Paint();
		paintXor.setXfermode(new PixelXorXfermode(Color.WHITE));

		actions = new ArrayList<Action>();
		
		final ArrayList<NotificationType>  notificationHistory = Notification.history();
		for(final NotificationType n : notificationHistory) {
			actions.add(new Action() {
			
				NotificationType notification = n;
				
				public String getName() {
					return notification.description;
				}

				public void performAction(Context context) {
					Notification.replay(context, notification);
					
				} });
		}
		
		{
			actions.add(new Action() {
	
				public String getName() {
					return "Dummy 1";
				}
	
				public void performAction(Context context) {				
				} });
			
			actions.add(new Action() {
				
				public String getName() {
					return "Dummy 2";
				}
	
				public void performAction(Context context) {				
				} });
		}
		
		if (watchType == WatchType.DIGITAL) {
			
			Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.WHITE);	
			
			int y=1;
			int index = 0;
			
			for(Action a : actions) {
				
				final StaticLayout layout = new StaticLayout(a.getName(), paint, 85, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
				final int height = layout.getHeight();
				
				canvas.save();		
				canvas.translate(1, y);
				layout.draw(canvas);
				canvas.restore();

				if(index==currentSelection) {
					canvas.drawRect(0, y-1, 96, y+height, paintXor);
				}
				
				y+= height;
				index++;
			}			
			
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "switch_app.png"), 87, 0, null);	
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_down.bmp"), 87, 43, null);
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_right.bmp"), 87, 87, null);
			
			return bitmap;
		}
		else if (watchType == WatchType.ANALOG) {
			Bitmap bitmap = Bitmap.createBitmap(80, 32, Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.WHITE);	
			
			return bitmap;
		}
		
		return null;
	}

	public boolean buttonPressed(Context context, int id) {

		if(actions==null) {
			return false;
		}
		
		switch (id) {
		case ACTION_NEXT:
			currentSelection = (currentSelection+1)%actions.size();
			return true;
			
		case ACTION_PERFORM:
			actions.get(currentSelection).performAction(context);
			return true;
		}
		
		
		return false;
	}

}
