package my.edu.mmu.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

public class WiFiConnectedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d("MMUWiFi", "Receiving Intent for SUPPLICANT_STATE_CHANGED_ACTION");	
		Log.d("MMUWiFi", "supplicant state: " + intent.getParcelableExtra("newState").toString());	
		if(intent.getParcelableExtra("newState").toString().equals("COMPLETED")) {
			WifiManager mgr = (WifiManager)(context.getSystemService(Context.WIFI_SERVICE));
			WifiInfo info = mgr.getConnectionInfo();
			if(info!=null) {
				Log.d("MMUWiFi", info.toString());
				if(info.getSSID() != null) {
					Log.d("MMUWiFi", "SSID:" + info.getSSID());
					if(Build.VERSION.SDK_INT >= 17) {
						if(info.getSSID().equals("\"MMU\"")) {					
							Intent i = new Intent(context, SecureWiFiService.class);
							i.putExtra("mode", 0);
							context.startService(i);
						} else {
							Log.d("MMUWiFi", "Not connected to MMU WiFi");						
						}					
					} else {
						if(info.getSSID().equals("MMU")) {					
							Intent i = new Intent(context, SecureWiFiService.class);
							i.putExtra("mode", 0);
							context.startService(i);
						} else {
							Log.d("MMUWiFi", "Not connected to MMU WiFi");						
						}
						
					}
				} else {
					Log.d("MMUWiFi", "Null SSID");
				}
			} else {
				Log.d("MMUWiFi", "Null WiFi Info");
			}	
		} else if(intent.getParcelableExtra("newState").toString().equals("DISCONNECTED")) {
			Intent i = new Intent(context, SecureWiFiService.class);
			i.putExtra("mode", 1);
			context.startService(i);
		}
	}

}
