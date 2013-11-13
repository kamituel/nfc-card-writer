package pl.kamituel.nfcbusinesscardwriter;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.widget.Toast;

public class NfcRequiredActivity extends Activity {

	@Override
	protected void onStart() {
		super.onStart();

		if ( !isNfcPresent() ) {
			// should never happen as google play store filters for this.
			// but may be useful when installing from APK file.
			Toast.makeText(this, getResources().getString(R.string.no_nfc_present), Toast.LENGTH_LONG).show();
			finish();
		}

		if ( !isNfcEnabled() ) {
			Toast.makeText(this, getResources().getString(R.string.nfc_disabled), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onPause() {
		super.onPause();

		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		nfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Intent i = new Intent(this, getClass());
		i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if ( nfcAdapter == null ) { 
			Toast.makeText(this, getResources().getString(R.string.no_nfc_present), Toast.LENGTH_LONG).show();
			return; 
		}
		nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
	}

	private boolean isNfcEnabled () {
		NfcManager manager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
		NfcAdapter adapter = manager.getDefaultAdapter();
		return ( adapter != null && adapter.isEnabled() );
	}

	private boolean isNfcPresent () {
		NfcManager manager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
		return ( manager.getDefaultAdapter() != null );
	}
}
