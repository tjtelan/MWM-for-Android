package org.metawatch.manager.actions;

import org.metawatch.manager.MediaControl;
import org.metawatch.manager.Monitors;
import org.metawatch.manager.Monitors.WeatherData;
import org.metawatch.manager.apps.InternalApp;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;

public class InternalActions {
	
	public abstract static class ToggleAction extends Action {
		protected abstract boolean isEnabled();
		
		public String bulletIcon() {
			return isEnabled()
					? "bullet_circle_open.bmp"
			        : "bullet_circle.bmp";
		}
	}
	
	public static class PingAction extends ToggleAction {
		Ringtone r = null;
		int volume = -1;
		int ringerMode = 0;
		
		public String getName() {
			return isSilent()
				? "Ping phone"
				: "Stop alarm";
		}
	
		public int performAction(Context context) {
			if (isSilent()) {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
				AudioManager as = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				volume = as.getStreamVolume(AudioManager.STREAM_RING);
				ringerMode = as.getRingerMode();
				
				as.setStreamVolume(AudioManager.STREAM_RING, as.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
				as.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				r = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
				r.play();
			}
			else {
				r.stop();
				r = null;
				
				AudioManager as = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				as.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
				as.setRingerMode(ringerMode);
			}
			return InternalApp.BUTTON_USED;
		}
		
		protected boolean isEnabled() {
			return !isSilent();
		}
		
		private boolean isSilent() {
			return (r==null || r.isPlaying() == false);
		}
	}

	public static class SpeakerphoneAction extends ToggleAction {
		public SpeakerphoneAction(Context context) {
			audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}
		
		private AudioManager audioManager = null;
		
		public String getName() {
			return isEnabled()
					? "Disable speakerphone"
			        : "Enable speakerphone";
		}
		
		protected boolean isEnabled() {
			return audioManager !=null && audioManager.isSpeakerphoneOn();
		}

		public int performAction(Context context) {
			MediaControl.ToggleSpeakerphone(context);
			return InternalApp.BUTTON_USED;
		}
	}
	
	public static class ClickerAction extends Action {
		int count = 0;
		long timestamp = 0;
		
		public String getName() {
			return "Clicker: "+count;
		}
		
		public String bulletIcon() {
			return "bullet_circle.bmp";
		}
	
		public int performAction(Context context) {
			count++;
			timestamp = System.currentTimeMillis();
			
			return InternalApp.BUTTON_USED;
		}
		
		public int getSecondaryType() {
			return Action.SECONDARY_RESET;
		}
		public int performSecondary(Context context) {
			count = 0;
			timestamp = 0;
			return InternalApp.BUTTON_USED;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
	}
	
	public static class WeatherRefreshAction extends Action {
		
		public String getName() {
			return WeatherData.received 
					? "Refresh Weather"
					: "Refreshing...";
		}
		
		public String bulletIcon() {
			return "bullet_circle.bmp";
		}
	
		public int performAction(Context context) {
			Monitors.updateWeatherDataForced(context);
			
			return InternalApp.BUTTON_USED;
		}
		
		public long getTimestamp() {
			return WeatherData.timeStamp;
		}
	}
	
	public static class ToggleWifiAction extends ToggleAction {
		
		WifiManager wifiMgr = null;
		
		public ToggleWifiAction(Context context) {
			wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		}
		
		public String getName() {
			return isEnabled() 
					? "Disable Wifi"
					: "Enable Wifi";
		}
		
		protected boolean isEnabled() {
			return wifiMgr !=null && wifiMgr.isWifiEnabled();
		}
	
		public int performAction(Context context) {
			if (wifiMgr!=null) {
				wifiMgr.setWifiEnabled( !wifiMgr.isWifiEnabled() );
			}
			
			return InternalApp.BUTTON_USED;
		}
		
	}
	
	public static class ToggleSilentAction extends ToggleAction {
		public ToggleSilentAction(Context context) {
			audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}
		
		private AudioManager audioManager = null;
		
		public String getName() {
			return isEnabled()
					? "Disable silent mode"
			        : "Enable silent mode";
		}
		
		protected boolean isEnabled() {
			return audioManager !=null && (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT);
		}

		public int performAction(Context context) {
			audioManager.setRingerMode( audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT
					? AudioManager.RINGER_MODE_NORMAL 
					: AudioManager.RINGER_MODE_SILENT );
			return InternalApp.BUTTON_USED;
		}
	}

	public static class WoodchuckAction extends Action {
		private static final String QUESTION = "How much wood would a woodchuck chuck if a woodchuck could chuck wood?";
		private static final String ANSWER = "A woodchuck could chuck no amount of wood, since a woodchuck can't chuck wood.";
		String name = QUESTION;
		
		public String getName() {
			return name;
		}
		
		public String bulletIcon() {
			return "bullet_square.bmp";
		}

		public int performAction(Context context) {
			name = ANSWER;
			return InternalApp.BUTTON_USED;
		}
		
		public int getSecondaryType() {
			return Action.SECONDARY_RESET;
		}
		public int performSecondary(Context context) {
			name = QUESTION;
			return InternalApp.BUTTON_USED;
		}
	}
	
	public static class MapsAction extends Action {
		public String getName() {
			return "Launch Google Maps on phone";
		}
		
		public String bulletIcon() {
			return "bullet_square.bmp";
		}

		public int performAction(Context context) {					
			Intent mapsIntent = context.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
			context.startActivity(mapsIntent);
			return InternalApp.BUTTON_USED;
		}
	}
}
