package com.snacourse.nfcreader;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class NFCReaderActivity extends AppCompatActivity {
    NfcAdapter mNfcAdapter;
    boolean nfcEnabled;
    PendingIntent mNfcPendingIntent;
    IntentFilter[] mReadTagFilters;
    private static final String TAG = "nfcinventory_simple";
    AlertDialog.Builder alertDialog;
    private boolean mWriteMode = false;
    TextView txtData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

txtData=findViewById(R.id.tx);
        checkNFCSupport();
initNFCListener();


    }
    private  void checkNFCSupport(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null)
        {
            Toast.makeText(this,
                    "Your device does not support NFC. Cannot run demo.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    public void doAction(Intent intent) {
        NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
        confirmDisplayedContentOverwrite(msgs[0]);
    }
    @Override
    protected void onNewIntent(Intent intent)
    {
        mWriteMode=false;

        if (!mWriteMode)  {

            // Currently in tag READING mode
            if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
            {
                doAction(intent);
            } else if (intent.getAction().equals(
                    NfcAdapter.ACTION_TAG_DISCOVERED))
            {
                Toast.makeText(this,
                        "This NFC tag currently has no inventory NDEF data.",
                        Toast.LENGTH_LONG).show();
            }

        }


    }
    private void enableTagReadMode()
    {
        mWriteMode = false;
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadTagFilters, null);
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause: " + getIntent());
        mNfcAdapter.disableForegroundDispatch(this);
    }


    NdefMessage[] getNdefMessagesFromIntent(Intent intent)
    {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED)
                || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null)
            {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else
            {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                        empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }
        } else
        {
            Log.e(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }

    private void confirmDisplayedContentOverwrite(final NdefMessage msg)
    {
       /* new AlertDialog.Builder(this)
                .setTitle("new Tag found")
                .setMessage("Replace current tag")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // use the current values in the NDEF payload
                        // to update the text fields
                        String payload = new String(msg.getRecords()[0]
                                .getPayload());
                        setTextFieldValues(payload);
                    }


                }).show();*/
        String payload = new String(msg.getRecords()[0]
                .getPayload());
        setTextFieldValues(payload);




    }



    private void setTextFieldValues(String jsonString)
    {

        JSONObject inventory = null;

        try
        {
            inventory = new JSONObject(jsonString);

            txtData.setText(inventory.toString());
         //   namDars=inventory.getString("nDars")  ;
          ///  tEmtahan = inventory.getString("name");
          //  nEmtahan = inventory.getString("ram");
          //  tAmoozegar = inventory.getString("processor");
        } catch (JSONException e)
        {
            Log.e(TAG, "Couldn't parse JSON: ", e);
        }


        }




    private void checkNfcEnabled()
    {
        nfcEnabled = mNfcAdapter.isEnabled();

        if (!nfcEnabled) {
            Log.e("dar if",nfcEnabled+"")  ;

            alertDialog=       new AlertDialog.Builder(this) ;






            alertDialog.setTitle("NFC is off") ;
            alertDialog.setMessage("Turn on nfc");
            alertDialog.setCancelable(false) ;
            alertDialog.setPositiveButton("Update Settings",
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog,
                                            int id)
                        {
                            dialog.dismiss();
                            startActivity(new Intent(

                                    android.provider.Settings.ACTION_WIRELESS_SETTINGS)
                            );

                        }
                    }).create().show();
            alertDialog.create().cancel();
            alertDialog.create().dismiss();




        }

    }
    @Override
    protected void onResume()
    {

        super.onResume();


        // Double check if NFC is enabled

        checkNfcEnabled();


        Log.d(TAG, "onResume: " + getIntent());
        if (!mWriteMode)  {
            if (getIntent().getAction() != null)
            {
                // tag received when app is not running and not in the foreground:
                if (getIntent().getAction().equals(
                        NfcAdapter.ACTION_NDEF_DISCOVERED))
                {
                    doAction(getIntent()) ;


                }

            }
        }


        // Enable priority for current activity to detect scanned tags
        // enableForegroundDispatch( activity, pendingIntent,
        // intentsFiltersArray, techListsArray );
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                mReadTagFilters, null);

    }



   private void initNFCListener(){

       mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
               getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

       // Create intent filter to handle NDEF NFC tags detected from inside our
       // application when in "read mode":
       IntentFilter ndefDetected = new IntentFilter(
               NfcAdapter.ACTION_NDEF_DISCOVERED);
       try
       {
           ndefDetected.addDataType("application/root.gast.playground.nfc");
       } catch (IntentFilter.MalformedMimeTypeException e)
       {
           throw new RuntimeException("Could not add MIME type.", e);
       }

       // Create intent filter to detect any NFC tag when attempting to write
       // to a tag in "write mode"
       IntentFilter tagDetected = new IntentFilter(
               NfcAdapter.ACTION_TAG_DISCOVERED);

       // create IntentFilter arrays:
       //  mWriteTagFilters = new IntentFilter[] { tagDetected };
       mReadTagFilters = new IntentFilter[] { ndefDetected, tagDetected };
   }
}
