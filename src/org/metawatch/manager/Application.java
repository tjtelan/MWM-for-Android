                                                                     
                                                                     
                                                                     
                                             
 /*****************************************************************************
  *  Copyright (c) 2011 Meta Watch Ltd.                                       *
  *  www.MetaWatch.org                                                        *
  *                                                                           *
  =============================================================================
  *                                                                           *
  *  Licensed under the Apache License, Version 2.0 (the "License");          *
  *  you may not use this file except in compliance with the License.         *
  *  You may obtain a copy of the License at                                  *
  *                                                                           *
  *    http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                           *
  *  Unless required by applicable law or agreed to in writing, software      *
  *  distributed under the License is distributed on an "AS IS" BASIS,        *
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
  *  See the License for the specific language governing permissions and      *
  *  limitations under the License.                                           *
  *                                                                           *
  *****************************************************************************/

 /*****************************************************************************
  * Application.java                                                          *
  * Application                                                               *
  * Application watch mode                                                    *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

import org.metawatch.manager.apps.InternalApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

public class Application {
	//FIXME This class has next to NO support for analog watches...
	
	public final static byte EXIT_APP = 90;
	
	private static InternalApp currentApp = null;

	public static void startAppMode(Context context) {
		startAppMode(context, null);
	}
	
	public static void startAppMode(Context context, InternalApp internalApp) {
		if (currentApp != null) {
			stopAppMode(context);
		}

		MetaWatchService.WatchModes.APPLICATION = true;
		currentApp = internalApp;
	}
	
	public static void stopAppMode(Context context) {
		MetaWatchService.WatchModes.APPLICATION = false;
		
		if (MetaWatchService.watchType == MetaWatchService.WatchType.DIGITAL) {
			Protocol.disableButton(0, 0, MetaWatchService.WatchBuffers.APPLICATION); // right top - immediate
		} else if (MetaWatchService.watchType == MetaWatchService.WatchType.ANALOG) {
			Protocol.disableButton(1, 0, MetaWatchService.WatchBuffers.APPLICATION); // right middle - immediate
		}
		if (currentApp != null) {
			currentApp.deactivate(MetaWatchService.watchType);
			currentApp.standaloneStop(context);
		}
		currentApp = null;
		
		if (MetaWatchService.WatchModes.IDLE == true) {
			Idle.toIdle(context);
		}
	}
	
	public static void updateAppMode(Context context) {
		Bitmap bitmap;
		if (currentApp != null) {
			bitmap = currentApp.update(context, MetaWatchService.watchType);
		} else {
			bitmap = Protocol.createTextBitmap(context, "Starting application mode ...");
		}
		
		updateAppMode(context, bitmap);
	}
	
	public static void updateAppMode(Context context, Bitmap bitmap) {
		MetaWatchService.WatchModes.APPLICATION = true;
		
		if (MetaWatchService.WatchModes.APPLICATION == true) {
			
			// enable app mode if there is no parent mode currently active
			if (MetaWatchService.watchState < MetaWatchService.WatchStates.APPLICATION)
				MetaWatchService.watchState = MetaWatchService.WatchStates.APPLICATION;
			
			if (MetaWatchService.watchState == MetaWatchService.WatchStates.APPLICATION) {
				Protocol.sendLcdBitmap(bitmap, MetaWatchService.WatchBuffers.APPLICATION);
				Protocol.updateLcdDisplay(MetaWatchService.WatchBuffers.APPLICATION);
			}
		}		
	}
	
	public static void updateAppMode(Context context, int[] array) {
		MetaWatchService.WatchModes.APPLICATION = true;
		
		if (MetaWatchService.WatchModes.APPLICATION == true) {
			
			// enable app mode if there is no parent mode currently active
			if (MetaWatchService.watchState < MetaWatchService.WatchStates.APPLICATION)
				MetaWatchService.watchState = MetaWatchService.WatchStates.APPLICATION;
			
			if (MetaWatchService.watchState == MetaWatchService.WatchStates.APPLICATION) {
				Protocol.sendLcdArray(array, MetaWatchService.WatchBuffers.APPLICATION);
				Protocol.updateLcdDisplay(MetaWatchService.WatchBuffers.APPLICATION);
			}
		}		
	}
	
	public static void updateAppMode(Context context, byte[] buffer) {
		MetaWatchService.WatchModes.APPLICATION = true;
		
		if (MetaWatchService.WatchModes.APPLICATION == true) {
			
			// enable app mode if there is no parent mode currently active
			if (MetaWatchService.watchState < MetaWatchService.WatchStates.APPLICATION)
				MetaWatchService.watchState = MetaWatchService.WatchStates.APPLICATION;
			
			if (MetaWatchService.watchState == MetaWatchService.WatchStates.APPLICATION) {
				Protocol.sendLcdBuffer(buffer, MetaWatchService.WatchBuffers.APPLICATION);
				Protocol.updateLcdDisplay(MetaWatchService.WatchBuffers.APPLICATION);
			}
		}		
	}
	
	public static void toApp() {
		MetaWatchService.watchState = MetaWatchService.WatchStates.APPLICATION;
		
		Idle.deactivateButtons();

		int watchType = MetaWatchService.watchType;
		if (currentApp != null) {
			currentApp.activate(watchType);
		}
		if (watchType == MetaWatchService.WatchType.DIGITAL) {
			Protocol.enableButton(0, 0, EXIT_APP, MetaWatchService.WatchBuffers.APPLICATION); // right top - immediate
		} else if (watchType == MetaWatchService.WatchType.ANALOG) {
			Protocol.enableButton(1, 0, EXIT_APP, MetaWatchService.WatchBuffers.APPLICATION); // right middle - immediate
		}
		
		// update screen with cached buffer
		Protocol.updateLcdDisplay(MetaWatchService.WatchBuffers.APPLICATION);
	}
	
	public static void buttonPressed(Context context, byte button) {
		if (button == EXIT_APP) {
			stopAppMode(context);
			
		} else if (currentApp != null) {
			currentApp.buttonPressed(context, button);
			
		} else {
			// Broadcast button to external app
			Intent intent = new Intent("org.metawatch.manager.BUTTON_PRESS");
			intent.putExtra("button", button);
			context.sendBroadcast(intent);
		}
	}
}
