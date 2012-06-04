package org.metawatch.manager.actions;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public abstract class ContainerAction extends Action {
	protected List<Action> subActions = new ArrayList<Action>();
	
	public void refreshSubActions() {
	}
	
	public List<Action> getSubActions() {
		return subActions;
	}
	
	public void addSubAction(Action action) {
		if(action.getParent()!=null) {
			action.getParent().removeSubAction(action);
		}
		subActions.add(action);
		action.setParent(this);
	}
	
	public void removeSubAction(Action action) {
		subActions.remove(action);
		action.setParent(null);
	}
	
	public int size() {
		return subActions.size();
	}
	
	public boolean isHidden() {
		return subActions.isEmpty();
	}
	
	// Overridable by subclasses, but default to the name.
	public String getTitle() {
		return getName();
	}
	
	public String bulletIcon() {
		return "bullet_plus.bmp"; //plus
	}
	
	// Never used, here to avoid having to reimplement it in subclasses.
	public int performAction(Context context) {
		return 0;
	}
	
	// Override to provide a custom back action
	public Action getBackAction() {
		return null;
	}
}