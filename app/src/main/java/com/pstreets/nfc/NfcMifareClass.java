package com.pstreets.nfc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nfcdemo.R;
import com.pstreets.nfc.dataobject.mifare.MifareBlock;
import com.pstreets.nfc.dataobject.mifare.MifareClassCard;
import com.pstreets.nfc.dataobject.mifare.MifareSector;
import com.pstreets.nfc.util.Converter;

public class NfcMifareClass extends Activity {

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
	MifareClassic mfc;
	MifareClassCard mifareClassCard = null;
	private TextView main_info;
	ArrayList<String> blockData;
	ListAdapter adapter;
	// ListView listview =null;
	ListView list = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Capture Purchase button from layout
		Button scanBut = (Button) findViewById(R.id.clear_but);
		read = (Button) findViewById(R.id.read);
		write = (Button) findViewById(R.id.write);
		list = (ListView) findViewById(R.id.list);
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
				if (mfc == null) {
					return;
				}
				// 5.1) Connect to card
				read.setClickable(false);
				read.setEnabled(false);

				new Thread() {
					@Override
					public void run() {
						super.run();
						try {
							mfc.close();
							mfc.connect();
							boolean auth = false;
							// 5.2) and get the number of sectors this card
							// has..and
							// loop
							// thru these sectors
							final int secCount = mfc.getSectorCount();
							mifareClassCard = new MifareClassCard(secCount);
							int bCount = 0;
							int bIndex = 0;
							for (int j = 0; j < secCount; j++) {
								MifareSector mifareSector = new MifareSector();
								mifareSector.sectorIndex = j;
								// 6.1) authenticate the sector
								// auth = mfc.authenticateSectorWithKeyA(j,
								// "passwo".getBytes());
								auth = mfc.authenticateSectorWithKeyA(j,
										MifareClassic.KEY_DEFAULT);
								mifareSector.authorized = auth;
								if (auth) {
									// 6.2) In each sector - get the block count
									bCount = mfc.getBlockCountInSector(j);
									bCount = Math.min(bCount,
											MifareSector.BLOCKCOUNT);
									bIndex = mfc.sectorToBlock(j);
									for (int i = 0; i < bCount; i++) {

										// 6.3) Read the block
										byte[] data = mfc.readBlock(bIndex);
										MifareBlock mifareBlock = new MifareBlock(
												data);
										mifareBlock.blockIndex = bIndex;
										// 7) Convert the data into a string
										// from Hex
										// format.

										bIndex++;
										mifareSector.blocks[i] = mifareBlock;

									}
									mifareClassCard.setSector(
											mifareSector.sectorIndex,
											mifareSector);
								} else { // Authentication failed - Handle it

								}
							}
							blockData = new ArrayList<String>();
							int blockIndex = 0;
							for (int i = 0; i < secCount; i++) {

								MifareSector mifareSector = mifareClassCard
										.getSector(i);
								for (int j = 0; j < MifareSector.BLOCKCOUNT; j++) {
									MifareBlock mifareBlock = mifareSector.blocks[j];
									byte[] data = mifareBlock.getData();
									blockData.add("Block "
											+ blockIndex++
											+ " : "
											+ Converter.getHexString(data,
													data.length));
									// System.out.println("1" + new
									// String(data));
								}
							}
							String[] contents = new String[blockData.size()];
							Message msg = new Message();
							msg.obj = contents;
							handler.sendMessage(msg);
							try {
								mfc.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								showAlert(3);
							}
						} catch (IOException e) {
							// Log.e(TAG, e.getLocalizedMessage());
							showAlert(3);
						} finally {

							if (mifareClassCard != null) {
								mifareClassCard.debugPrint();
							}
						}
					}
				}.start();
			}
		});
		write.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mfc == null) {
					return;
				}
				try {
					mfc.connect();
					boolean auth = false;
					short sectorAddress = 8;
					auth = mfc.authenticateSectorWithKeyA(4,
							MifareClassic.KEY_DEFAULT);
					if (auth) {
						Log.d(TAG, "--------------?>>>>>>>>>");
						// the last block of the sector is used for KeyA and
						// KeyB cannot be overwritted
						mfc.writeBlock(16, "1313838438000000".getBytes());
						Toast.makeText(NfcMifareClass.this,
								"1313838438000000 write successfully",
								Toast.LENGTH_SHORT).show();
						mfc.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					try {
						mfc.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String[] contents = (String[]) msg.obj;
			blockData.toArray(contents);
			adapter = new ArrayAdapter<String>(NfcMifareClass.this,
					android.R.layout.simple_list_item_1, contents);
			list.setAdapter(adapter);
			read.setClickable(true);
			read.setEnabled(true);
		};
	};

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
				mTechLists = new String[][] { new String[] { MifareClassic.class
						.getName() } };

				Intent intent = getIntent();
				resolveIntent(intent);
			} else {
				Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
				new AlertDialog.Builder(this)
						.setTitle("Settings")
						// .setMessage("5")
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
			Toast.makeText(this, "Please put the card in appointed field",
					Toast.LENGTH_SHORT).show();
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
			mfc = MifareClassic.get(tagFromIntent);
			if (mfc != null) {
				Toast.makeText(this, "initialization successed",
						Toast.LENGTH_SHORT).show();
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
		if (blockData != null) {
			blockData.clear();
			list.setAdapter(null);
		}
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