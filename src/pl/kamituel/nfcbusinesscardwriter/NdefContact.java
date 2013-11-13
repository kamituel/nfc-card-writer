package pl.kamituel.nfcbusinesscardwriter;

import java.nio.charset.Charset;

import android.nfc.NdefRecord;
import android.util.Log;


public class NdefContact {
	private String mVcard;
	
	private static final String VCARD_HEADER = "BEGIN:VCARD\nVERSION:2.1\n";
	private static final String VCARD_FOOTER = "END:VCARD";
	
	private NdefContact (String vcard) {
		Log.w("xxx", vcard);
		mVcard = vcard;
	}
	
	public NdefRecord toNdefRecord () {
		byte[] vcard = mVcard.getBytes(Charset.forName("US-ASCII"));
		byte[] payload = new byte[vcard.length + 1];
		System.arraycopy(vcard, 0, payload, 1, vcard.length); 
		return new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/vcard".getBytes(), new byte[0], payload);
	}
	
	public static class Builder {
		public static final String PHONE_TYPE_HOME = "HOME";
		
		private StringBuilder mVcard = new StringBuilder();
		
		public Builder () {
			mVcard.append(VCARD_HEADER);
		}
		
		public Builder appendPhone (String type, String number) {
			appendField("TEL", type, number);
			return this;
		}
		
		public Builder appendName (String name) {
			appendField("FN", "", name);
			return this;
		}
		
		public Builder appendEmail (String email) {
			appendField("EMAIL", "PREF", "INTERNET", email);
			return this;
		}
		
		private Builder appendField (String fieldName, String... values) {
			mVcard.append(fieldName);
			
			for (int v = 0; v < values.length; v += 1) {
				mVcard.append(v == values.length - 1 ? ":" : ";");
				mVcard.append(values[v]);
			}

			mVcard.append("\n");
			return this;
		}
		
		public NdefContact build () {
			mVcard.append(VCARD_FOOTER);
			return new NdefContact(mVcard.toString());
		}
	}
}
