package org.metawatch.manager.actions;

import org.metawatch.manager.Idle;
import org.metawatch.manager.MetaWatchService;
import org.metawatch.manager.apps.AppManager;
import org.metawatch.manager.apps.InternalApp;
import org.metawatch.manager.apps.InternalApp.AppData;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;

public class QuickDialAction extends ContainerAction {

	public String id = "quickdial";
	public String getId() {
		return id;
	}
	
	public String getName() {
		return "Quickdial";
	}
	
	public void refreshSubActions(Context context) {
		subActions.clear();
		
		subActions.add(ActionManager.getAction(InternalActions.SpeakerphoneAction.id));
		
		subActions.add(new Action(){

			@Override
			public String getName() {
				return "Dial Voicemail";
			}

			@Override
			public String bulletIcon() {
				return "bullet_circle.bmp";
			}

			@Override
			public int performAction(Context context) {
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("voicemail:"));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				return InternalApp.BUTTON_USED;
			}
			
		});
		
		final String[] projection = new String[] {
				ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.HAS_PHONE_NUMBER,
				};

		Cursor people = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, "starred=?", new String[] {"1"}, ContactsContract.Contacts.TIMES_CONTACTED + " DESC");
		
		while (people.moveToNext()) {
			int idFieldIndex = people.getColumnIndex(ContactsContract.Contacts._ID);
			final String id = people.getString(idFieldIndex);
			
			int nameFieldIndex = people.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			final String contact = people.getString(nameFieldIndex);
			
			int hasNumberFieldIndex = people.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
			if(Integer.parseInt(people.getString(hasNumberFieldIndex)) >0 ) {
			
				Cursor personNumber = context.getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID +"=?", new String[] {id}, null);
				
				while (personNumber.moveToNext()) {
				
					int phoneFieldIndex = personNumber.getColumnIndex(Phone.DATA);
					final String number = personNumber.getString(phoneFieldIndex);
					
					// TODO: Turn this into a string
					int typeFieldIndex = personNumber.getColumnIndex(Phone.TYPE);
					final String type = personNumber.getString(typeFieldIndex);
					
					subActions.add(new Action(){
		
						String title = contact + " (" +type +")" +"\n" + number;
						String uri = "tel:" + number;
						
						@Override
						public String getName() {
							return title;
						}
		
						@Override
						public String bulletIcon() {
							return "bullet_circle.bmp";
						}
		
						@Override
						public int performAction(Context context) {
							Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(intent);
							return InternalApp.BUTTON_USED;
						}
						
					});
				}
				
			}

		}
		
		people.close();
	}

}
