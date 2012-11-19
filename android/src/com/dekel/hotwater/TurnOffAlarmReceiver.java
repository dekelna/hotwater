package com.dekel.hotwater;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

public class TurnOffAlarmReceiver extends BroadcastReceiver {
	private static final int NOTIFICATION_ID = 0x1234;
	private static final int ERROR_NOTIFICATION_ID = 0x5678;
	private static final int TIME_TO_SLEEP = 5 * 1000; // ms
	private static final int MAX_ATTEMPS = 30;
	
	Client client = new Client();
	
	public void onReceive(final Context context, Intent intent) {
		Logger.log("Timer expired - Turning off!");

		new AsyncTask<Void, Void, Void>() {
			boolean success = false;
			protected Void doInBackground(Void... params) {
				for (int i = 0; i < MAX_ATTEMPS; i++) {
					try {
						client.postNewState(false);
						success = true;
						return null;
					} catch (IOException e) {
						Logger.log("Warning - Couldn't turn off! attempt " + i);
						e.printStackTrace();
						try {
							Thread.sleep(TIME_TO_SLEEP);
						} catch (InterruptedException e1) {
						}
					}
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				if (success) {
					if (MainActivity.isForeground()) {
						context.startActivity(new Intent(context, MainActivity.class)
							.putExtra(MainActivity.TURN_OFF_INTENT, true)
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
					} else {
						Logger.log("Showing notification!");
						notify(context, "Hotwater", "boiler has been turned off after 30 mins!", R.drawable.ic_launcher, NOTIFICATION_ID);
					}
				} else {
					Logger.log("ERROR - Couldn't turn off!");
					notify(context, "Hotwater", "has FAILED to turn off boiler!", R.drawable.ic_launcher, ERROR_NOTIFICATION_ID);
				}					
				super.onPostExecute(result);
			}

			private void notify(final Context context, String title, String msg, int icon, int notificationId) {
				Notification notification = new NotificationCompat.Builder(context)
					.setContentTitle(title)
					.setContentText(msg)
					.setSmallIcon(icon)
					.setDefaults(Notification.DEFAULT_ALL)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setAutoCancel(true)
					.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0))
					.build();
				((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(notificationId, notification);
			}
		}.execute((Void) null);
	}
}
