package com.spacitron.backupp.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MasterDocument extends Document {

	protected MasterDocument(String scheduleName, File file) {
		super(scheduleName, file.getAbsolutePath(), getHash(file) + file.lastModified(), 0);
	}
	
	/**
	 * @return = returns SHA1 hash for the file. This will be the unique file name in the backup folder 
	 */
	private static String getHash(File file) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA1");
			FileInputStream inStream = new FileInputStream(file);
			DigestInputStream dis = new DigestInputStream(inStream, md);
			BufferedInputStream bis = new BufferedInputStream(dis);
			while (true) {
				int b = bis.read();
				if (b == -1)
					break;
			}
			bis.close();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		
		BigInteger bi = new BigInteger(md.digest());
		return bi.toString(16);
	}
}
