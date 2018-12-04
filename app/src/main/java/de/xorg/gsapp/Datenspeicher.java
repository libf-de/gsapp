package de.xorg.gsapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Datenspeicher {

	@SuppressLint("TrulyRandom")
	public static boolean savePassword(String pass, Context ct) {
	    try {
	    	if(pass.equals("")) {
	    		Editor editor = PreferenceManager.getDefaultSharedPreferences(ct).edit();
			    editor.putString("EssenPassword", "");
		        editor.commit();
	    	} else {
	    		DESKeySpec keySpec = new DESKeySpec("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".getBytes("UTF8"));
		        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		        SecretKey key = keyFactory.generateSecret(keySpec);
		        
		        String passwd = "0CB0" + pass;

		        byte[] clearText = passwd.getBytes("UTF8");
		        // Cipher is not thread safe
		        Cipher cipher = Cipher.getInstance("DES");
		        cipher.init(Cipher.ENCRYPT_MODE, key);

		        String encrypedValue = Base64.encodeToString(cipher.doFinal(clearText), Base64.DEFAULT);
		        
		        Editor editor = PreferenceManager.getDefaultSharedPreferences(ct).edit();
			    editor.putString("EssenPassword", encrypedValue);
		        editor.commit();
	    	}

	        return true;
	    } catch (InvalidKeyException e) {
	        e.printStackTrace();
	        return false;
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	        return false;
	    } catch (InvalidKeySpecException e) {
	        e.printStackTrace();
	        return false;
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	        return false;
	    } catch (BadPaddingException e) {
	        e.printStackTrace();
	        return false;
	    } catch (NoSuchPaddingException e) {
	        e.printStackTrace();
	        return false;
	    } catch (IllegalBlockSizeException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public static String getPassword(Context ct) {
	    try {
	        DESKeySpec keySpec = new DESKeySpec("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".getBytes("UTF8"));
	        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
	        SecretKey key = keyFactory.generateSecret(keySpec);
	        
	        String epass = PreferenceManager.getDefaultSharedPreferences(ct).getString("EssenPassword", "");
	        
	        if(epass.equals("")) {
	        	return "";
	        } else {
	        	byte[] encrypedPwdBytes = Base64.decode(epass, Base64.DEFAULT);
		        // cipher is not thread safe
		        Cipher cipher = Cipher.getInstance("DES");
		        cipher.init(Cipher.DECRYPT_MODE, key);
		        byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));

		        String decrypedValue = new String(decrypedValueBytes);
		        
		        if(decrypedValue.startsWith("0CB0")) {
		        	String rPassWD = decrypedValue.replaceFirst("0CB0", "");
		        	return rPassWD;
		        } else {
		        	return "error:nocb";
		        }
	        }
	    } catch (InvalidKeyException e) {
	        e.printStackTrace();
	        return "error:ike";
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	        return "error:uee";
	    } catch (InvalidKeySpecException e) {
	        e.printStackTrace();
	        return "error:ikse";
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	        return "error:nsae";
	    } catch (BadPaddingException e) {
	        e.printStackTrace();
	        return "error:bpe";
	    } catch (NoSuchPaddingException e) {
	        e.printStackTrace();
	        return "error:nspe";
	    } catch (IllegalBlockSizeException e) {
	        e.printStackTrace();
	        return "error:ibse";
	    }
	} 
	
	public static void saveUser(String user, Context ct) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(ct).edit();
	    editor.putString("EssenUsername", user);
        editor.commit();
	}
	
	public static String getUser(Context ct) {
		return PreferenceManager.getDefaultSharedPreferences(ct).getString("EssenUsername", "");
	}
	
	
}
