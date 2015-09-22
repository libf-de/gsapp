package de.xorg.gsapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Html;
import android.text.Html.TagHandler;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ServerMessageHandler extends AsyncTask<String, Void, String> {
		Context context;
		int statusCode;
		String TAG = "GSApp ServerMessageHandler";
		public final static String EXTRA_URL = "de.xorg.gsapp.MESSAGE";
		public final static String EXTRA_NAME = "de.xorg.gsapp.MESSAGENAME";
		public ServerMessageHandler(Context context) {
			this.context = context;
		}
		
		@Override
		protected String doInBackground(String... uri) {
			DefaultHttpClient client = new DefaultHttpClient();
			String responseBody = null;
			HttpResponse httpResponse = null;
			ByteArrayOutputStream out = null;
			
			try {
				HttpGet httpget = new HttpGet("http://thexorg.tk/gsapp/notifyG.json"); 
				httpResponse = client.execute(httpget);
				statusCode = httpResponse.getStatusLine().getStatusCode();
				
				if(statusCode == HttpStatus.SC_OK){
					out = new ByteArrayOutputStream();
					httpResponse.getEntity().writeTo(out);
					responseBody = out.toString();
				}
				
			}catch (Exception e) {
				Log.e(TAG, e.toString());
			} finally {
				if(out != null){
					try {
						out.close();
					} catch (IOException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
			return responseBody;
		}

		@Override
		protected void onPostExecute(String result) {
			String content = null;
			String title = null;
			String uris = null;
			String urit = null;
			int messageId;
			if(statusCode != HttpStatus.SC_OK){
				Log.e(TAG, "Response invalid. status code=" + statusCode);
			}else{
				if(!result.startsWith("{")){ // for response who append with unknown char
					 result= result.substring(1);
			    }
				try{
					// json format from server: 
					JSONObject json = (JSONObject)new JSONTokener(result).nextValue();
					messageId = json.optInt("msg_id");
					content = json.optString("content");
					title = json.optString("title");
					
					int lastRID = getRID();
					if(lastRID < messageId){
						    final int newRID = messageId;
						    final String hrl = uris;
							AlertDialog.Builder builder = new AlertDialog.Builder(context);
							
							if(title.equals("")) {
								builder.setTitle("Servernachricht erhalten");
							} else {
								builder.setTitle(title);
							}
							
							builder.setMessage(Html.fromHtml(content, null, new CustomTagHandler()));
							
							builder.setNeutralButton("Okay", new OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									saveRID(newRID);
									arg0.dismiss();
								}
								
							});
							
							builder.setCancelable(false);

					        
					        AlertDialog dialog = builder.create();
					        dialog.show();
					}
				}catch (JSONException e){
					Log.e(TAG, "is your server response have valid json format?");
				}catch(Exception e) {
					Log.e(TAG, e.toString());
				} 
			}
	    }
		
		public int getRID() {
			return PreferenceManager.getDefaultSharedPreferences(context).getInt("lastMsgId", 0);
		}
		
		public void saveRID(int id) {
			Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putInt("lastMsgId", id);
	        editor.commit();
		}
}

class CustomTagHandler implements TagHandler{

	@Override
	public void handleTag(boolean opening, String tag, Editable output,
			XMLReader xmlReader) {
		// you may add more tag handler which are not supported by android here
		if("li".equals(tag)){
			if(opening){
				output.append(" · ");
			}else{
				output.append("\n");
			}
		}
	}
}
