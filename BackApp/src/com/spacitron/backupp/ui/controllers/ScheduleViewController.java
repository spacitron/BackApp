package com.spacitron.backupp.ui.controllers;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import com.spacitron.backupp.core.BackupManager;
import com.spacitron.backupp.core.Schedule;

public class ScheduleViewController implements Initializable{
	
	protected static final String SCHEDULE = "SCHEDULE";
	protected static final String DOCUMENT = "DOCUMENT";
	private static String selection;
	private static String scheduleName;
	private static String docName;
	
	BackupManager bc;
	static ObservableList<String> scheduleList;
	static ObservableList<String> docNamesList;
	
	@FXML
	private static Label labelDest;
	@FXML
	private static Label labelVersion;
	@FXML
	private static Label labelInterval;
	@FXML
	private static Label labelDateCreated;
	@FXML
	private static Label labelDestCap;
	@FXML
	private static Label labelVersionCap;
	@FXML
	private static Label labelIntervalCap;
	@FXML
	private static Label labelScheduleEmpty;
	@FXML
	private static Label labelDateCreatedCap;
	@FXML
	private static ListView<String> listScheduleNames;
	@FXML
	private static ListView<String> listDocNames;
	@FXML
	private static Button buttonStartSchedule;
	@FXML
	private static Button buttonStopSchedule;
	
	public ScheduleViewController(){
		BackupManager.addSchedule("ciao", "C:\\Users\\paolo\\Desktop", 1000l, 3);
		BackupManager.addToSchedule("ciao", "C:\\Users\\paolo\\Desktop\\intern_application.txt");
		BackupManager.addToSchedule("ciao", "C:\\Users\\paolo\\Desktop\\CV-PM.doc");
		scheduleList = FXCollections.observableArrayList(BackupManager.getScheduleNames());
		docNamesList = FXCollections.observableArrayList();
		selection = new String();
		scheduleName = new String();
		docName = new String();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Collections.sort(scheduleList);
		listScheduleNames.setItems(scheduleList);
		listDocNames.setItems(docNamesList);
	}
	
	@FXML
	private void onScheduleSelected(){
		MenuController.setDisableFileRemove();
		//Sets the current item on which menu operations will be performed
		if((scheduleName = listScheduleNames.getSelectionModel().getSelectedItem())!=null){
			selection = SCHEDULE;
			scheduleName = listScheduleNames.getSelectionModel().getSelectedItem();
			initializeInfoLabels();
			setInfoLabels(scheduleName);
			setStartStopButtons(scheduleName);
			
			//Updates the document fields
			docNamesList.removeAll(docNamesList);
			docNamesList.addAll(BackupManager.getMasterDocuments(scheduleName));
			docName = new String();
			MenuController.setEnableDelete();
			MenuController.setEnableEdit();
			MenuController.setEnableProperties();
		}else{
			killInfoLabels();
			MenuController.setDisableDelete();
			MenuController.setDisableEdit();
			MenuController.setDisableProperties();
		}
	}
	
	@FXML
	private void onDocumentSelected(){
		MenuController.setEnableFileRemove();
		docName = listDocNames.getSelectionModel().getSelectedItem();
		selection = DOCUMENT;
	}

	@FXML
	private void startSchedule(){
		String scheduleName = listScheduleNames.getSelectionModel().getSelectedItem();
		if(!BackupManager.startSchedule(scheduleName)){
			labelScheduleEmpty.setVisible(true);
		}else{
			buttonStartSchedule.setDisable(true);
			buttonStopSchedule.setDisable(false);
		}
		
	}
	@FXML
	private void stopSchedule(){
		String scheduleName = listScheduleNames.getSelectionModel().getSelectedItem();
		BackupManager.stopSchedule(scheduleName);
		buttonStartSchedule.setDisable(false);
		buttonStopSchedule.setDisable(true);
	}
	
	protected static void addSchedule(String scheduleName){
		scheduleList.add(scheduleName);
		Collections.sort(scheduleList);
		int i = scheduleList.indexOf(scheduleName);
		listScheduleNames.getSelectionModel().select(i);
		listScheduleNames.scrollTo(i);
	}
	
	protected static void removeSelectedFile() {
		String doc = listDocNames.getSelectionModel().getSelectedItem();
		BackupManager.removeFilesFromSchedule(scheduleName, doc);
		docNamesList.remove(doc);
	}

	//Will need to ask for confirmation first
	protected static void deleteSelected() {
		if(selection.equals(SCHEDULE)){
			BackupManager.deleteSchedule(scheduleName);
			scheduleList.remove(scheduleName);
			docNamesList.removeAll(docNamesList);
			killInfoLabels();
			scheduleName = new String();
			docName = new String();
		}else if(selection.equals(DOCUMENT)){
			BackupManager.removeFiles(scheduleName, docName);
			docNamesList.remove(docName);	
			docName = new String();
		}
		
	}
	
	private void initializeInfoLabels(){
		labelScheduleEmpty.setVisible(false);
		buttonStartSchedule.setVisible(true);
		buttonStopSchedule.setVisible(true);
		labelDateCreatedCap.setVisible(true);
		labelDestCap.setVisible(true);
		labelIntervalCap.setVisible(true);
		labelVersionCap.setVisible(true);
		labelDest.setVisible(true);
		labelInterval.setVisible(true);
		labelDateCreated.setVisible(true);
		labelVersion.setVisible(true);
	}
	private static void killInfoLabels(){
		labelScheduleEmpty.setVisible(false);
		buttonStartSchedule.setVisible(false);
		buttonStopSchedule.setVisible(false);
		labelDateCreatedCap.setVisible(false);
		labelDestCap.setVisible(false);
		labelIntervalCap.setVisible(false);
		labelVersionCap.setVisible(false);
		labelDest.setVisible(false);
		labelInterval.setVisible(false);
		labelDateCreated.setVisible(false);
		labelVersion.setVisible(false);
	}
	
	private void setInfoLabels(String scheduleName){
		HashMap<String, String> map = BackupManager.getScheduleData(scheduleName);
		labelDest.setText(map.get(Schedule.DESTINATION));
		labelInterval.setText(map.get(Schedule.INTERVAL));
		labelVersion.setText(map.get(Schedule.VERSIONLIMIT));
		String dateString = map.get(Schedule.DATECREATED);
		Date date = new Date(Long.valueOf(dateString));
		SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yy 'at' hh.mm.ss a");
		labelDateCreated.setText(formatDate.format(date));
	}
	
	private void setStartStopButtons(String scheduleName){
		if(BackupManager.scheduleIsStared(scheduleName)){
			buttonStartSchedule.setDisable(true);
			buttonStopSchedule.setDisable(false);
		}else{
			buttonStartSchedule.setDisable(false);
			buttonStopSchedule.setDisable(true);
		}
	}
	
	

}
