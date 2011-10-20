package org.snancekivell.Bike_Pics_Wallpaper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class site_utils {
	public static final String site_address = "http://bikepics.com/";

	public static final String regex_img_src_token = "src=\"http://[\\d\\w\\.\\-\\\\/]+\\.jpg\"";
	public static final String regex_img_line = "<a href=\"/pictures/\\d+/\"><img border=\"0\" "+regex_img_src_token+" style=\"border: 1px solid #000000;\" width=\"[\\d\\.]+\" height=\"[\\d\\.]+\"\\s+((Alt=\")|(></a>))";
	//<a href="/pictures/2104017/"><img border="0" src="http://p1.bikepics.com/pics/2010\11\25\bikepics-2104017-800.jpg" style="border: 1px solid #000000;" width="800" height="531" ></a>
	
	public static final String regex_full_href_token = "href=\"[:\\d\\w\\.\\-\\\\/]+\"";

	public static void update(Context context) throws NoLinkInLineException, NoImgLineException, UnknownHostException{
		//update the image in the cache, if wanted update wallpaper
		String pic_line = get_pic_line(context);
		
		String addr = site_utils.address_from_line(context,pic_line);
	
		SharedPreferences prefs = constants.get_prefs(context);
		long cTime = System.currentTimeMillis();
		long last_update = prefs.getLong(constants.s_last_update_time, 0);
		cTime = cTime-last_update;
		
		// dont update if have very recently updated
		//cTime could be less that 0 if the phone time travelled backward.
		if (cTime < constants.last_update_threshold || cTime < 0) {
			Log.i(constants.LOG_TAG, "update - skipping");
			return; // dont do anything.
		}
		
		try{
			site_utils.get_pic_to_cache(context, addr);
		} catch(IOException e)
		{
			Log.e(constants.LOG_TAG, "get_pic_to_cache E: "+e.toString());
			return;
		}
		catch(URISyntaxException e){
			Log.e(constants.LOG_TAG, "get_pic_to_cache E: "+e.toString());
			return;
		}
		
		if (prefs.getBoolean(constants.s_change_periodic, constants.s_change_periodic_default)){
    		set_pic_from_cache(context);
			//pic might already be wallpaper but no way to tell if it still is.
		}
    	Editor edit = prefs.edit();
    	edit.putLong(constants.s_last_update_time, System.currentTimeMillis());
    	edit.commit();
    	Log.i(constants.LOG_TAG, "update");
	}
	
	private static String get_pic_line(Context context) throws UnknownHostException, NoImgLineException{
		String line = null;
		try{
			HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(site_address));
            HttpResponse response = client.execute(request);
            InputStream is = response.getEntity().getContent();
            line = get_pic_line_is(context, is, regex_img_line);
            is.close();
		}
		catch(UnknownHostException e) {throw e;}
		catch(Exception e){
			Log.e(constants.LOG_TAG, "get_pic_line_addr: "+e.toString());
			throw new NoImgLineException();
		}
		return line;
	}
	public static String get_pic_line_is(Context context, InputStream is, String regex) throws NoImgLineException{
		String line=null;
		Pattern line_p = Pattern.compile(regex);
		BufferedReader reader=null;
		try{
		reader = new BufferedReader (new InputStreamReader(is));
	            String line_i;
	            while ((line_i = reader.readLine()) != null) {
	            	Matcher line_m = line_p.matcher(line_i);
	            	if (line_m.find()){
	            		line = line_i;
	            		break;
	            	}
	            }
	            if (reader != null) reader.close();
		} catch(IOException e){
			Log.e(constants.LOG_TAG, "get_pic_line_is"+e.toString());
		}
		if (line==null) throw new NoImgLineException(); //if line was never set to line_i or IOException
		
		return line;
	}
	
	private static String address_from_line(Context context,String line) throws UnknownHostException, NoLinkInLineException{
		Pattern pic_p = Pattern.compile(regex_img_src_token);
		String addr = null;
		Matcher m = pic_p.matcher(line);
		
		try{
			if (m.find() == false) throw new NoLinkInLineException();
			addr = m.group();
			addr = addr.substring(5, addr.lastIndexOf("\""));
		
		} catch(Exception e){
			Log.e(constants.LOG_TAG, "get_pic_address: "+e.toString());
			throw new NoLinkInLineException();
		}
		return addr;
	}
	
	public static void get_pic_to_cache(Context context, String addr) throws IOException, URISyntaxException{
		HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        	// lame, URI cant handle '\'
        addr = addr.replace("\\", "/");
        request.setURI(new URI(addr));
        HttpResponse response = client.execute(request);
        
        InputStream is = response.getEntity().getContent();
        FileOutputStream fos = context.openFileOutput(constants.IMGfile, Context.MODE_PRIVATE);
        
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        BufferedInputStream bis = new BufferedInputStream(is);
        int b;
        while ((b=bis.read())!=-1){
        	bos.write((byte)b);
        }
        bos.flush();
        bos.close();
        bis.close();
        fos.flush();
        fos.close();
	}
	private static void set_pic_from_cache(Context context){
		try{
			FileInputStream fis = context.openFileInput(constants.IMGfile);
			WallpaperManager wm = WallpaperManager.getInstance(context);
	        wm.setStream(fis);
			fis.close();
		}
		catch (Exception e){
			Log.e(constants.LOG_TAG, "set_pic_from_cache: "+e.toString());
		}
	}
}
