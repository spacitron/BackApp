package com.spacitron.backupp.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.spacitron.backupp.data.FileSystemFiler;
import com.spacitron.backupp.data.FilerFactory;

public class BackupControl {

	
	HashMap<String, Schedule> schedules;
	FilerFactory filerFac;
	
	/**
	 * This class provides a communication point between the lower layers and the user interface. 
	 */
	public BackupControl(){
		schedules = new HashMap<String, Schedule>();
		filerFac = FilerFactory.getFilerFactory(); 
		retrieveSchedules();
	}
		
	/**
	 * Creates a new backup schedule associate with the appropriate Filer object depending on where the 
	 * schedule needs to be stored. 
	 * 
	 * @param name Unique string that will be used to identify the schedule.
	 * @param destination Location where files will be saved.
	 * @param interval Interval - in milliseconds - between backups.
	 * @param versionLimit Maximum number of copies for each file managed by this schedule.
	 * @return Returns false if the output destination selected cannot be reached.
	 */
	public boolean addSchedule(String name, String destination, long interval, int versionLimit){
		Filer filer;
		//Other checks will be performed here to decide what type of filer to request.
		if(new File(destination).isDirectory()){
			filer = filerFac.getFiler(FileSystemFiler.FILERTYPE, name, destination);
		}else{
			return false;
		}
		Schedule schedule = new Schedule(name, interval, versionLimit, filer);
		schedules.put(name, schedule);
		return true;
	}
	
	/**
	 * Adds files to the backup schedule.
	 * 
	 * @param scheduleName Name of existing schedule to which files or directories need to be added for backup
	 * @param filePaths Absolute paths of files to be added to this schedule.
	 */
	public void addToSchedule(String scheduleName, String... filePaths){
		for(String filePath:filePaths){
			schedules.get(scheduleName).addMaster(filePath);
		}
	}
	
	/**
	 * @param scheduleName Name of existing schedule.
	 * @return Returns data maps of each of the files stored in the backup managed by the named schedule. 
	 */
	public ArrayList<HashMap<String, String>> getBackupData(String scheduleName){
		return schedules.get(scheduleName).getBackupMaps();
	}
	
	/**
	 * @param scheduleName Name of the schedule for which data is requested.
	 * @return Data map for display purposes.
	 */
	public HashMap<String, String> getScheduleData(String scheduleName){
		return schedules.get(scheduleName).getData();		
	}
	
	//This method needs to be overloaded in order to give the user the opportunity to decide what to do with the stored files
	public void removeFilesFromSchedule(String scheduleName, String... filePaths){
		for(String filePath:filePaths){
			schedules.get(scheduleName).removeMasterDocument(filePath);
		}
	}

	/**
	 * This method will begin periodic backups for the named schedule.
	 * 
	 * @param scheduleName Name of existing schedule.
	 */
	public void startSchedule(String scheduleName){
		if(schedules.get(scheduleName).start()){
			System.out.println("Started");
		}else{
			System.out.println("Not Started");
		}
	}
	
	/**
	 * This method will end the periodic backups for the named schedule.
	 * 
	 * @param scheduleName Name of existing schedule.
	 */
	public void stopSchedule(String scheduleName){
		schedules.get(scheduleName).stop();
	}
	

	/**
	 * Removes files from backup schedule.
	 * 
	 * @param scheduleName Name of existing schedule.
	 * @param filePaths Paths of files tracked by above schedule.
	 */
	public boolean removeFiles(String scheduleName, String...filePaths){
		Schedule schedule = schedules.get(scheduleName);
		for(String path: filePaths){
			schedule.removeMasterDocument(path);
		}
		return false;
	}
	
	/**
	 * Deletes the selected backups for the named schedule. 
	 * 
	 * @param scheduleName Name of existing schedule.
	 * @param docNames Path of the stored documents to be deleted.
	 * @return
	 */
	public boolean deleteBackups(String scheduleName, String...docNames){
		Schedule schedule = schedules.get(scheduleName);
		for(String name:docNames){
			schedule.deleteStoredDocument(name);
		}
		return false;
	}
	
	
	/**
	 * Deletes a schedule and all files and directories associated with it.
	 * 
	 * @param scheduleName Name of schedule to be deleted.
	 * @return True if all files and folders associated with this schedule have been removed.
	 */
	public boolean deleteSchedule(String scheduleName){
		if(schedules.get(scheduleName).delete()){
			filerFac.deleteFiler(scheduleName);
			return true;
		}
		return false;
	}

	/**
	 * Helper method that re-populates this controller with stored schedules. 
	 */
	private void retrieveSchedules(){
		ArrayList<Filer> filers = filerFac.retrieveFilers();
		for(Filer filer: filers){
			ArrayList<HashMap<String,String>> maps = filer.getDataMaps(Schedule.TYPE);
			if(maps.size()>0){
				HashMap<String, String> map = maps.get(0);
				String name = map.get(Schedule.ITEMNAME);
				long interval = Long.valueOf(map.get(Schedule.INTERVAL));
				int versionLimit = Integer.valueOf(map.get(Schedule.VERSIONLIMIT));
				Schedule schedule =  new Schedule(name, interval, versionLimit, filer);
				schedules.put(name, schedule);
			}
		}
	}
}
