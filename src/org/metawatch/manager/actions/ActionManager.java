package org.metawatch.manager.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.metawatch.manager.Idle;
import org.metawatch.manager.MediaControl;
import org.metawatch.manager.MetaWatchService;
import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.MetaWatchService.WeatherProvider;
import org.metawatch.manager.Monitors;
import org.metawatch.manager.actions.InternalActions.PhoneCallAction;
import org.metawatch.manager.actions.InternalActions.PhoneSettingsAction;
import org.metawatch.manager.apps.ActionsApp;
import org.metawatch.manager.apps.AppManager;
import org.metawatch.manager.apps.InternalApp;
import org.metawatch.manager.apps.InternalApp.AppData;

import android.content.Context;
import android.content.pm.PackageManager;

public class ActionManager {
	
	static Map<String, Action> actions = new HashMap<String, Action>();
	
	static NotificationsAction notificationsAction = null;
	static PhoneSettingsAction phoneSettingsAction = null;
	static PhoneCallAction phoneCallAction = null;
	
	public static void initActions(final Context context) {
		if(actions.size()==0) {
			
			notificationsAction = new NotificationsAction();
			addAction(notificationsAction);
			
			phoneSettingsAction = new PhoneSettingsAction();
			addAction(phoneSettingsAction);
			
			PackageManager pm = context.getPackageManager();
			if (pm.hasSystemFeature(PackageManager.FEATURE_WIFI))
				addAction(new InternalActions.ToggleWifiAction(context), phoneSettingsAction);
			addAction(new InternalActions.ToggleSilentAction(context), phoneSettingsAction);		
			if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY))
				addAction(new InternalActions.SpeakerphoneAction(context), phoneSettingsAction);
			
			phoneCallAction = new PhoneCallAction();
			addAction(phoneCallAction);
			addAction(new InternalActions.SpeakerphoneAction(context), phoneCallAction);
			addAction(new Action(){

				@Override
				public String getName() {
					return "Test";
				}

				@Override
				public String bulletIcon() {
					return "bullet_circle.bmp";
				}

				@Override
				public int performAction(Context context) {
					
					
					
					return InternalApp.BUTTON_USED;
				}
				
			}, phoneCallAction);
			
			addAction(new Action(){

				@Override
				public String getName() {
					return "Hang up";
				}

				@Override
				public String bulletIcon() {
					return "bullet_circle.bmp";
				}

				@Override
				public int performAction(Context context) {
					MediaControl.DismissCall(context);
					return InternalApp.BUTTON_USED;
				}
				
			}, phoneCallAction);
			
			addAction(new InternalActions.PingAction());
			addAction(new InternalActions.WeatherRefreshAction());
			addAction(new InternalActions.ClickerAction());
			
			
		}
	}
	
	public static void addAction(Action action) {
		if (action.getId()!=null) {
			actions.put(action.getId(), action);
		}
	}
	
	public static void addAction(Action action, ContainerAction parent) {
		if (action.getId()!=null) {
			actions.put(action.getId(), action);
		}

		parent.addSubAction(action);
	}
	
	public static void addAction(Action action, String parentId) {
		if (actions.containsKey(parentId)) {
			Action parent = actions.get(parentId);
			addAction(action, (ContainerAction)parent);
		}
		else {
			addAction(action);
		}
	}
	
	public static Action getAction(final String id) {
		if (!actions.containsKey(id))
			return null;
		return actions.get(id);
	}
	
	private static List<Action> getAppActions() {
		List<Action> list = new ArrayList<Action>();
		
		for (final AppData a : AppManager.getAppInfos()) {
			if (a.id.equals(ActionsApp.APP_ID))
				continue; // Skip actions app.
			
			int watchType = MetaWatchService.watchType;
			if ((watchType == MetaWatchService.WatchType.ANALOG && !a.supportsAnalog) ||
					(watchType == MetaWatchService.WatchType.DIGITAL && !a.supportsDigital))
				continue; // Skip unsupported apps.
				
			
			list.add(new Action() {
				
				public String getId() {
					return "launch"+a.id;
				}
				
				public String getName() {
					return a.name;
				}
				
				private boolean isRunning() {
					return Idle.getAppPage(a.id)!=-1;
				}

				public String bulletIcon() {
					return isRunning() ? "bullet_square_open.bmp" 
							 		   : "bullet_square.bmp";
				}

				public int performAction(Context context) {
					AppManager.getApp(a.id).open(context, false);
					return InternalApp.BUTTON_USED_DONT_UPDATE;
				}
				
				public int getSecondaryType() {
					return isRunning() ? Action.SECONDARY_EXIT
									   : Action.SECONDARY_NONE;
				}
				public int performSecondary(Context context) {
					if (isRunning()) {
						Idle.removeAppPage(context, AppManager.getApp(a.id));
						return InternalApp.BUTTON_USED;
					}
					
					return InternalApp.BUTTON_NOT_USED;
				}
			});
		}
		
		return list;
	}
	
	public static List<Action> getRootActions() {
		List<Action> result = new ArrayList<Action>();
		
		if (Monitors.CallData.inCall)
			result.add(phoneCallAction);
			
		notificationsAction.refreshSubActions();
		result.add(notificationsAction);
		result.addAll(getAppActions());
		result.add(phoneSettingsAction);
		
		result.add(getAction(InternalActions.PingAction.id));
		
		if (Preferences.weatherProvider!=WeatherProvider.DISABLED)
			result.add(getAction(InternalActions.WeatherRefreshAction.id));
		
		//result.add(getAction(InternalActions.MapsAction.id);
		//result.add(getAction(InternalActions.WoodchuckAction.id);
		
		/*
		// For scroll testing.
		for (int i = 0; i < 12; i++) {
			final int f = i;
			result.add(new Action() {
				public String getName() {
					return String.valueOf(f);
				}

				public String bulletIcon() {
					return "bullet_triangle.bmp";
				}

				public int performAction(Context context) {
					return BUTTON_USED;
				}
			});
		}
		*/
		
		
		return result;
	}
	
	public static void displayAction(final Context context, ContainerAction container) {
		ActionsApp app = (ActionsApp)AppManager.getApp(ActionsApp.APP_ID);
		app.displayContainer(container);
		app.open(context, true);
	}
	
	public static void displayCallActions(final Context context) {
		displayAction(context, phoneCallAction);
	}
	
}
