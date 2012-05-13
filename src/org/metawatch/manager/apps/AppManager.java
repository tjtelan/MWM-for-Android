package org.metawatch.manager.apps;

import java.util.HashMap;
import java.util.Map;

public class AppManager {

	static Map<String, InternalApp> apps = new HashMap<String, InternalApp>();
	
	public static void initApps() {

		if(apps.size()==0) {
			addApp(new MediaPlayerApp());
			addApp(new ActionsApp());
		}
		
	}
	
	private static void addApp(InternalApp app) {
		apps.put(app.getInfo().id, app);
	}
	
	public static InternalApp getApp(String appId) {
		if(!apps.containsKey(appId)) {
			return null;
		}
		
		return apps.get(appId);
	}
	
}
