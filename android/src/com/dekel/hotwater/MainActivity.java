package com.dekel.hotwater;

import java.io.IOException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String TURN_OFF_INTENT = "turn_off";
	
	private static boolean isForeground = false;
	Client client = new Client();
	
	public static boolean isForeground() {
		Logger.log("isForeground=" + isForeground);
		return isForeground;
	}
	
    public void onCreate(Bundle savedInstanceState) {
    	Logger.log("Started.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    protected void onResume() {
    	Logger.log("Started.");
    	isForeground = true;
    	super.onResume();
    	updateCurrentState();
    }
    
    protected void onPause() {
    	Logger.log("Started.");
    	isForeground = false;
    	super.onPause();
    }
   
    public void onSwitch(View view) {
    	Logger.log("Started.");
    	final boolean newState = ((Switch) view).isChecked();
    	System.out.println("Switched state to [" + (newState ? "on]" : "off]"));

		CheckBox cb = (CheckBox) findViewById(R.id.checkBox1);
		cb.setChecked(newState);
		cb.setVisibility(newState ? 0 : 4);
		handleNewCheckBoxState(newState);
    	
    	new Thread(new Runnable() {
			public void run() {
				try {
					client.postNewState(newState);
				} catch (Exception e) {
					e.printStackTrace();
					// TODO revert state if fails?
				}
			}	
    	}).start();
    }
    
    public void onCheck(View view) {
    	Logger.log("Started.");
    	handleNewCheckBoxState(((CheckBox) view).isChecked());
    }
    
    public void handleNewCheckBoxState(boolean newState) {
    	Logger.log("Setting auto-turn-off [" + (newState ? "on]" : "off]"));
    	
    	Intent i = new Intent(this, TurnOffAlarmReceiver.class);
    	PendingIntent pi = PendingIntent.getBroadcast(this, 0x1234, i, PendingIntent.FLAG_UPDATE_CURRENT);
    	AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
    	
    	if (newState) {
    		 am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Configuration.TURN_OFF_PERIOD, pi);
    	} else {
    		am.cancel(pi);
    	}
    }
    
	private void updateCurrentState() {
    	Logger.log("Started.");		
		
    	final Switch sw = (Switch) findViewById(R.id.switch1);
    	final CheckBox cb = (CheckBox) findViewById(R.id.checkBox1);

		Intent i = new Intent(this, TurnOffAlarmReceiver.class);
		cb.setChecked(PendingIntent.getBroadcast(this, 0x1234, i, PendingIntent.FLAG_NO_CREATE) != null);

		final Context context = this;
		new AsyncTask<Void, Void, Void>() {
			boolean state = false;
			Toast t = Toast.makeText(context, "Status is being updated..", Toast.LENGTH_SHORT);
			protected void onPreExecute() {
				sw.setEnabled(false);
				t.show();
			}
			protected void onProgressUpdate(Void... progress) {
				t.show();
		     }
			
			protected Void doInBackground(Void... params) {
				try {
					state = client.getCurrentState();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				sw.setChecked(state);
				sw.setEnabled(true);
				cb.setVisibility(state ? 0 : 4);
				t.cancel();
				Toast.makeText(context, "Status has been updated.", Toast.LENGTH_SHORT).show();
				super.onPostExecute(result);
			}
		}.execute((Void) null);
		
		
	}	
}
