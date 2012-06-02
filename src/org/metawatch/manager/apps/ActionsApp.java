package org.metawatch.manager.apps;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.metawatch.manager.FontCache;
import org.metawatch.manager.Idle;
import org.metawatch.manager.MetaWatchService;
import org.metawatch.manager.MetaWatchService.Preferences;
import org.metawatch.manager.MetaWatchService.WeatherProvider;
import org.metawatch.manager.Notification;
import org.metawatch.manager.Protocol;
import org.metawatch.manager.Notification.NotificationType;
import org.metawatch.manager.actions.Action;
import org.metawatch.manager.actions.ContainerAction;
import org.metawatch.manager.actions.InternalActions;
import org.metawatch.manager.Utils;
import org.metawatch.manager.MetaWatchService.WatchType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelXorXfermode;
import android.graphics.Region;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;

public class ActionsApp extends InternalApp {
	
	public final static String APP_ID = "org.metawatch.manager.apps.ActionsApp";
	
	static AppData appData = new AppData() {{
		id = APP_ID;
		name = "Actions";
	
		supportsDigital = true;
		supportsAnalog = true;
		
		pageSettingKey = "IdleActions";
		pageSettingAttribute = "idleActions";
	}};
	
	public final static byte ACTION_NEXT = 30;
	public final static byte ACTION_PERFORM = 31;
	public final static byte ACTION_SECONDARY = 32;
	public final static byte ACTION_TOP = 33;
	
	public static class NotificationsAction extends ContainerAction {
		public String getName() {
			return "Recent Notifications";
		}
		
		public String getTitle() {
			return "Notifications";
		}

		public long getTimestamp() {
			if (subActions.size() > 0) {
				return subActions.get(0).getTimestamp();
			} else {
				return 0;
			}
		}
		
	}
	
	public AppData getInfo() {
		return appData;
	}
	
	public boolean isToggleable() {
		// Always provide a way to reach the Actions app (since it's quite central).
		if (MetaWatchService.watchType == MetaWatchService.WatchType.DIGITAL &&
				Preferences.quickButton == Idle.QB_OPEN_ACTIONS) {
			// Only set toggleable if Quick Button can open it again.
			return true;
		}
		
		return false;
	}
	
	List<Action> internalActions = null;
	Stack<ContainerAction> containerStack = new Stack<ContainerAction>(); //Contains the stack of ContainerActions opened (empty if at root).
	List<Action> currentActions = null; //Contains the list as it's shown on the screen (including a "back" item for sub level lists).
	Action backAction = null;
	NotificationsAction notificationsAction = null;
	
	Stack<Integer> selectionStack = new Stack<Integer>(); //Only contains selection for upper level lists.
	int currentSelection = 0;
	
	private List<Action> getAppActions() {
		List<Action> list = new ArrayList<Action>();
		
		for (final AppData a : AppManager.getAppInfos()) {
			if (a.id.equals(APP_ID))
				continue; // Skip self.
			
			//if (Idle.getAppPage(a.id) != -1) {
			//	continue; // Skip apps running as idle pages.
			//}
			
			int watchType = MetaWatchService.watchType;
			if ((watchType == MetaWatchService.WatchType.ANALOG && !a.supportsAnalog) ||
					(watchType == MetaWatchService.WatchType.DIGITAL && !a.supportsDigital))
				continue; // Skip unsupported apps.
				
			
			list.add(new Action() {
				public String getName() {
					return a.name;
				}

				public String bulletIcon() {
					return isRunning(null) ? "bullet_square_open.bmp" 
							 			   : "bullet_square.bmp";
				}

				public int performAction(Context context) {
					AppManager.getApp(a.id).open(context, false);
					return BUTTON_USED_DONT_UPDATE;
				}
				
				public boolean isRunning(Context context) {
					return Idle.getAppPage(a.id)!=-1;
				}
				
				public int getSecondaryType() {
					return isRunning(null) ? Action.SECONDARY_EXIT
										   : Action.SECONDARY_NONE;
				}
				public int performSecondary(Context context) {
					if (isRunning(null)) {
						Idle.removeAppPage(context, AppManager.getApp(a.id));
						return BUTTON_USED;
					}
					
					return BUTTON_NOT_USED;
				}
			});
		}
		
		return list;
	}

	private List<Action> getNotificationActions() {
		List<Action> list = new ArrayList<Action>();
		for(final NotificationType n : Notification.history()) {
			list.add(new Action() {
				public String getName() {
					return n.description;
				}
				
				public long getTimestamp() {
					return n.timestamp;
				} 
				
				public String bulletIcon() {
					return "bullet_triangle.bmp";
				}
	
				public int performAction(Context context) {
					Notification.replay(context, n);
					// DONT_UPDATE since the idle screen overwrites the notification otherwise.
					return BUTTON_USED_DONT_UPDATE;
				}
			});
		}
		
		return list;
	}
	
	private void init(final Context context) {
		if (backAction == null) {
			backAction = new Action() {
				public String getName() {
					return "-- Back --";
				}
				
				public String bulletIcon() {
					return null;
				}
				
				public int performAction(Context context) {
					if (!containerStack.isEmpty()) {
						containerStack.pop();
						currentSelection = selectionStack.pop();
					}
					return BUTTON_USED;
				}
			};
		}
		if (notificationsAction == null) {
			notificationsAction = new NotificationsAction();
		}
		
		if (internalActions == null) {
			internalActions = new ArrayList<Action>();

			internalActions.add(new InternalActions.PingAction());
			if (Preferences.weatherProvider!=WeatherProvider.DISABLED)
				internalActions.add(new InternalActions.WeatherRefreshAction());
			internalActions.add(new InternalActions.SpeakerphoneAction(context));
			internalActions.add(new InternalActions.ClickerAction());
			//internalActions.add(new InternalActions.MapsAction());
			//internalActions.add(new InternalActions.WoodchuckAction());
			
			/*
			// For scroll testing.
			for (int i = 0; i < 12; i++) {
				final int f = i;
				internalActions.add(new Action() {
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
		}
		
		if (currentActions == null) {
			currentActions = new ArrayList<Action>();
		}
	}

	public void activate(final Context context, int watchType) {
		init(context);
		
		if (watchType == WatchType.DIGITAL) {
			Protocol.enableButton(1, 1, ACTION_NEXT, MetaWatchService.WatchBuffers.APPLICATION); // right middle - press
			Protocol.enableButton(1, 2, ACTION_TOP, MetaWatchService.WatchBuffers.APPLICATION); // right middle - hold
			Protocol.enableButton(1, 3, ACTION_TOP, MetaWatchService.WatchBuffers.APPLICATION); // right middle - long hold
			
			Protocol.enableButton(2, 1, ACTION_PERFORM, MetaWatchService.WatchBuffers.APPLICATION); // right bottom - press
			Protocol.enableButton(2, 2, ACTION_SECONDARY, MetaWatchService.WatchBuffers.APPLICATION); // right bottom - hold
			Protocol.enableButton(2, 3, ACTION_SECONDARY, MetaWatchService.WatchBuffers.APPLICATION); // right bottom - long hold
		}
		else if (watchType == WatchType.ANALOG) {
			Protocol.enableButton(0, 1, ACTION_NEXT, MetaWatchService.WatchBuffers.APPLICATION); // top - press
			Protocol.enableButton(0, 2, ACTION_TOP, MetaWatchService.WatchBuffers.APPLICATION); // top - hold
			Protocol.enableButton(0, 3, ACTION_TOP, MetaWatchService.WatchBuffers.APPLICATION); // top - long hold
			
			Protocol.enableButton(2, 1, ACTION_PERFORM, MetaWatchService.WatchBuffers.APPLICATION); // bottom - press
			Protocol.enableButton(2, 2, ACTION_SECONDARY, MetaWatchService.WatchBuffers.APPLICATION); // bottom - hold
			Protocol.enableButton(2, 3, ACTION_SECONDARY, MetaWatchService.WatchBuffers.APPLICATION); // bottom - long hold
		}
	}

	public void deactivate(final Context context, int watchType) {
		if (!containerStack.isEmpty()) {
			//Return to root.
			containerStack.clear();
			while(selectionStack.size() > 1)
				selectionStack.pop();
			currentSelection = selectionStack.pop();
		}
		
		if (watchType == WatchType.DIGITAL) {
			Protocol.disableButton(1, 1, MetaWatchService.WatchBuffers.APPLICATION);
			Protocol.disableButton(2, 1, MetaWatchService.WatchBuffers.APPLICATION);
			Protocol.disableButton(2, 2, MetaWatchService.WatchBuffers.APPLICATION);
			Protocol.disableButton(2, 3, MetaWatchService.WatchBuffers.APPLICATION);
		}
		else if (watchType == WatchType.ANALOG) {
			Protocol.disableButton(0, 1, MetaWatchService.WatchBuffers.APPLICATION);
			Protocol.disableButton(2, 1, MetaWatchService.WatchBuffers.APPLICATION);
			Protocol.disableButton(2, 2, MetaWatchService.WatchBuffers.APPLICATION);
			Protocol.disableButton(2, 3, MetaWatchService.WatchBuffers.APPLICATION);
		}
		
	}

	public Bitmap update(final Context context, boolean preview, int watchType) {
		init(context);
				
		// This is not the nicest solution, but it keeps the display updated.
		if (containerStack.isEmpty() ||
				containerStack.peek() == notificationsAction) {
			List<Action> notifications = notificationsAction.getSubActions();
			notifications.clear();
			notifications.addAll(getNotificationActions());
		}

		currentActions.clear();
		if (containerStack.isEmpty()) {
			// At the root.
			//TODO Add external actions using intents, similar to widgets.
			currentActions.add(notificationsAction);
			currentActions.addAll(getAppActions());
			currentActions.addAll(internalActions);
		} else {
			// In a ContainerAction.
			if (watchType==WatchType.DIGITAL)
				currentActions.add(backAction);
			currentActions.addAll(containerStack.peek().getSubActions());
			
			// Put the back action at the end on Analog, so the first displayed
			// element actually has content.
			if (watchType==WatchType.ANALOG)
				currentActions.add(backAction);
		}
		
		// Clean away empty actions.
		ListIterator<Action> it = currentActions.listIterator();
		while (it.hasNext()) {
			Action a = it.next();
			if (a.isHidden()) {
				it.remove();
			}
		}
		
		if (currentSelection >= currentActions.size()) {
			currentSelection = 0;
		}
		
		if (watchType == WatchType.DIGITAL) {	
			return drawDigital(context, preview);		
		} else if (watchType == WatchType.ANALOG) {
			return drawAnalog(context, preview);
		}
		
		return null;
	}

	private Bitmap drawDigital(final Context context, boolean preview) {
		TextPaint paint = new TextPaint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(FontCache.instance(context).Get().size);
		paint.setTypeface(FontCache.instance(context).Get().face);
		int textHeight = FontCache.instance(context).Get().realSize;

		Paint paintXor = new Paint();
		paintXor.setXfermode(new PixelXorXfermode(Color.WHITE));
		
		Paint paintWhite = new Paint();
		paintWhite.setColor(Color.WHITE);
		
		// Double the height to make room for multi line items that trigger scrolling.
		Bitmap bitmap = Bitmap.createBitmap(96, 192, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		
		final int maxY = 96 - textHeight;
		int y = textHeight + 5; //Make room for a title.

		boolean scrolled = false;
		for (int i = Math.max(0, currentSelection - 96/textHeight + 4);
				(i < currentActions.size() && (i <= currentSelection || y <= 96));
				i++) {
			Action a = currentActions.get(i);
			
			if (a.bulletIcon() != null) {
				canvas.drawBitmap(Utils.loadBitmapFromAssets(context, a.bulletIcon()), 1, y, null);
			}
			
			if (i == currentSelection) {
				// Draw full multi-line text.
				StringBuilder name = new StringBuilder(a.getName());
				if (a instanceof ContainerAction) {
					name.append(" (");
					name.append(((ContainerAction)a).size());
					name.append(")");
				}
				
				final StaticLayout layout = new StaticLayout(name, paint, 79, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
				final int height = layout.getHeight();
				
				final int top = y;
				
				canvas.save();		
				canvas.translate(7, y);
				layout.draw(canvas);
				canvas.restore();
			
				y+= height;
				
				// Draw timestamp, if any.
				final long timestamp = a.getTimestamp();
				if (timestamp != -1) {
					final String timetext = (timestamp > 0 ?
							Utils.ticksToText(context, timestamp, true) :
							"---");
					canvas.drawLine(7, y, 86, y, paint);
					y+= 2;
					canvas.drawText((String) TextUtils.ellipsize(timetext, paint, 79, TruncateAt.START), 7, y+textHeight, paint);
					y+= textHeight+1;
				}

				// Invert item to mark as selected.
				canvas.drawRect(0, top-1, 96, y, paintXor);
				
				// Scroll screen if necessary.
				if (y >= maxY) {
					final int scroll = y - maxY;
					bitmap = Bitmap.createBitmap(bitmap, 0, scroll, 96, 96);
					canvas.setBitmap(bitmap);
					y -= scroll;
					
					if (i == currentActions.size() - 1) {
						// Mark the end of the list.
						Idle.drawLine(canvas, 96 - textHeight/2 - 1);
					}
					scrolled = true;
				}
							
			} else {
				//Draw elipsized text.
				canvas.drawText((String) TextUtils.ellipsize(a.getName(), paint, 79, TruncateAt.END), 7, y+textHeight, paint);
				y+= textHeight+1;
			}
		}
		
		// Draw title.
		if (scrolled) {
			// Paint white over any scrolled items.
			canvas.drawRect(0, 0, 95, textHeight+4, paintWhite);
		}
		String title = (containerStack.isEmpty() ? "Actions" : containerStack.peek().getTitle());
		canvas.drawText((String) TextUtils.ellipsize(title, paint, 84, TruncateAt.END), 2, textHeight+1, paint);
		canvas.drawLine(1, textHeight+2, (isToggleable() ? 79 : 87), textHeight+2, paint);
		
		// Draw icons.
		drawDigitalAppSwitchIcon(context, canvas, preview);
		canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_down.bmp"), 87, 43, null);
		
		final int currentType = currentActions.get(currentSelection).getSecondaryType();
		//TODO split the secodary icons to separate files and draw them in addition to the right icon.
		if (currentType == Action.SECONDARY_RESET) {
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_reset_right.bmp"), 79, 87, null);
		} else if (currentType == Action.SECONDARY_EXIT) {
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_exit_right.bmp"), 79, 87, null);
		} else {
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_right.bmp"), 87, 87, null);
		}
		
		// If the screen hasn't scrolled, the bitmap is too large, shrink it.
		if (!scrolled) {
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, 96, 96);
		}
		return bitmap;
	}
	
	private Bitmap drawAnalog(final Context context, boolean preview) {
		TextPaint paint = new TextPaint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(FontCache.instance(context).Small.size);
		paint.setTypeface(FontCache.instance(context).Small.face);

		Paint paintXor = new Paint();
		paintXor.setXfermode(new PixelXorXfermode(Color.WHITE));
		
		Paint paintWhite = new Paint();
		paintWhite.setColor(Color.WHITE);
		
		Bitmap bitmap = Bitmap.createBitmap(80, 32, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		
		// Top screen
		canvas.clipRect(0, 0, 80, 16, Region.Op.REPLACE);
		
		String title = "Actions";
		StringBuilder position = new StringBuilder();
		if (currentActions.size()>0) {
			position.append("(");
			position.append(currentSelection+1);
			position.append("/");
			position.append(currentActions.size());
			position.append(")");
		}
					
		if (!containerStack.isEmpty()) {
			title = containerStack.peek().getTitle();
		}
		
		canvas.drawText((String) TextUtils.ellipsize(title, paint, 74, TruncateAt.END), 0, 6, paint);
		canvas.drawText((String) TextUtils.ellipsize(position.toString(), paint, 74, TruncateAt.END), 0, 13, paint);
		
		canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_down_5.bmp"), 75, 11, null);
		
		// Bottom screen
		canvas.clipRect(0, 16, 80, 32, Region.Op.REPLACE);
		
		Action a = currentActions.get(currentSelection);
		String itemLine1 = a.getName();
		String itemLine2 = "";
		final long timestamp = a.getTimestamp();
		if (timestamp != -1) {
			itemLine2 = (timestamp > 0 ?
					Utils.ticksToText(context, timestamp, true) :
					"---");
		}
		
		canvas.drawText((String) TextUtils.ellipsize(itemLine1, paint, 74, TruncateAt.END), 0, 22, paint);
		canvas.drawText((String) TextUtils.ellipsize(itemLine2, paint, 74, TruncateAt.END), 0, 29, paint);
		
		final int type = a.getSecondaryType();
		//TODO split the secodary icons to separate files and draw them in addition to the right icon.
		if (type == Action.SECONDARY_RESET) {
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_reset_right_5.bmp"), 75, 16, null);
		} else if (type == Action.SECONDARY_EXIT) {
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_exit_right_5.bmp"), 75, 16, null);
		} else {
			canvas.drawBitmap(Utils.loadBitmapFromAssets(context, "action_right_5.bmp"), 75, 16, null);
		}
				
		return bitmap;
	}

	public int buttonPressed(Context context, int id) {
		if(currentActions==null) {
			return BUTTON_NOT_USED;
		}
		
		if (currentSelection >= currentActions.size()) {
			currentSelection = 0;
		}
		
		Action currentAction = currentActions.get(currentSelection);
		switch (id) {
		case ACTION_NEXT:
			currentSelection = (currentSelection+1)%currentActions.size();
			return BUTTON_USED;
			
		case ACTION_TOP:
			currentSelection = 0;
			return BUTTON_USED;
			
		case ACTION_PERFORM:
			if (currentAction instanceof ContainerAction) {
				containerStack.push((ContainerAction)currentAction);
				selectionStack.push(currentSelection);
				currentSelection = 0;
				
				return BUTTON_USED;
				
			} else {
				return currentAction.performAction(context);
			}
			
		case ACTION_SECONDARY:
			return currentAction.performSecondary(context);
		}
		
		
		return BUTTON_NOT_USED;
	}

}
