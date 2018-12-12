package com.pstreets.nfc.iso15693;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spd.nfcdemo.R;
import com.pstreets.nfc.dataobject.iso15693.NfcVUtil;

import java.io.IOException;
public class NfcVmifare extends ListActivity {

	// NFC parts
	private static NfcAdapter mAdapter;
	private static PendingIntent mPendingIntent;
	private static IntentFilter[] mFilters;
	private static String[][] mTechLists;
	private boolean isNFC;
	private static final int AUTH = 1;
	private static final int EMPTY_BLOCK_0 = 2;
	private static final int EMPTY_BLOCK_1 = 3;
	private static final int NETWORK = 4;
	private static final String TAG = "mifare";
	private Button read, write;
	private NfcVUtil mNfcVUtil;
	private NfcV mNfcV;
	private TextView main_info;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main15693);

		// Capture Purchase button from layout
		Button scanBut = (Button) findViewById(R.id.clear_but);
		read = (Button) findViewById(R.id.read);
		write = (Button) findViewById(R.id.write);
		main_info = (TextView) findViewById(R.id.empty);
		// Register the onClick listener with the implementation above
		isNFC = getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_NFC);
		scanBut.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {

				clearFields();
			}
		});
		read.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mNfcV == null) {
					return;
				}
			
				try { // 5.1) Connect to card
					mNfcV.close();
					mNfcV.connect();
					NfcVUtil mNfcVutil = new NfcVUtil(mNfcV);
					String getdatestring = mNfcVutil.getUID();
					main_info.setTextSize(20);
					main_info.append("card number:"+getdatestring+"\n\n");
					//读卡28个块内容
					String sx = mNfcVutil.readOneBlock(27);
					//main_info.append(sx+"\n\n");
					Log.d("kwang", "nfc");
					mNfcV.close();
				} catch (IOException e) {
					// Log.e(TAG, e.getLocalizedMessage());
					showAlert(3);
				} finally {

				}
			}
		});
		write.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mNfcV == null) {
					return;
				}
				try {
					mNfcV.connect();
					NfcVUtil mNfcVutil = new NfcVUtil(mNfcV);
					mNfcVutil.writeBlock(14, new byte[] { 1, 1, 1, 1 });
					String getdate = mNfcVutil.readOneBlock(14);
					main_info.append(getdate);
					Toast.makeText(NfcVmifare.this, "write success", Toast.LENGTH_SHORT)
							.show();
					mNfcV.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						mNfcV.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void initNFC() {
		if (isNFC) {
			mAdapter = NfcAdapter.getDefaultAdapter(this);
			if (mAdapter.isEnabled()) {
				// Create a generic PendingIntent that will be deliver to this
				// activity.
				// The NFC stack
				// will fill in the intent with the details of the discovered
				// tag before
				// delivering to
				// this activity.
				mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(
						this, getClass())
						.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
				// Setup an intent filter for all MIME based dispatches
				IntentFilter ndef = new IntentFilter(
						NfcAdapter.ACTION_TECH_DISCOVERED);

				try {
					ndef.addDataType("*/*");
				} catch (MalformedMimeTypeException e) {
					throw new RuntimeException("fail", e);
				}
				mFilters = new IntentFilter[] { ndef, };

				// Setup a tech list for all NfcF tags
				mTechLists = new String[][] { new String[] { NfcV.class
						.getName() } };

				Intent intent = getIntent();
				resolveIntent(intent);
			} else {
				Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
				new AlertDialog.Builder(this)
						.setTitle("Settings")
						// .setMessage("设置")
						.setNegativeButton("open",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										Intent callGPSSettingIntent = new Intent(
												android.provider.Settings.ACTION_WIRELESS_SETTINGS);
										startActivity(callGPSSettingIntent);
									}
								})
						.setPositiveButton("cancel",
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

									}
								}).create().show();

			}

		} else {
			Toast.makeText(this, "Please put the card in appointed field", Toast.LENGTH_SHORT).show();
		}
	}

	void resolveIntent(Intent intent) {
		// 1) Parse the intent and get the action that triggered this intent
		String action = intent.getAction();
		// 2) Check if it was triggered by a tag discovered interruption.
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			// 3) Get an instance of the TAG from the NfcAdapter
			Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			// 4) Get an instance of the Mifare classic card from this TAG
			// intent
			mNfcV = NfcV.get(tagFromIntent);
			if (mNfcV != null) {
				Toast.makeText(this, "The card connected, don't move", Toast.LENGTH_SHORT).show();
			}
		}// End of method
	}

	private void showAlert(int alertCase) {
		// prepare the alert box
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		switch (alertCase) {

		case AUTH:// Card Authentication Error
			alertbox.setMessage("Authentication Failed ");
			break;
		case EMPTY_BLOCK_0: // Block 0 Empty
			alertbox.setMessage("Failed reading ");
			break;
		case EMPTY_BLOCK_1:// Block 1 Empty
			alertbox.setMessage("Failed reading 0");
			break;
		case NETWORK: // Communication Error
			alertbox.setMessage("Tag reading error");

			break;
		}
		// set a positive/yes button and create a listener
		alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			// Save the data from the UI to the database - already done
			public void onClick(DialogInterface arg0, int arg1) {
				clearFields();
			}
		});
		// display box
		alertbox.show();

	}

	private void clearFields() {
		main_info.setText("");
	}

	@Override
	public void onResume() {

		super.onResume();
		initNFC();
		if (isNFC && mAdapter.isEnabled()) {
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
					mTechLists);
		}

	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);
		resolveIntent(intent);
		// mText.setText("Discovered tag " + ++mCount + " with intent: " +
		// intent);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (isNFC && mAdapter.isEnabled()) {
			mAdapter.disableForegroundDispatch(this);
		}
	}
}