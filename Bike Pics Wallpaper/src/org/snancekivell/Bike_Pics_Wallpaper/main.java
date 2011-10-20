package org.snancekivell.Bike_Pics_Wallpaper;

import java.io.File;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class main extends Activity implements OnClickListener, OnCheckedChangeListener {
    private ProgressBar mProgress;
    private Handler mHandler = new Handler();
    Button set_now;
    Context mcontext;
    Boolean auto_update;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final SharedPreferences prefs = constants.get_prefs(this);
        set_now = (Button) findViewById( R.id.m_set_wallpaper);
        set_now.setOnClickListener(this);
        if (prefs.getBoolean(constants.s_change_periodic, constants.s_change_periodic_default)){
        	set_now.setVisibility(View.GONE);
        }
        Button rate = (Button) findViewById(R.id.m_rate);
        rate.setOnClickListener(this);
        
        CheckBox check_periodic = (CheckBox) findViewById(R.id.m_change_periodic_checkbox);
        auto_update = prefs.getBoolean(constants.s_change_periodic, constants.s_change_periodic_default);
		check_periodic.setChecked(auto_update);
        check_periodic.setOnCheckedChangeListener(this);
        
        ImageButton bp_site = (ImageButton) findViewById(R.id.m_bp_site);
        bp_site.setOnClickListener(this);
        
        final ImageView imagePreview = (ImageView) findViewById(R.id.m_preview_image);
        imagePreview.setVisibility(ImageView.GONE);
        
        mProgress = (ProgressBar) findViewById(R.id.m_progress);
        
        mcontext = this.getBaseContext();
        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
            	mProgress.setProgress(0);
            	
            	try {
					site_utils.update(mcontext);
				} catch(final Exception e){
					mHandler.post(new Runnable() {
	                     public void run() {
	                    	 display_error(e);
	                     }
	                 });
				} finally{
					if (auto_update) Changer.start_alarm_once(mcontext);
				}
				            	
            	 mHandler.post(new Runnable() {
                     public void run() {
                    	 Changer.start_alarm_once(mcontext);
                    	 image_preview();
                    	 mProgress.setVisibility(ProgressBar.GONE);
                    	 imagePreview.setVisibility(ImageView.VISIBLE);
                     }
                 });
            }
        }).start();
    }
	
	private void display_error(Exception e){
		Log.e(constants.LOG_TAG, "de:"+e.toString());
		
		String msg =null;
		if (e.getClass()==UnknownHostException.class)
			msg = constants.Unknown_host_exception_msg;
		else
			msg = "Error updating";
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	private void image_preview(){
		ImageView imagePreview = (ImageView) findViewById(R.id.m_preview_image);
        try{
	        File f = getFileStreamPath(constants.IMGfile);
	        Drawable d = Drawable.createFromPath(f.getAbsolutePath());
	        imagePreview.setImageDrawable(d);
        }catch(Exception e){
        	Log.e(constants.LOG_TAG, "image_preview: "+e.toString());
        }
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.m_set_wallpaper:
			startService(Changer.get_intent(this));			
			break;
		case R.id.m_bp_site:
			Uri u = Uri.parse(site_utils.site_address);
			Intent i = new Intent(Intent.ACTION_VIEW, u);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			break;
		case R.id.m_rate:
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.snancekivell.Bike_Pics_Wallpaper"));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try{
				startActivity(intent);
			} catch (ActivityNotFoundException e){
				Toast.makeText(this, "Cannot start android market, activity not found", Toast.LENGTH_LONG).show();
			}
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Editor editor = constants.get_prefs(this).edit();
		switch (buttonView.getId()){
			case R.id.m_change_periodic_checkbox:
				editor.putBoolean(constants.s_change_periodic, isChecked).commit();
				auto_update = isChecked;
				if (isChecked){
					Changer.start_alarm_once(this);
					set_now.setVisibility(View.GONE);
				}
				else{
					Changer.stop_periodic(this);
					set_now.setVisibility(View.VISIBLE);
				}
				break;
		}
	}
}