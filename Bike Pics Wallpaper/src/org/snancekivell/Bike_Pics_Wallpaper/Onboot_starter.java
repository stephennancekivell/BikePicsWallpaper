package org.snancekivell.Bike_Pics_Wallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class Onboot_starter extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// just make sure we are getting the right intent (better safe than sorry)
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ==false){
			Log.w(constants.LOG_TAG, "onboot, incorrect intent");
			return;
		}
		SharedPreferences prefs = constants.get_prefs(context);
		if (prefs.getBoolean(constants.s_change_periodic, constants.s_change_periodic_default))
			Changer.start_alarm_once(context);
	}
}