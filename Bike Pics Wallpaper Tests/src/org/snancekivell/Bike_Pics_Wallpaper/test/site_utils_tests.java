package org.snancekivell.Bike_Pics_Wallpaper.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.snancekivell.Bike_Pics_Wallpaper.constants;
import org.snancekivell.Bike_Pics_Wallpaper.site_utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.PowerManager;
import android.test.InstrumentationTestCase;
import android.util.Log;

public class site_utils_tests extends  InstrumentationTestCase{
	
	public void  test_get_pic_line() throws Throwable{
		Context context = this.getInstrumentation().getContext();
		Resources resources = context.getResources();
		
		int fail=0;
		int res_start = R.raw.index1033;
		
		boolean result = process_test_res_pic_line(R.raw.index_2010_12_131, resources, context);
		
		
		for(int res=res_start; res <= R.raw.index1180; res++){
			if (false== process_test_res_pic_line(res, resources, context))
				fail+=1;
		}
		for(int res=R.raw.index_2010_12_04; res <=R.raw.index_2011_01_23; res++ ){
			if (false== process_test_res_pic_line(res, resources, context)) 
				fail+=1;
		}
		
		
		assert(fail==0);
		Log.i(org.snancekivell.Bike_Pics_Wallpaper.constants.LOG_TAG, "num failed regex tests:"+fail);
	}
	
	private boolean process_test_res_pic_line(int res, Resources resources, Context context){
		
		InputStream is = resources.openRawResource(res);
		
		String line="";
		try{
			line = site_utils.get_pic_line_is(context, is, site_utils.regex_img_line);
			is.close();
		}
		catch(Exception e){
			Log.e(constants.LOG_TAG, "res:"+(res)+" "+e.toString());
			return false;
		}
		//<a href="/pictures/2107735/"><img border="0" src="http://p1.bikepics.com/pics/2010\12\02\bikepics-2107735-800.jpg" style="border: 1px solid #000000;" width="800" height="448" ></a>
		
		//Log.i(constants.LOG_TAG, line);
		
		assert(line.contains("<a href=\"/pictures"));
		assert(line.contains("<img border=\"0\" src=\"http://"));
		assert(line.contains("style=\"border: 1px solid #000000;\" width="));
		return true;
	}
	
	
	public void test_get_pic_to_cache() throws Exception{
		Context context = this.getInstrumentation().getTargetContext();
		String addr = "http://p1.bikepics.com/pics/2010\\12\\02\\bikepics-2107735-800.jpg";
		
		//use method to put img in cache.
		site_utils.get_pic_to_cache(context, addr);
		
		//check cache file is same as source.
		
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        	// lame, URI cant handle '\'
        addr = addr.replace("\\", "/");
        request.setURI(new URI(addr));
        HttpResponse response = client.execute(request);
        InputStream web_is = response.getEntity().getContent();
        BufferedInputStream web_bis = new BufferedInputStream(web_is);
        
        FileInputStream cache_fis = context.openFileInput(constants.IMGfile);
        BufferedInputStream cache_bis = new BufferedInputStream(cache_fis);
        
        int w,c;
        while(true){
        	w = web_bis.read();
        	c = cache_bis.read();
        	
        	if (w==-1 & c==-1) break;
        	if (w==-1) throw new Exception("cache bigger than source");
        	if (c==-1) throw new Exception("cache smaller than source");
        	if (c!=w) throw new Exception("cache content different to source");
        }
        
        cache_bis.close();
        web_bis.close();
	}


	public void test_set_pic_addr() throws Exception{
		Context context = this.getInstrumentation().getTargetContext();
		String a_pic = "http://p1.bikepics.com/pics/2010\\12\\02\\bikepics-2107735-800.jpg";
		SharedPreferences prefs = constants.get_prefs(context);
		Editor editor = prefs.edit();
		String last_addr = prefs.getString(constants.s_last_addr, "not");
		editor.putString(constants.s_last_addr, last_addr+"not").commit();
		
		site_utils.set_pic_addr(context, a_pic); // This will force it to get a new one & put it in the cache
		
		//do it again this time it will match s_last_addr and get it from the cache.
		site_utils.set_pic_addr(context, a_pic);
		
		//cause a IOException with the cache then get it.
		context.deleteFile(constants.IMGfile);
		site_utils.set_pic_addr(context, a_pic);
		
	}
}
