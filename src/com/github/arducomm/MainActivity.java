package com.github.arducomm;

import java.util.Set;

import com.example.openarduino.R;
import com.example.openarduino.R.id;
import com.example.openarduino.R.layout;
import com.example.openarduino.R.menu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Main activity:
 * - turns BT on/off
 * - show list of paired devices
 * todos:
 * - device pairing
 * - non-bt devices?
 * @author sae762
 *
 */
public class MainActivity extends Activity {

	final String LOG_TAG = "openarduino-main";
	private BluetoothAdapter myBluetoothAdapter;
	private ToggleButton tbBT;
	private Set<BluetoothDevice> pairedDevices;
	private ListView myListView;
	private ArrayAdapter<String> BTArrayAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(myBluetoothAdapter == null) {
		      Toast.makeText(getApplicationContext(),"Your device does not support Bluetooth",
		      Toast.LENGTH_LONG).show();
		      return;
		}
		tbBT = (ToggleButton)findViewById(R.id.toggleBT);
		tbBT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				toggleBT(isChecked);
			}
		});
		if (myBluetoothAdapter.isEnabled()) 
			tbBT.setChecked(true);
        BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		myListView=(ListView)findViewById(R.id.listView1);
        myListView.setAdapter(BTArrayAdapter);	
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	//if bt device clicked
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	String sDevName=(String)myListView.getAdapter().getItem(position);
            	//Log.d(LOG_TAG, "device selected:"+sDevName);
            	startDevice(sDevName);//start device activity (select activity by device name/type)
            }
          });        
		fillPairedList();//fill list if BT on
	    // Register for broadcasts on BluetoothAdapter state change
	    IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	    this.registerReceiver(mReceiver, filter);
	    
		/*
		//exit button - moved to options
		Button btnExit = (Button) findViewById(R.id.btnExit);
	    btnExit.setOnClickListener(new View.OnClickListener() {

	        @Override
	        public void onClick(View v) {
	            finish();
	            System.exit(0);
	        }
	    });
	    */  
	}

	/**
	 * turn BT on/off
	 * @param isChecked
	 */
	public void toggleBT(boolean isChecked) {
		if (myBluetoothAdapter.isEnabled() && !isChecked) {//if bt enabled and button going off
			if (myBluetoothAdapter.disable()) {
				tbBT.setChecked(false);
			}
			return;
  	    }
		if (!myBluetoothAdapter.isEnabled() && isChecked){//if bt disabled and button going on
			if (myBluetoothAdapter.enable()) {
				tbBT.setChecked(true);
				//fillPairedList(); //empty while bt state not changed, moved to receiver 
			}
			return;
		}		
	}

	/**
	 * receiver for BT events 
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                                     BluetoothAdapter.ERROR);
                switch (state) {
                case BluetoothAdapter.STATE_OFF:
                	//clearPairedList(); to be implemented if need
    				BTArrayAdapter.clear();
    				BTArrayAdapter.notifyDataSetChanged();
                    break;
                case BluetoothAdapter.STATE_ON:
                	fillPairedList();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Toast.makeText(getApplicationContext(),"Turning BT off, pls wait...",
                    Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Toast.makeText(getApplicationContext(),"Turning BT on, pls wait...",
                    Toast.LENGTH_SHORT).show();
                	break;
                }
            }
        }
    };	    
	
	/**
	 * fill list paired devices
	 */
	private void fillPairedList() {
		if (myBluetoothAdapter.enable()) {
		    // get paired devices
			pairedDevices = myBluetoothAdapter.getBondedDevices();
			BTArrayAdapter.clear();
			for(BluetoothDevice device : pairedDevices)
			   BTArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
	        BTArrayAdapter.notifyDataSetChanged();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.action_exit) {
            finish();
            System.exit(0);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    // Unregister broadcast listeners
	    this.unregisterReceiver(mReceiver);
	}
	
	//TODO: here we can start activity by device name,
	//for example activity_RGB for devices with name "RGB-*" etc 
	
	private void startDevice(String sName) {
		Intent intent = new Intent(this, RGBActivity.class);
	    intent.putExtra("sName", sName);
	    startActivity(intent);
	}
}
