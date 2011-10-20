package org.snancekivell.Bike_Pics_Wallpaper;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

public class constants {
	
	//constants
	public static final String IMGfile = "IMGfile.jpeg";
	public static final long last_update_threshold = 30000;//time in milliseconds
	public static final boolean s_change_periodic_default = true;
	public static final String LOG_TAG = "Bike Pics Wallpaper";
	public static final long s_update_interval = AlarmManager.INTERVAL_HOUR*2;
	public static final String Unknown_host_exception_msg = "Unable to connect to Bikepics.com";

	//SharedPreferences
	public static final String s_change_periodic = "change_periodic";
	public static final String s_last_update_time = "s_last_update_time";
	
	public static SharedPreferences get_prefs(Context context){
		return context.getSharedPreferences("bike pics wallpaper settings", Context.MODE_PRIVATE);
	}
}