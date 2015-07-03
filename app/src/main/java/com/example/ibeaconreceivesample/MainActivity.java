package com.example.ibeaconreceivesample;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import android.widget.TextView;
import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;


public class MainActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter;
	/** BLE 機器検索のタイムアウト(ミリ秒) */
//	private static final long SCAN_PERIOD = 10000;
    private static final long SCAN_PERIOD = 1000;
	private static final String TAG = "MainActivity";
	Handler mHandler = new Handler(); // (1)
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final BluetoothManager bluetoothManager =
		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		Button btn = (Button)findViewById(R.id.button1);
		btn.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			    mHandler.postDelayed(new Runnable() {
			        @Override
			        public void run() {
			            // タイムアウト
			        	Log.d(TAG, "タイムアウト");
			            mBluetoothAdapter.stopLeScan(mLeScanCallback);

						// fuji - start
                        // textView1 の内容を取得
						TextView textView = (TextView) findViewById(R.id.textView1);

                        // iBeacon が見つからなかった場合は "Scanning...." なので
                        // "Not Found." に変更し、invalidate() を使い再描画
                        // 見つかった場合は iBeacon の情報が格納されているので
                        // invalidate() を使い再描画し、通知を送信
                        if (textView.getText() == "Scanning....") {
                            textView.setText("Not Found.");
                            textView.invalidate();
                        } else {
                            textView.invalidate();
                            sendNotification();
                        }
						// fuji - end

			        }
			    }, SCAN_PERIOD);
			 
			    // スキャン開始
			    mBluetoothAdapter.startLeScan(mLeScanCallback);

				// fuji - start
                // ボタンが押された後に textView1 を "Scanning...." に変更
				TextView textView = (TextView) findViewById(R.id.textView1);
				textView.setText("Scanning....");
                // fuji - end
			}
		} );

	}

	private void sendNotification() {
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

        // ----- 最低限必要な情報 -------
		notificationBuilder.setSmallIcon(R.drawable.ic_launcher);               // アイコン画像
		notificationBuilder.setContentTitle("ゲスト接近");                       // タイトル
		notificationBuilder.setContentText("○○様がもうすぐ到着されます");   // メッセージ内容

        // バイブレーションさせたい場合に設定する
		notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        // 通知を送るためのノティフィケーションマネージャーの生成
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // 通知を送る
		int notificationId = 1;
		notificationManager.notify(notificationId, notificationBuilder.build());
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
	    @Override
	    public void onLeScan(final BluetoothDevice device, int rssi,byte[] scanRecord) {
	        Log.d(TAG, "receive!!!");
	        getScanData(scanRecord);
            Log.d(TAG, "device name:" + device.getName());
            Log.d(TAG, "device address:" + device.getAddress());
	    }
	    
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void getScanData( byte[] scanRecord ){
		if(scanRecord.length > 30)
		{
		    if((scanRecord[5] == (byte)0x4c) && (scanRecord[6] == (byte)0x00) &&
		       (scanRecord[7] == (byte)0x02) && (scanRecord[8] == (byte)0x15))
		    {
		            String uuid = Integer.toHexString(scanRecord[9] & 0xff) 
		            + Integer.toHexString(scanRecord[10] & 0xff)
		            + Integer.toHexString(scanRecord[11] & 0xff)
		            + Integer.toHexString(scanRecord[12] & 0xff)
		            + "-"
		            + Integer.toHexString(scanRecord[13] & 0xff)
		            + Integer.toHexString(scanRecord[14] & 0xff)
		            + "-"
		            + Integer.toHexString(scanRecord[15] & 0xff)
		            + Integer.toHexString(scanRecord[16] & 0xff)
		            + "-"
		            + Integer.toHexString(scanRecord[17] & 0xff)
		            + Integer.toHexString(scanRecord[18] & 0xff)
		            + "-"
		            + Integer.toHexString(scanRecord[19] & 0xff)
		            + Integer.toHexString(scanRecord[20] & 0xff)
		            + Integer.toHexString(scanRecord[21] & 0xff)
		            + Integer.toHexString(scanRecord[22] & 0xff)
		            + Integer.toHexString(scanRecord[23] & 0xff)
		            + Integer.toHexString(scanRecord[24] & 0xff);

				String major = Integer.toHexString(scanRecord[25] & 0xff) + Integer.toHexString(scanRecord[26] & 0xff);
		            String minor = Integer.toHexString(scanRecord[27] & 0xff) + Integer.toHexString(scanRecord[28] & 0xff);
		            
		            Log.d(TAG, "UUID:"+uuid );
		            Log.d(TAG, "major:" + major);
		            Log.d(TAG, "minor:" + minor);

                    // fuji - start
                    // Intensity(rssi) を取得
                    String rssi = Integer.toHexString(scanRecord[29] & 0xff);

                    // iBeacon の情報を textView1 に格納
                    TextView textView = (TextView) findViewById(R.id.textView1);
                    textView.setText("uuid: "+uuid + "\n" + "major: " + major + "\n" + "minor: " + minor + "\n" + "intensity: -" + rssi);

                    // コールバック関数の中は画面の再描画がされないので、
                    // mHandler.postDelayed() において textView.invalidate() を呼び出す

                    // fuji - end
            }
		}
	}

}
