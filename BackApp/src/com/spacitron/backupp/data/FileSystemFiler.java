package com.spacitron.backupp.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import com.spacitron.backupp.core.DataStorable;
import com.spacitron.backupp.core.Document;
import com.spacitron.backupp.core.Filer;
import com.spacitron.backupp.core.Schedule;

public class FileSystemFiler implements Filer {
	
	public static final String FILERTYPE = "FILESYSTEMFILER";
	
	String[] schColumns;
	String[] docColumns;
	String[] filerColumns;
	
	DataManager remoteDataManager;
	String dest;
	String destination;
	String docDest;
	String dataDest;
	
	
	public FileSystemFiler(String dest, String scheduleName){
		this.dest = dest;
		this.destination= dest+"\\"+scheduleName;
		docDest = destination+"\\docs";
		dataDest = destination+"\\meta";
		new File(destination).mkdir();
		new File(docDest).mkdir();
		new File(dataDest).mkdir();
		
		schColumns = new String[] {Schedule.INTERVAL, Schedule.VERSIONLIMIT, Schedule.DATECREATED};
		docColumns = new String[]{Document.ORIGINALLOCATION, Document.PARENT, Document.VERSION};
		remoteDataManager = new DataManager(dataDest+"\\"+scheduleName);
		remoteDataManager.makeTable(Document.TYPE, docColumns);
		remoteDataManager.makeTable(Schedule.TYPE, schColumns);
	}
	
	
	@Override
	public boolean store(ArrayList<DataStorable> dataStorables) {
		ArrayList<HashMap<String, String>> maps = new ArrayList<>();
		for(DataStorable dataStorable: dataStorables){
			if(dataStorable.getType().equals(Document.TYPE)){
				Document d = (Document)dataStorable;
				this.copyFile(d.getOriginalPath(), docDest+"\\"+d.getItemName());
			}
				maps.add(dataStorable.getData());
		}
			remoteDataManager.insert(Document.TYPE, maps);
		return false;
	}


	@Override
	public boolean store(DataStorable dataStorable) {
		String type = dataStorable.getType();
		if(type.equals(Document.TYPE)){
			Document d = (Document) dataStorable;
			copyFile(d.getOriginalPath(), docDest+"\\"+ d.getItemName());
		}
		remoteDataManager.insert(type, dataStorable.getData());
		return false;
	}


	@Override
	public boolean update(DataStorable dataStorable) {
			remoteDataManager.update(dataStorable.getType(), dataStorable.getItemName(), dataStorable.getData());
		return false;
	}


	@Override
	public boolean update(ArrayList<DataStorable> dataStorables) {
		ArrayList<HashMap<String, String>> maps = new ArrayList<>();
		ArrayList<String> names = new ArrayList<>();
		String type="";
		for(DataStorable d: dataStorables){
				maps.add(d.getData());
				names.add(d.getItemName());
				type = Document.TYPE;
		}
			remoteDataManager.update(type, names, maps);
		return false;
	}


	@Override
	public boolean delete(DataStorable dataStorable){
		String type = dataStorable.getType();
		if(type.equals(Document.TYPE)){
			if(new File(docDest+"\\"+dataStorable.getItemName()).delete()){
					remoteDataManager.deleteRow(type, dataStorable.getItemName());
					return true;
			}
		}else if(type.equals(Schedule.TYPE)){
			File docDir = new  File(docDest);
			File[] docs = docDir.listFiles();
			for(File f: docs){
				f.delete();
			}
			docDir.delete();
			File dataDir = new File(dataDest);
			File[] data = dataDir.listFiles();
			for(File f: data){
				f.delete();
			}
			dataDir.delete();
		 	return new File(destination).delete();
		}
		return false;
	}



	@Override
	public boolean delete(ArrayList<DataStorable> dataStorables) {
		ArrayList<String> items = new ArrayList<>();
		String type = "";
		for(DataStorable d:dataStorables){
			if(d.getType().equals(Document.TYPE)){
				if(!new File(docDest+"\\"+d.getItemName()).delete()){
					return false;
				}
			}else if(d.getType().equals(Schedule.TYPE)){
				if(!delete(d)){
					return false;
				}
			}
			type = d.getType();
			items.add(d.getItemName());
		}
		remoteDataManager.deleteRows(type, items);
		return true;
	}

	
	@Override
	public HashMap<String, String> getDataMap(String type, String itemName) {
		return remoteDataManager.getRow(type, itemName);
	}


	@Override
	public ArrayList<HashMap<String, String>> getDataMaps(String type) {
		return remoteDataManager.getTable(type);
	}

	private void copyFile(String origin,String destination) {
		
		try {
			FileUtils.copyFile(new File(origin), new File(destination));
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
