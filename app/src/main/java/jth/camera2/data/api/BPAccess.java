package jth.camera2.data.api;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class BPAccess {

	public static boolean Debug = false;


	public static String getUrl_W(Context context){
 return "https://www.aladin.co.kr/";
//		return "http://aa.aladin.co.kr/";
//		 return "https://juliet.aladin.co.kr/";
//		return "https://stage.aladin.co.kr/";
		/*if (Debug) {
			return getAzureAccessKey(context, "W","eKk1RY+ov07DJ1bkgI8bbxjt6sXaFQj+XIF3raOtObo=");
		} else {
			return getAzureAccessKey(context,"W","wLF/4xtjZGfavZSs371vpuJyqSsNl9b8nbdmiIxYoVU=");
		}*/
	}

	public static String getUrl_B(Context context){
//		return "https://blog.aladin.co.kr/api/";
return "https://blogif.aladin.co.kr/api/";
//		return "http://blogdev.aladin.co.kr/api/";
//		return "http://blogif.aladin.co.kr/api/";
		/*if (Debug) {
			return getAzureAccessKey(context, "B","FrLG5c1ocnQ8DQXIOMh3PMwR6pvD+Eo9090xsoRMMOw=");
		} else {
			return getAzureAccessKey(context,"B","AUiyZuFgri7hQLVY5PGFrb9rOMXsKGawVUloOv8eBiA=");
		}*/
	}

	public static String getURL_BPPR(){
		return "paper.aspx";
	}

	public static String getURL_BPPT(){
		return  "product.aspx";
	}

	public static String getURL_BPUR(){
		return "user.aspx";
	}

	public static String getURL_BPFD(){
		return "feed.aspx";
	}

	public static String getURL_BPNI(){
		return "noti.aspx";
	}

	public static String getURL_BPBS(){
		return "reading.aspx";
	}

	public static String getURL_BPFR(){
		 return "friend.aspx";
	}

	//================================================================

	public static String getURL_BPSERVICE_AN(){
		 return "m/mservice/applogin.aspx";
	}
	public static String getURL_BPSERVICE_AH(){
		return "m/mservice/apppush.aspx";
	}
	public static String getURL_BPSERVICE_AG(){
		return "m/mservice/appmng.aspx";
	}

	public static String getURL_BPSERVICE_UT(){
		return "m/mservice/utility.aspx";
	}

	//================================================================

	public static String getMD_BPAUTH_LN(){
		return "UserLogin";
	}

	public static String getMD_BPAUTH_CJ(){
		return "CustomerJoin";
	}
	//================================================================
	public static String get_PARAM_I(){
		return "ID";
	}
	public static String get_PARAM_D(){
		return "PWD";
	}
	public static String get_PARAM_U(){
		return "uuid";
	}

	//================================================================

	public static String getOcrApiKey(Context context) {
		if (Debug) {
			return getAzureAccessKey(context, "OCR", "uMB2m3X22FtjH1FOcc8G+4kmdvN1IEeo/DeGwje98uifFkYtRTsn1ylphyFT4wij");
		} else {
			return getAzureAccessKey(context, "OCR", "XnDo4sLt80ACMYDQZtn0eP5EjHdprUSarwK27992clv+bu4o6SU+wko3mQLHkK4a");
		}
	}


	static HashMap<String, String> mCachedKeys = new HashMap<String, String>();
	public static String getAzureAccessKey(Context context, String AZURE_ACCESS_KEY , String Encode) {
		Cipher c = null;
		String accessKey = mCachedKeys.get(AZURE_ACCESS_KEY);

		if (accessKey == null) {
			try {
				byte[] data = Base64.decode(Encode, Base64.DEFAULT);

				c = Cipher.getInstance("AES/ECB/PKCS5Padding"); // c = Cipher.getInstance(ENCRYPT_ALGORITHM);
				c.init(Cipher.DECRYPT_MODE, getKey(context));

				accessKey = new String(c.doFinal(data), "UTF-8");

				mCachedKeys.put(AZURE_ACCESS_KEY, accessKey);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			}
		}
		return accessKey;
	}

	private static SecretKey getKey(Context context) {
		SecretKey key = null;

		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA-256"); // c = Cipher.getInstance(ENCRYPT_ALGORITHM);

				md.update(signature.toByteArray());

				key = new SecretKeySpec(md.digest(), "AES/ECB/PKCS5Padding");
				break;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return key;
	}
}
