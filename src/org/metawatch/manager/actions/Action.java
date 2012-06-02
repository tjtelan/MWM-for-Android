package org.metawatch.manager.actions;

import org.metawatch.manager.apps.InternalApp;

import android.content.Context;

public abstract class Action {
	public static final int SECONDARY_NONE = 0;
	public static final int SECONDARY_RESET = 1;
	public static final int SECONDARY_EXIT = 2;
	
	public abstract String getName();
	public abstract String bulletIcon();
	public abstract int performAction(Context context);
	
	public boolean isHidden() {
		return false;
	}
	
	public int getSecondaryType() {
		return SECONDARY_NONE;
	}
	public int performSecondary(Context context) {
		return InternalApp.BUTTON_NOT_USED;
	}
	
	/*
	 * -1 for Actions that never have a timestamp.
	 * 0 for "no timestamp right now".
	 * Otherwise a timestamp.
	 */
	public long getTimestamp() {
		return -1;
	}
	
	public boolean isRunning(Context context) {
		return false;
	}
}