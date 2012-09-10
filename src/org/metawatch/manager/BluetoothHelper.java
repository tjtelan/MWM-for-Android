package org.metawatch.manager;

import org.metawatch.manager.MetaWatchService.Preferences;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.bluetooth.BluetoothAdapter;

public class BluetoothHelper {

	// Dialogs
	
	public synchronized static boolean enableBluetoothDialog(final Context context) {
		
	
		if (Preferences.logging) Log.d(MetaWatch.TAG,
				"BluetoothHelper.enableBluetoothDialog()");
		
		if (MetaWatchService.bluetoothAdapter == null)
			MetaWatchService.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (MetaWatchService.bluetoothAdapter == null) {
			//sendToast("Bluetooth not supported");
			return false;
		}
		
		if(!MetaWatchService.bluetoothAdapter.isEnabled()){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Can't connect to watch.")
					.setMessage("Bluetooth is disabled. Would you like to turn it on?")
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   MetaWatchService.bluetoothAdapter.enable();
			               dialog.dismiss();
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       })
			       .create().show();
			
		}
		return true;
	}
	

	public synchronized static boolean disableBluetoothDialog(final Context context) {
		
		
		if (Preferences.logging) Log.d(MetaWatch.TAG,
				"BluetoothHelper.disableBluetoothDialog()");
		
		if(MetaWatchService.bluetoothAdapter.isEnabled()){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Can't connect to watch.")
					.setMessage("Bluetooth is enabled. Would you like to turn it off?")
			       .setCancelable(false)
			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   MetaWatchService.bluetoothAdapter.disable();
			               dialog.dismiss();
			           }
			       })
			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       })
			       .create().show();

		}
		

		return true;
	}

}

