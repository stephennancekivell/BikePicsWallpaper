package org.snancekivell.Bike_Pics_Wallpaper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class Changer extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		
		try{
			site_utils.update(this);
		}
		catch(Exception e){
			Log.e(constants.LOG_TAG, "Changer: "+e.toString());
		}
		finally{
			Changer.start_alarm_once(this);
		}
		this.stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// doesnt happen
		return null;
	}
	
	public static Intent get_intent(Context c){
		return new Intent(c, Changer.class);
	}
	
	private static PendingIntent getPendingIntent(Context context) {
		return PendingIntent.getService(context,
                0, get_intent(context), 0);
	}

	public static void stop_periodic(Context context, AlarmManager am){
		am.cancel(getPendingIntent(context));
	}
	public static void stop_periodic(Context context){
		AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
		stop_periodic(context, am);
	}
	
	public static void start_alarm_once(Context context){
		AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
		stop_periodic(context, am); // would be dumb if it ended up running twice.
		
		long cTime = SystemClock.elapsedRealtime();
		
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, cTime, constants.s_update_interval,
					getPendingIntent(context));
		
		// seem to not wake up properly, or maybe if it fails because it was asleep it wont retry.
		//am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, cTime+constants.s_update_interval,
			//	getPendingIntent(context));
		
		
	}
}
