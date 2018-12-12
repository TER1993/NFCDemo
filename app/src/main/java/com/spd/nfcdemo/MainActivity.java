package com.spd.nfcdemo;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.pstreets.nfc.NfcMifareClass;
import com.pstreets.nfc.iso15693.NfcVmifare;

/**
 * @author xuyan
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button btMfare = (Button) findViewById(R.id.mifare);
		Button btIso15693 = (Button) findViewById(R.id.iso15693);
		btMfare.setOnClickListener(new OnClickListener()
		{

			@Override
            public void onClick(View view)
				{
					Intent intent = new Intent (MainActivity.this,NfcMifareClass.class);			
					startActivity(intent);
				}
		});
		btIso15693.setOnClickListener(new OnClickListener()
		{

			@Override
            public void onClick(View view)
				{
				Intent intent = new Intent (MainActivity.this,NfcVmifare.class);			
				startActivity(intent);
				}
		});
	}
}
