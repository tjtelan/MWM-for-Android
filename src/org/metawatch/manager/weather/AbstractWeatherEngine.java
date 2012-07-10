package org.metawatch.manager.weather;

import org.metawatch.manager.MetaWatch;
import org.metawatch.manager.MetaWatchService.Preferences;

import android.util.Log;

public abstract class AbstractWeatherEngine implements WeatherEngine {

	private final static int TIME_FIVE_MINUTES = 5 * 60 * 1000;

	/**
	 * Weather update is done frequently. Update rate can be configured here.
	 * 
	 * @param data
	 *            Weather data
	 * @return True if weather data shall be updated
	 */
	public boolean isUpdateRequired(WeatherData data) {
		if (data.timeStamp > 0 && data.received) {
			long currentTime = System.currentTimeMillis();
			long diff = currentTime - data.timeStamp;

			if (diff < TIME_FIVE_MINUTES) {
				if (Preferences.logging)
					Log.d(MetaWatch.TAG,
							"Skipping weather update - updated less than 5m ago");

				return false;
			}
		}

		return true;
	}

}
