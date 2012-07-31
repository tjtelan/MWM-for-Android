package org.metawatch.manager.apps;

import org.metawatch.manager.Protocol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;

public class ExternalApp extends ApplicationBase {
	
	Bitmap buffer = null;
	AppData appData = null;
	boolean receivedData = false;
	
	public ExternalApp(final String appId, final String appName) {
		
		appData = new AppData() {{
			id = appId;
			name = appName;
		
			supportsDigital = true;
			supportsAnalog = false;
			
			pageSettingKey = "idle" + appId;
			pageSettingAttribute = "idle" + appId;
		}};
	}
	
	

	@Override
	public AppData getInfo() {
		return appData;
	}

	@Override
	public void activate(Context context, int watchType) {
		if (appData!=null) {
			Intent intent = new Intent("org.metawatch.manager.APPLICATION_ACTIVATE");
			Bundle b = new Bundle();
			b.putString("id", appData.id);
			intent.putExtras(b);
			context.sendBroadcast(intent);
		}
	}

	@Override
	public void deactivate(Context context, int watchType) {
		if (appData!=null) {
			Intent intent = new Intent("org.metawatch.manager.APPLICATION_DEACTIVATE");
			Bundle b = new Bundle();
			b.putString("id", appData.id);
			intent.putExtras(b);
			context.sendBroadcast(intent);
		}
	}

	@Override
	public Bitmap update(Context context, boolean preview, int watchType) {
		initBuffer(context);
		receivedData = true;
		Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(buffer, 0, 0, null);
		drawDigitalAppSwitchIcon(context, canvas, preview);
		return bitmap;
	}

	@Override
	public int buttonPressed(Context context, int id) {
		return ApplicationBase.BUTTON_NOT_USED;
	}
	
	private void initBuffer(Context context) {
		if (buffer == null) {
			buffer = Protocol.createTextBitmap(context, "Starting application mode ...");
		}
	}
	
	public void setBuffer(Bitmap bitmap) {
		buffer = bitmap;
	}

}
