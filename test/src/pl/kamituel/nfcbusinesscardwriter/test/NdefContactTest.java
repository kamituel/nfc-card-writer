package pl.kamituel.nfcbusinesscardwriter.test;

import android.nfc.NdefRecord;
import junit.framework.TestCase;
import pl.kamituel.nfcbusinesscardwriter.NdefContact;

public class NdefContactTest extends TestCase {
		
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testNoInputData() {
		NdefRecord contact = new NdefContact.Builder().build().toNdefRecord();
		assertEquals((String) PrivateAccessor.getPrivateField(contact, "VCARD_HEADER") 
				+ (String) PrivateAccessor.getPrivateField(contact, "VCARD_FOOTER"),
				ndefRecordToString(contact));
	}
	
	public void testContact1() {
		NdefRecord contact = new NdefContact.Builder()
			.appendName("Jorge Luis Borges")
			.appendPhone(NdefContact.Builder.PHONE_TYPE_HOME, "123456789")
			.appendPhone(NdefContact.Builder.PHONE_TYPE_HOME, "987654321")
			.appendEmail("123@321.com")
			.build().toNdefRecord();
		
		assertEquals((String) PrivateAccessor.getPrivateField(contact, "VCARD_HEADER") 
				+ "TEL:HOME;123456789"
				+ "TEL:HOME;987654321"
				+ "EMAIL:PREF:INTERNET;123@321.com"
				+ (String) PrivateAccessor.getPrivateField(contact, "VCARD_FOOTER"),
				ndefRecordToString(contact));
	}
	
	private String ndefRecordToString(NdefRecord record) {
		return new String(record.getPayload());
	}	
}
