package com.spacitron.backupp.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.spacitron.backupp.data.FileSystemFiler;
import com.spacitron.backupp.data.FilerFactory;

public class BackupControl {

	
	HashMap<String, Schedule> schedules;
	FilerFactory filerFac;
	
	public BackupControl(){
		schedules = new HashMap<String, Schedule>();
		filerFac = FilerFactory.getFilerFactory(); 
		retrieveSchedules();
	}
	
	public static void main (String[] args){
		//Name for the backup schedule.
		String scheduleName = "";
		//Folder where schedule will create backup directories.
		String outputPath = "";
		//Path of file to backup.
		String fileToBackup = "";
		//Intervals in milliseconds between backups.
		long interval = 0l;
		//Number of versions to maintain for each file.
		int versionLimit = 0;
		
		//Creates a new controller
		BackupControl c = new BackupControl();
		//Creates a new schedule
		c.addSchedule(scheduleName, outputPath, interval,versionLimit);
		//Adds file to schedule
		c.addFileToSchedule(scheduleName,fileToBackup);
		//Starts backups
		c.startSchedule(scheduleName);
		//Deletes all files and data related to this schedule 
//		c.deleteSchedule(scheduleName);
	}
	
	
	
	/**
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
	 * @param scheduleName Name of existing schedule to which files need to be added
	 * @param filePaths Absolute paths of files to be added to this schedule.
	 */
	public void addFileToSchedule(String scheduleName, String... filePaths){
		for(String filePath:filePaths){
			schedules.get(scheduleName).addMasterDocument(filePath);
		}
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
	 * @param scheduleName Name of existing schedule.
	 * 
	 * This method will begin periodic backups for the named schedule.
	 */
	public void startSchedule(String scheduleName){
		if(schedules.get(scheduleName).start()){
			System.out.println("Started");
		}else{
			System.out.println("Not Started");
		}
	}
	

	/**
	 * @param scheduleName Name of schedule to be deleted.
	 * @return True if all files and folders associated with this schedule have been removed.
	 * 
	 * Note, all filed managed by this schedule will be removed once this method is called.
	 */
	public boolean deleteSchedule(String scheduleName){
		if(schedules.get(scheduleName).delete()){
			filerFac.deleteFiler(scheduleName);
			return true;
		}
		return false;
	}

	/**
	 * This helper method re-populates this controller with stored schedules. 
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
