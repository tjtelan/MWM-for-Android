package org.metawatch.manager.actions;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public abstract class ContainerAction implements Action {
	protected List<Action> subActions = new ArrayList<Action>();

	public List<Action> getSubActions() {
		return subActions;
	}
	
	public int size() {
		return subActions.size();
	}
	
	public String bulletIcon() {
		return "bullet_plus.bmp"; //plus
	}
	
	// Never used, here to avoid having to reimplement it in subclasses.
	public int performAction(Context context) {
		return 0;
	}
}