package org.metawatch.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.metawatch.manager.MetaWatchService.Preferences;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ThemePicker extends ListActivity {
	
	public class ThemeData {
		public String name;
		public String description;

		public Bitmap bitmap;
	}
	
	private List<ThemeData> themeList;

    private static class EfficientAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<ThemeData> mThemes;

        public EfficientAdapter(Context context, List<ThemeData> themes) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);
            mThemes = themes;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return mThemes.size();
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficient to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         *
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         *
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view to hold each row.
         *
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_theme, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            String desc = mThemes.get(position).description;
            if (mThemes.get(position).name.equalsIgnoreCase(Preferences.themeName)) {
            	desc += " (current)";
            }
            holder.text.setText(desc);
            if(mThemes.get(position).bitmap!=null)
            	holder.icon.setImageBitmap(mThemes.get(position).bitmap);

            return convertView;
        }

        static class ViewHolder {
            TextView text;
            ImageView icon;
        }
    }
    
    private static Comparator<ThemeData> COMPARATOR = new Comparator<ThemeData>()
    {
	// This is where the sorting happens.
        public int compare(ThemeData o1, ThemeData o2)
        {
            return o1.name.compareTo(o2.name);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            
        //Map<String,WidgetData> widgetMap = WidgetManager.refreshWidgets(this, null);
        themeList = new ArrayList<ThemeData>();
        
        ThemeData defaultTheme = new ThemeData();
        defaultTheme.name = "";
        defaultTheme.description = "MetaWatch 'rm' default theme";
        defaultTheme.bitmap = BitmapCache.getDefaultThemeBanner(this);
        
        themeList.add(defaultTheme);
        
        File searchDir = Utils.getExternalFilesDir(this, "Themes");
        
        File[] themeFiles = searchDir.listFiles();

		for (File file : themeFiles) {
			String themeName = file.getName().replace(".zip", "");
			
			if (Preferences.logging) Log.d(MetaWatch.TAG, "Found theme "+themeName);
			
			Bitmap themeBanner = BitmapCache.getThemeBanner(this, themeName);
		
			ThemeData theme = new ThemeData();
			theme.name = themeName;
			theme.description = themeName;
			theme.bitmap = themeBanner;
			
			themeList.add(theme);
		}
        
               
        Collections.sort(themeList, COMPARATOR);
        
        if (Preferences.logging) Log.d(MetaWatch.TAG, "Showing " +themeList.size() + " themes");
        
        setListAdapter(new EfficientAdapter(this, themeList));
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Intent result = new Intent();
    	
    	Preferences.themeName = themeList.get(position).name;   	
    	MetaWatchService.saveTheme(this, Preferences.themeName);
    	
    	if (Preferences.logging) Log.d(MetaWatch.TAG, "Selected theme '"+Preferences.themeName+"'");
    	
    	setResult(Activity.RESULT_OK, result);

    	super.onListItemClick(l, v, position, id);
    	
    	ProgressDialog dialog = ProgressDialog.show(this, "", 
                "Applying Theme. Please wait...", true);
    	Idle.updateIdle(this, true);
    	dialog.dismiss();
    	
    	finish();
    }

}
