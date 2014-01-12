package com.spacitron.backupp.core;

import java.io.File;
import java.util.HashMap;

import com.spacitron.backupp.data.DataStorable;

public class Document implements DataStorable{
	
	private HashMap<String, String> itemData = new HashMap<String, String>();	
	private File file;
	public final static String TYPE = "DOCUMENT";
	final static String ITEMNAME = "ITEMNAME";
	public final static String ORIGINALLOCATION = "ORIGINALLOCATION";
	public final static String VERSION = "VERSION";
	
	protected Document(String originalLocation, String itemName, int version){
		itemData.put(ITEMNAME, itemName);
		itemData.put(ORIGINALLOCATION, originalLocation);
		itemData.put(VERSION, Integer.toString(version));
	}

	@Override
	public String getType(){
		return TYPE;
	}

	@Override
	public HashMap<String, String> getData(){
		return itemData;
	}

	@Override
	public String getItemName() {
		return itemData.get(ITEMNAME);
	}
	
	//This method needs to be public to allow for manipulation of the document's file by the Filer.
	public String getOriginalPath(){
		return itemData.get(ORIGINALLOCATION);
	}

	protected int getVersion(){
		String v = itemData.get(VERSION);
		int version = Integer.valueOf(v);
		return version;
	}

	protected void updateVersion(int versionChange){
		int version = getVersion();
		version+=versionChange;
		String v = String.valueOf(version);
		itemData.remove(VERSION);
		itemData.put(VERSION, v);
	}

	protected long getLastModified(){
		return file.lastModified();
	}
	
	protected boolean isBackupOf(Document doc){
		boolean retu = doc.getOriginalPath().equals(this.getOriginalPath());
		return retu;
	}
	
	protected boolean equals(Document doc){
		return doc.getItemName().equals(this.getItemName());
	}
}
