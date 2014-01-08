package com.spacitron.backupp.ui.controllers;
/*package com.spacitron.backupp.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.spacitron.backupp.core.BackupControl;

public class Demo {
	
	BackupControl control;
	String demoSchedule;
	
	public Demo(String demoScheduleName){
		demoSchedule = demoScheduleName;
		control = new BackupControl();
	}
	
//	public static void main(String[] args){

		//Fill in path to where the demo should create the backup directories.
		String demoDestination = "";
		//Fill in path of file to be backed up by the demo.
		String demoFilePath = "";
		
		String demoName = "ciao";
		Demo demo = new Demo(demoName);
		
//		demo.createAndStartSchedule(demoName, demoDestination, demoFilePath);
		//Print the data from files associated with demo schedule, provided there are any files to be shown.
//		demo.printBackupData();	
		//Delete the file with the specified hash name. The hash name can be taken by running the printBackupData() method.
//		demo.deleteBackedUpFile(fileHash);
		//Deletes the demo schedule. 
//		demo.deleteSchedule();
		
//	}
	
	public void createAndStartSchedule(String name, String destination, String...filePath){
		control.addSchedule(name, destination, 3000l,2);
		control.addToSchedule(name, filePath);
		control.startSchedule(name);
		}
	

	public void printBackupData(){
		ArrayList<HashMap<String, String>> maps = control.getBackupData(demoSchedule);
		for(HashMap<String, String> map: maps){
			Set<String> set = map.keySet();
			for(String key: set){
				System.out.println(key+"= "+map.get(key));
			}
			System.out.println();
		}
	}
	
	public void deleteBackedUpFile(String fileHash){
		control.deleteBackups(demoSchedule, fileHash);
	}
	
	public void deleteSchedule(){
		control.deleteSchedule(demoSchedule);
	}
	
	

}

*/