package com.spacitron.backupp.data;

import java.util.ArrayList;
import java.util.HashMap;

public class FilerFactory {
	
	public static final String TYPE = "FILER";
	protected static final String ITEMNAME = "ITEMNAME";
	protected static final String FILERTYPE = "FILERTYPE";
	protected static final String DESTINATION = "DESTINATION";
	private static FilerFactory factory = new FilerFactory();
	private DataManager localDataManager;
	
	
	/**
	 * This class will manage the creation, storage and retrieval of all types of
	 * Filer objects used in the system. The constructor method will create the local Filer table.
	 * Remote Schedule tables will be created by the specific filers if and when needed.
	 */
	private FilerFactory(){
		localDataManager = new DataManager("local");
		String[] filerColumns = new String[] {FilerFactory.FILERTYPE, FilerFactory.DESTINATION};
		localDataManager.makeTable(TYPE, filerColumns);
	}
	
	public static FilerFactory getFilerFactory(){
		return factory;
	}
	
	/**
	 * @param 	filerType: String constant indicating the type of file necessary. This is found as the static final 
	 * 			variable FILERTYPE in the filer classes.
	 * @param 	scheduleName: Name of the Schedule that the file will be assigned to.
	 * @param 	backupDestination: Output destination for the backups. Should throw an exception if the
	 * 			wrong filer is used to manage backups for a particular destination
	 * @return	Returns filer type requested.
	 */
	public Filer getFiler(String filerType, String scheduleName, String backupDestination){
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(FilerFactory.ITEMNAME, scheduleName);
		map.put(FilerFactory.FILERTYPE, filerType);
		map.put(FilerFactory.DESTINATION, backupDestination);
		localDataManager.insert(FilerFactory.TYPE,map);
		
		
		//Add more Filer types here
		switch(filerType){
		case FileSystemFiler.FILERTYPE:
			return new FileSystemFiler(backupDestination, scheduleName);
		default: return null;
		}
	}
	
	
	/**
	 * @return 	Returns array list of locally stored filers. Each filer contains the location and name of the schedule it
	 *			was originally assigned to.
	 */
	public ArrayList<Filer> retrieveFilers(){
		ArrayList<HashMap<String, String>> maps = localDataManager.getTable(TYPE);
		ArrayList<Filer> filers = new ArrayList<Filer>();
		for(HashMap<String, String> map:maps){
			String destination = map.get(DESTINATION);
			String schedule = map.get(ITEMNAME);
			String filerType = map.get(FILERTYPE);
			switch(filerType){
			case FileSystemFiler.FILERTYPE:
				filers.add(new FileSystemFiler(destination, schedule));
			}
		}
		return filers;
	}
	
	public void deleteFiler(String scheduleName){
		localDataManager.deleteRow(TYPE, scheduleName);
	}
	
	

}
