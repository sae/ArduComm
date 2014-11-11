package com.github.arducomm;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.example.openarduino.R;
import com.example.openarduino.R.id;
import com.example.openarduino.R.layout;
import com.example.openarduino.R.menu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class RGBActivity extends Activity {

	private String sDevName;
	private String sDevAddr;
	private BluetoothAdapter        myBluetoothAdapter;
	private BluetoothSocket         mBTSocket   = null;
	private BluetoothDevice         mBTDevice       = null; 
	private InputStream           mBTInputStream  = null;
	private OutputStream          mBTOutputStream = null;
	static final UUID UUID_RFCOMM_GENERIC = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	final String LOG_TAG = "my";//this.getCallingPackage();
	
	private ToggleButton tbConnect;
	private SeekBar sbR;
	private SeekBar sbG;
	private SeekBar sbB;
	private TextView txtStat;
	//button Black, White, etc
	private Button btBlack;
	private Button btWhite;
	//how to obtain color from Arduino?
	private boolean blockRGB=false;//to block sendRGB() when changing seekbars
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//assume that BT supported and enabled (if device in list was selected)
		myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		setContentView(R.layout.rgb_device);
		String sTmp[]=getIntent().getStringExtra("sName").split("\n");
		sDevName=sTmp[0];
		sDevAddr=sTmp[1];
		TextView txtName=(TextView)findViewById(R.id.textView1);
		txtName.setText(sDevName+"\n("+sDevAddr+")");
		txtStat=(TextView)findViewById(R.id.textStat);
		tbConnect = (ToggleButton)findViewById(R.id.toggleDevice);
		tbConnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				toggleConnect(isChecked);
			}
		});
		btBlack=(Button)findViewById(R.id.buttonBlack);
		btBlack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendCommand("#0,0,0\r");
			}
		});
		btWhite=(Button)findViewById(R.id.buttonWhite);
		btWhite.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendCommand("#255,255,255\r");
			}
		});
		OnSeekBarChangeListener l=new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				getData();//safe place to obtain status )
				//Log.d(LOG_TAG, "received");
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
					sendRGB();
					//getData();//call here is hangs app and bt!
			}
		};
		//set colors here, height set in xml
		sbR=(SeekBar)findViewById(R.id.seekBarR);
		sbR.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.RED,  PorterDuff.Mode.SRC));
		sbR.setOnSeekBarChangeListener(l);
		sbG=(SeekBar)findViewById(R.id.seekBarG);
		sbG.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.GREEN,  PorterDuff.Mode.SRC));
		sbG.setOnSeekBarChangeListener(l);
		sbB=(SeekBar)findViewById(R.id.seekBarB);
		sbB.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.BLUE,  PorterDuff.Mode.SRC));
		sbB.setOnSeekBarChangeListener(l);
		//feature for return to main screen, thanks to http://habrahabr.ru/post/145646/
		getActionBar().setHomeButtonEnabled(true); 
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	
	private void toggleConnect(boolean bConnect) {
		if (bConnect) {
			connect();
			getData();
		} else {
			disconnect();
		}
	}
	
	/**
	 * get data from device
	 */
	private void getData() {
		byte[] buf=new byte[100];
		try {
			int i=mBTInputStream.available();
			if (i>1) {//0 is sometimes blocking app
				mBTInputStream.read(buf,0,i-1);
				String s=new String(buf,0,i-1);
				//txtStat.setText(s);
			}
		} catch (Exception e) {
			txtStat.setText(e.getMessage());
		}
		
	}
	
	private void sendRGB() {
		StringBuffer sb1=new StringBuffer();
		//set progress:max in activity xml =)
		//to 0-255 for arduino analogWrite()
		sb1.append("#")//start command
		.append(sbR.getProgress())
		.append(",")
		.append(sbG.getProgress())
		.append(",")
		.append(sbB.getProgress())
		.append("\r");//end command
		sendCommand(sb1.toString());
	}
	/**
	 * send a command to device
	 */
	private void sendCommand(String sCommand) {
		try {
			mBTOutputStream.write(sCommand.getBytes());
			//mBTOutputStream.flush(); //does it need here?
		} catch (Exception e) {
			txtStat.setText(e.getMessage());
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.device, menu);
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
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		//msg("disconnect...");
		disconnect();//disconnect device
		super.onBackPressed();//processing to exit 
	}
	
	/**
	 * Reset input and output streams and make sure socket is closed. 
	 * This method will be used during shutdown() to ensure that the connection is properly closed during a shutdown.  
	 * @return
	 */
	private void disconnect() {
	        if (mBTInputStream != null) {
	                try {mBTInputStream.close();} catch (Exception e) {}
	                mBTInputStream = null;
	        }

	        if (mBTOutputStream != null) {
	                try {mBTOutputStream.close();} catch (Exception e) {}
	                mBTOutputStream = null;
	        }

	        if (mBTSocket != null) {
	        	//http://stackoverflow.com/questions/3031796/disconnect-a-bluetooth-socket-in-android
	        	try {Thread.sleep(1000);} catch (Exception e) {}
	       		try {mBTSocket.close();} catch (Exception e) {}
	            mBTSocket = null;
	        }

	}	

	/**
	 * Try to establish a connection with the peer. 
	 * This method runs synchronously and blocks for one or more seconds while it does its thing 
	 * SO CALL IT FROM A NON-UI THREAD!
	 * @return - returns true if the connection has been established and is ready for use. False otherwise. 
	 */
	private  boolean connect() {

	        disconnect();// Reset all streams and socket.
	        // make sure peer is defined as a valid device based on their MAC. If not then do it. 
	        if (mBTDevice == null) 
	                mBTDevice = myBluetoothAdapter.getRemoteDevice(sDevAddr);
	        // Make an RFCOMM binding. 
	        try {
	        	mBTSocket = mBTDevice.createRfcommSocketToServiceRecord(UUID_RFCOMM_GENERIC);
	        } catch (Exception e1) {
	            msg (e1.getMessage());
	            return false;
	        }
	        //msg ("connect(): Trying to connect.");
	        try {
	                mBTSocket.connect();
	        } catch (Exception e) {
	                msg (e.getMessage());
	                tbConnect.setChecked(false);
	                return false;
	        }
			txtStat.setText("CONNECTED!");
	        //msg ("CONNECTED!");
	        try {
	                mBTOutputStream = mBTSocket.getOutputStream();
	                mBTInputStream  = mBTSocket.getInputStream();
	        } catch (Exception e) {
	                msg (e.getMessage());
	                return false;
	        }
	        return true;
	}
	
	private void msg(String sText) {
        Toast.makeText(getApplicationContext(),sText,Toast.LENGTH_SHORT).show();
	}
}
