package my.edu.mmu.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class SecureWiFiActivity extends Activity implements TextWatcher, OnCheckedChangeListener {

	private EditText editTextUsername;
	private EditText editTextPassword;
	private CheckBox checkBoxToast;
	private String username;
	private String password;
	private boolean toast;
	private WifiManager mgr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main);
        
		mgr = (WifiManager)(getSystemService(Context.WIFI_SERVICE));
        
        editTextUsername = (EditText)findViewById(R.id.editTextUsername);
        editTextPassword = (EditText)findViewById(R.id.editTextPassword);
        checkBoxToast = (CheckBox)findViewById(R.id.checkBoxToast);
        
        editTextUsername.addTextChangedListener(this);
        editTextPassword.addTextChangedListener(this);
        checkBoxToast.setOnCheckedChangeListener(this);
        
        Button buttonConnect = (Button)findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				WifiInfo info;
				
				// stop the checking task if it is running
				Intent i = new Intent(SecureWiFiActivity.this, SecureWiFiService.class);
				i.putExtra("mode", 1);
				startService(i);
				
				if(!mgr.isWifiEnabled()) {
					new AlertDialog.Builder(SecureWiFiActivity.this)
					.setTitle("WiFi is off")
					.setMessage("Please turn on your WiFi")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					})
					.show();
				} else {
					info = mgr.getConnectionInfo();
					if(info == null) {
						Toast.makeText(SecureWiFiActivity.this, "No WiFi connection, please try again later", Toast.LENGTH_SHORT).show();
					} else {
						if(info.getSSID() == null) {
							Toast.makeText(SecureWiFiActivity.this, "No WiFi connection, please try again later", Toast.LENGTH_SHORT).show();							
						} else {
							Log.d("WiFi", info.getSSID());							
							if(Build.VERSION.SDK_INT >= 17) {
								if(info.getSSID().equals("\"MMU\"")) {
									i = new Intent(SecureWiFiActivity.this, SecureWiFiService.class);
									i.putExtra("mode", 0);
									startService(i);
								} else {
									Toast.makeText(SecureWiFiActivity.this, "Connected to other WiFi", Toast.LENGTH_SHORT).show();
								}
							} else {
								if(info.getSSID().equals("MMU")) {
									i = new Intent(SecureWiFiActivity.this, SecureWiFiService.class);
									i.putExtra("mode", 0);
									startService(i);
								} else {
									Toast.makeText(SecureWiFiActivity.this, "Connected to other WiFi", Toast.LENGTH_SHORT).show();
								}								
							}
						}
					}
				}
			}
		}); 
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		username = pref.getString("username", "");
		password = pref.getString("password", "");
		toast = pref.getBoolean("toast", true);
		editTextUsername.setText(username);
		editTextPassword.setText(password);
		checkBoxToast.setChecked(toast);
	}

	private void changePreference() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = pref.edit();
		editor.putString("username", editTextUsername.getText().toString());
		editor.putString("password", editTextPassword.getText().toString());
		editor.putBoolean("toast", checkBoxToast.isChecked());
		editor.commit();
	}

	
	@Override
	public void afterTextChanged(Editable s) {
		changePreference();	
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		changePreference();			
	}
    
}