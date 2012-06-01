package org.metawatch.manager.actions;

import android.content.Context;

public interface ExitableAction extends Action {
	public boolean isRunning(Context context);
	public int performExit(Context context);
}