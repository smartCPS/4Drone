package com.example.fftsample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.example.fftsample.R;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.widget.Toast;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import com.emotiv.insight.IEdk;
import com.emotiv.insight.IEdkErrorCode;
import com.emotiv.insight.IEdk.IEE_DataChannel_t;
import com.emotiv.insight.IEdk.IEE_Event_t;

/**
 * 2017.02.09 이승기
 * 현재 emotiv에서 제공하는 jar파일(라이브러리)를 찾을 수 없기에
 * IEdk에서 가져오는 함수들을 사용하는데 문제가 됨
 * 메니페스트 파일과 레이아웃은 코드를 맞춰놈
 * 어뎁터 부분 제외 코드 카피 (이상의 추가는 라이브러리를 찾고 개시)
 * -----
 * 2017.02.13 이승기
 * com.emotiv.insight.IEdk문제 해결
 * 가상에뮬레이터를 특수하게 설정 해 주어야함
 * 1. Go to Tools -> Android -> AVD Manager
 * 2. Click "Create Virtual Device"
 * 3. Select which device you want to use from the list (i.e Nexus 5) and click "Next".
 * 4. Here you're given a list of android release versions. Look at the ABI column. "Armeabi-v7a" ABI is what to look for, for whichever API Level you want.
 * 5. Hit "Next" and modify name/size if you want, click "Finish" when done.
 *
 * 현재 블루투스 함수 부분에서 에러가 남
 * at com.example.fftsample.MainActivity.checkConnect(MainActivity.java:305)
 * at com.example.fftsample.MainActivity.onCreate(MainActivity.java:89)
 *
 */


public class MainActivity extends Activity {

		private Thread processingThread;
		private static final int REQUEST_ENABLE_BT = 1;
		private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 0;
		private BluetoothAdapter mBluetoothAdapter;
		private boolean lock = false;
		private boolean isEnablGetData = false;
		private boolean isEnableWriteFile = false;
		int userId;
		private BufferedWriter motion_writer;
		Button Start_button,Stop_button;
		IEE_DataChannel_t[] Channel_list = {IEE_DataChannel_t.IED_AF3, IEE_DataChannel_t.IED_T7,IEE_DataChannel_t.IED_Pz,
				IEE_DataChannel_t.IED_T8,IEE_DataChannel_t.IED_AF4};
		String[] Name_Channel = {"AF3","T7","Pz","T8","AF4"};
		@Override
		protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

		//버전에 따른 어뎁터 방식이 다르다.
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mBluetoothAdapter = bluetoothManager.getAdapter();
		} else {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}


		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			/***Android 6.0 and higher need to request permission*****/
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_FINE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED) {

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
						MY_PERMISSIONS_REQUEST_BLUETOOTH);
			} else {
				checkConnect();
			}
		} else {
			checkConnect();
		}


		Start_button = (Button)findViewById(R.id.startbutton);
		Stop_button  = (Button)findViewById(R.id.stopbutton);
		
		Start_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.e("FFTSample","Start Write File");
				setDataFile();
				isEnableWriteFile = true;
			}
		});
		Stop_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.e("FFTSample","Stop Write File");
				StopWriteFile();
				isEnableWriteFile = false;
			}
		});

		processingThread=new Thread()
		{
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				while(true)
				{
					try
					{
						handler.sendEmptyMessage(0);
						handler.sendEmptyMessage(1);
						if(isEnablGetData && isEnableWriteFile)handler.sendEmptyMessage(2);
						Thread.sleep(5);
					}
					
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
		};
		processingThread.start();

	}
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case 0:
				int state = IEdk.IEE_EngineGetNextEvent();
				if (state == IEdkErrorCode.EDK_OK.ToInt()) {
					int eventType = IEdk.IEE_EmoEngineEventGetType();
				    userId = IEdk.IEE_EmoEngineEventGetUserId();
					if(eventType == IEE_Event_t.IEE_UserAdded.ToInt()){
						Log.e("SDK","User added");
						IEdk.IEE_FFTSetWindowingType(userId, IEdk.IEE_WindowsType_t.IEE_BLACKMAN);
						isEnablGetData = true;
					}
					if(eventType == IEE_Event_t.IEE_UserRemoved.ToInt()){
						Log.e("SDK","User removed");
						isEnablGetData = false;
					}
				}

				break;
			case 1:
				/*Connect device with Insight headset*/
				int number = IEdk.IEE_GetInsightDeviceCount();
				if(number != 0) {
					if(!lock){
						lock = true;
						IEdk.IEE_ConnectInsightDevice(0);
					}
				}
				/**************************************/
				/*Connect device with Epoc Plus headset*/
//				int number = IEdk.IEE_GetEpocPlusDeviceCount();
//				if(number != 0) {
//					if(!lock){
//						lock = true;
//						IEdk.IEE_ConnectEpocPlusDevice(0,false);
//					}
//				}
				/**************************************/
				else lock = false;
				break;
			case 2:
				if(motion_writer == null) return;
				for(int i=0; i < Channel_list.length; i++)
				{
					double[] data = IEdk.IEE_GetAverageBandPowers(Channel_list[i]);
					if(data.length == 5){
						try {
							motion_writer.write(Name_Channel[i] + ",");
							for(int j=0; j < data.length;j++)
								addData(data[j]);
							motion_writer.newLine();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				break;
			}

		}

	};

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_BLUETOOTH: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					checkConnect();

				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this, "App can't run without this permission", Toast.LENGTH_SHORT).show();
				}
				return;
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_ENABLE_BT) {
			if(resultCode == Activity.RESULT_OK){
				//Connect to emoEngine
				IEdk.IEE_EngineConnect(this,"");
			}
			if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(this, "You must be turn on bluetooth to connect with Emotiv devices"
						, Toast.LENGTH_SHORT).show();
			}
		}
	}



	private void setDataFile() {
		try {
			String eeg_header = "Channel , Theta ,Alpha ,Low beta ,High beta , Gamma ";
			File root = Environment.getExternalStorageDirectory();
			String file_path = root.getAbsolutePath()+ "/FFTSample/";
			File folder=new File(file_path);
			if(!folder.exists())
			{
				folder.mkdirs();
			}		
			motion_writer = new BufferedWriter(new FileWriter(file_path+"bandpowerValue.csv"));
			motion_writer.write(eeg_header);
			motion_writer.newLine();
		} catch (Exception e) {
			Log.e("","Exception"+ e.getMessage());
		}
	}
	private void StopWriteFile(){
		try {
			motion_writer.flush();
			motion_writer.close();
			motion_writer = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * public void addEEGData(Double[][] eegs) Add EEG Data for write int the
	 * EEG File
	 * 
	 * @param eegs
	 *            - double array of eeg data
	 */
	public void addData(double data) {

		if (motion_writer == null) {
			return;
		}

			String input = "";
				input += (String.valueOf(data) + ",");
			try {
				motion_writer.write(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	private void checkConnect(){
		if (!mBluetoothAdapter.isEnabled()) {
			/****Request turn on Bluetooth***************/
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		else
		{
			//Connect to emoEngine
			IEdk.IEE_EngineConnect(this,"");
		}
	}

}
