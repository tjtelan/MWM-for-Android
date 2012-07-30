package org.metawatch.manager.apps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppManager {

	static Map<String, ApplicationBase> apps = new HashMap<String, ApplicationBase>();
	
	public static void initApps() {
		if(apps.size()==0) {
			addApp(new MediaPlayerApp());
			addApp(new ActionsApp());
		}
	}
	
	public static void addApp(ApplicationBase app) {
		apps.put(app.getId(), app);
	}
	
	public static void removeApp(ApplicationBase app) {
		if (apps.containsKey(app.getId())) {
			apps.remove(app.getId());
		}
	}
	
	public static ApplicationBase.AppData[] getAppInfos() {
		List<ApplicationBase.AppData> list = new ArrayList<ApplicationBase.AppData>();
		for (ApplicationBase a : apps.values()) {
			list.add(a.getInfo());
		}
		return list.toArray(new ApplicationBase.AppData[0]);
	}
	
	public static ApplicationBase getApp(String appId) {
		if(!apps.containsKey(appId)) {
			return null;
		}
		
		return apps.get(appId);
	}
	
	public static int getAppState(String appId) {
		if(!apps.containsKey(appId)) {
			return ApplicationBase.INACTIVE;
		}
		
		return apps.get(appId).appState;
	}
}
