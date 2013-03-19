package my.edu.mmu.wifi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SecureWiFiService extends Service {

	private TrustManager[] trustAllCerts;
	public boolean toast = false;
	private SharedPreferences pref;
	private String username;
	private String password;
	private boolean isChecking = false;
	private WifiInfo info;
	private WifiManager mgr;
	private boolean shouldStop = true;

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d("MMUWiFi", "MMU WiFi Service created");
		
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		mgr = (WifiManager)(getSystemService(Context.WIFI_SERVICE));
		
		trustAllCerts = new TrustManager[] {

			new X509TrustManager() {
	
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
	
				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
	
				}
	
				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
	
				}
				
			} 
		};
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		int mode = 0;
		
		if(intent!=null) {
			mode = intent.getIntExtra("mode", 0);
		}
		
		Log.d("MMUWiFi", "Receive Start Command mode " + mode);
		
		switch(mode) {
		case 0:
			username = pref.getString("username", "");
			password = pref.getString("password", "");
			toast = pref.getBoolean("toast", true);

			info = mgr.getConnectionInfo();
			if(info!=null) {
				if(info.getSSID()!=null) {
					if(Build.VERSION.SDK_INT >= 17) {
						if(info.getSSID().equals("\"MMU\"")) {
							if(!isChecking) {
								new CheckTask().execute();
								isChecking = true;
							}
						} 	
					} else {
						if(info.getSSID().equals("MMU")) {
							if(!isChecking) {
								new CheckTask().execute();
								isChecking = true;
							}
						} 							
					}
				}
			} 
			break;
		case 1:
			shouldStop = true;
			stopSelf();
			break;
		}

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	class CheckTask extends AsyncTask<Void, Void, Boolean> {
				
		boolean canPing = false;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			shouldStop = false;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			Log.d("MMUWiFi", "Pinging wifi.mmu.edu.my ...");

			int count = 0;
			
			while(!canPing) {
				try {
					if(shouldStop) {
						return canPing;
					}
					Thread.sleep(250);
					count++;
					if(count > 50) {
						return canPing;
					}
					SSLContext sc = SSLContext.getInstance("TLS");
					sc.init(null, trustAllCerts, new SecureRandom());
					
					HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
					HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
						
						@Override
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					}); 					
					URL url = new URL("https://wifi.mmu.edu.my/login.html");
					info = mgr.getConnectionInfo();
					if(info!=null) {
						if(info.getSSID()!=null) {
							if(Build.VERSION.SDK_INT >= 17) {
								if(info.getSSID().equals("\"MMU\"")) {
									URLConnection conn = url.openConnection();
									conn.getInputStream();
									canPing = true;
									shouldStop = true;
								} else {							
									return canPing;
								}
							} else {
								if(info.getSSID().equals("MMU")) {
									URLConnection conn = url.openConnection();
									conn.getInputStream();
									canPing = true;
									shouldStop = true;
								} else {							
									return canPing;
								}
							}
						} else {
							return canPing;
						}
					} else {
						return canPing;
					}
					Log.d("MMUWiFi", "count: " + count);
				} catch (Exception e) {
					Log.e("MMUWiFi", e.toString());
				}
			}			
			return canPing;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			isChecking = false;
			if(result) {
				new SendTask().execute();
			} else {
				Log.d("MMUWiFi", "MMU WiFi Server not found");
				if(toast) {
					Toast.makeText(SecureWiFiService.this, "Can not connect to MMU WiFi Server", Toast.LENGTH_LONG).show();						
				}
				stopSelf();
			}
		}
	}

	class SendTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... arg0) {
			Log.d("MMUWiFi", "HTTP Post to MMU WiFi");

			try {
				// Construct data
				String data = URLEncoder.encode("username", "UTF-8") + "="
						+ URLEncoder.encode(username, "UTF-8");
				data += "&" + URLEncoder.encode("password", "UTF-8") + "="
						+ URLEncoder.encode(password, "UTF-8");
				data += "&" + URLEncoder.encode("buttonClicked", "UTF-8") + "="
						+ URLEncoder.encode("4", "UTF-8");

				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, trustAllCerts, new SecureRandom());

				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
					
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				}); 
				
				 URL url = new URL("https://wifi.mmu.edu.my/login.html");
				 info = mgr.getConnectionInfo();
				 if(info!=null) {
					 if(info.getSSID()!=null) {
							if(Build.VERSION.SDK_INT >= 17) {
								if(info.getSSID().equals("\"MMU\"")) {
									URLConnection conn = url.openConnection();
									conn.setDoOutput(true);
									OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
									    
									// Send data
									writer.write(data);
									writer.flush();

									// Get response
									BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
									String str;
									StringBuilder results = new StringBuilder();
									while ((str = reader.readLine()) != null) {
										results.append(str + "\n");
									}
									writer.close();
									reader.close();
									return results.toString();
								}   
							} else {
								if(info.getSSID().equals("MMU")) {
									URLConnection conn = url.openConnection();
									conn.setDoOutput(true);
									OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
									    
									// Send data
									writer.write(data);
									writer.flush();

									// Get response
									BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
									String str;
									StringBuilder results = new StringBuilder();
									while ((str = reader.readLine()) != null) {
										results.append(str + "\n");
									}
									writer.close();
									reader.close();
									return results.toString();
								}   
								
							}
					 }
				 } 
			} catch (Exception e) {
				Log.e("MMUWiFi", e.toString());
			}
			return "failed";
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d("MMUWiFi", result.toString());

			if(toast) {
				if(result.equals("failed")) {
					Toast.makeText(SecureWiFiService.this, "Failed to connect to MMU WiFi", Toast.LENGTH_LONG).show();					
				} else {
					if(result.contains("Login Successful")) {
						Log.d("MMUWiFi", "Login Successful");
						Toast.makeText(SecureWiFiService.this, "Login Successful to MMU WiFi", Toast.LENGTH_LONG).show();						
					} else if(result.contains("You are already logged in")) {
						Log.d("MMUWiFi", "Already Logged In");
						Toast.makeText(SecureWiFiService.this, "Already Logged In to MMU WiFi", Toast.LENGTH_LONG).show();	
					} else if(result.contains("The User Name and Password combination you have entered is invalid")) {
						Log.d("MMUWiFi", "Invalid Username/Password");
						Toast.makeText(SecureWiFiService.this, "Invalid Username/Password for MMU WiFi", Toast.LENGTH_LONG).show();		
					} else {
						Log.d("MMUWiFi", "Failed to Login");
						Toast.makeText(SecureWiFiService.this, "Failed to Login to MMU WiFi", Toast.LENGTH_LONG).show();	
					}
				}				
			}
			stopSelf();
		}
	}

}
