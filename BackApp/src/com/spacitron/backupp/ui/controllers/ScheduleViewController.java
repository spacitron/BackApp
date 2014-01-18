package com.spacitron.backupp.ui.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.spacitron.backupp.core.BackupManager;
import com.spacitron.backupp.core.BackupObserver;
import com.spacitron.backupp.core.Schedule;

public class ScheduleViewController implements Initializable, BackupObserver{
	
	protected static final String SCHEDULE = "SCHEDULE";
	protected static final String DOCUMENT = "DOCUMENT";
	private static String selection;
	
	//Static variables will hold the names of backup schedule and file currently selected
	private static String scheduleSelected;
	private static String docSelected;
	
	static BackupManager backupManager;
	private FileChooser fileChooser;
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
	private static Label labelNoScheduleErr;
	@FXML
	private static ListView<String> listScheduleNames;
	@FXML
	private static ListView<String> listDocNames;
	@FXML
	private static Button buttonStartSchedule;
	@FXML
	private static Button buttonStopSchedule;
	@FXML
	private static Button buttonAddFiles;
	
	public ScheduleViewController(){
		backupManager = BackupManager.getBackupManagerSingleton();
		backupManager.registerObserver(this);
		scheduleList = FXCollections.observableArrayList(backupManager.getScheduleNames());
		docNamesList = FXCollections.observableArrayList();
		selection = new String();
		scheduleSelected = new String();
		docSelected = new String();
		fileChooser = new FileChooser();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Collections.sort(scheduleList);
		listScheduleNames.setItems(scheduleList);
		listDocNames.setItems(docNamesList);
	}

	@Override
	public void alertObserver() {
		Set<String> scheduleSet =  backupManager.getScheduleNames();
		if(scheduleSet.size()>scheduleList.size()){
			for(String g: scheduleSet){
				if(!scheduleList.contains(g)){
					scheduleList.add(g);
					scheduleSelected = g;
				}
			}
		}
		
		scheduleList.removeAll(scheduleList);
		scheduleList.addAll(scheduleSet);
		Collections.sort(scheduleList);
		
		if(scheduleList.contains(scheduleSelected)){
			listScheduleNames.getSelectionModel().select(scheduleSelected);
			docNamesList.removeAll(docNamesList);
			docNamesList.addAll(backupManager.getMasterDocuments(scheduleSelected));
			Collections.sort(docNamesList);
			initializeInfoLabels();
			setInfoLabels(scheduleSelected);
			setStartStopButtons(scheduleSelected);
		}else{
			killInfoLabels();
			docNamesList.removeAll(docNamesList);
		}
	}
	
	@FXML
	private void onScheduleSelected(){
		//Disables menu actions performed on files
		MenuController.setDisableFileRemove();
		
		//Sets the current item on which menu operations will be performed
		if((scheduleSelected = listScheduleNames.getSelectionModel().getSelectedItem())!=null){
			labelNoScheduleErr.setVisible(false);
			selection = SCHEDULE;
			scheduleSelected = listScheduleNames.getSelectionModel().getSelectedItem();
			initializeInfoLabels();
			setInfoLabels(scheduleSelected);
			setStartStopButtons(scheduleSelected);
			
			//Updates the document fields
			docNamesList.removeAll(docNamesList);
			docNamesList.addAll(backupManager.getMasterDocuments(scheduleSelected));
			Collections.sort(docNamesList);
			docSelected = new String();
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
		docSelected = listDocNames.getSelectionModel().getSelectedItem();
		selection = DOCUMENT;
	}

	@FXML
	private void startSchedule(){
		String scheduleName = listScheduleNames.getSelectionModel().getSelectedItem();
		if(!backupManager.startSchedule(scheduleName)){
			labelScheduleEmpty.setVisible(true);
		}else{
			buttonStartSchedule.setDisable(true);
			buttonStopSchedule.setDisable(false);
		}
		
	}
	@FXML
	private void stopSchedule(){
		String scheduleName = listScheduleNames.getSelectionModel().getSelectedItem();
		backupManager.stopSchedule(scheduleName);
		buttonStartSchedule.setDisable(false);
		buttonStopSchedule.setDisable(true);
	}
	
	@FXML
	private void addFiles(){
		//Checks that a schedule is selected to add files to
		if(scheduleSelected.length()<1){
			labelNoScheduleErr.setVisible(true);
			return;
		}
		Stage stageFileChooser = new Stage();
		fileChooser.setTitle("Select files for backup");
		List<File> files = fileChooser.showOpenMultipleDialog(stageFileChooser);
		if(!(files == null)){
			for(File file:files){
				if(!docNamesList.contains(file)){
					String filePath = file.getAbsolutePath();
					backupManager.addToSchedule(scheduleSelected, filePath);
					fileChooser.setInitialDirectory(files.get(0).getParentFile());
				}
			}
		}
	}
	
	protected static void removeSelectedFile() {
		String doc = listDocNames.getSelectionModel().getSelectedItem();
		backupManager.removeFilesFromSchedule(scheduleSelected, doc);
	}

	public static void editSelected() {
		if(selection.equals(SCHEDULE)){
			try {
				ScheduleEditController.setScheduleName(scheduleSelected);
				new FXMLLoader().load(ScheduleViewController.class.getResource("views/ScheduleEditView.fxml"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else if(selection.equals(DOCUMENT)){
			
		}
	}

	//Will need to ask for confirmation first
	protected static void deleteSelected() {
		if(selection.equals(SCHEDULE)){
			backupManager.deleteSchedule(scheduleSelected);
			killInfoLabels();
			scheduleSelected = new String();
			docSelected = new String();
		}else if(selection.equals(DOCUMENT)){
			backupManager.removeFiles(scheduleSelected, docSelected);
			docSelected = new String();
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
		HashMap<String, String> map = backupManager.getScheduleData(scheduleName);
		labelDest.setText(map.get(Schedule.DESTINATION));
		labelInterval.setText(getTimeLabel(map.get(Schedule.INTERVAL)));
		labelVersion.setText(map.get(Schedule.VERSIONLIMIT));
		String dateString = map.get(Schedule.DATECREATED);
		Date date = new Date(Long.valueOf(dateString));
		SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yy 'at' hh.mm.ss a");
		labelDateCreated.setText(formatDate.format(date));
	}
	
	private void setStartStopButtons(String scheduleName){
		if(backupManager.scheduleIsStarted(scheduleName)){
			buttonStartSchedule.setDisable(true);
			buttonStopSchedule.setDisable(false);
		}else{
			buttonStartSchedule.setDisable(false);
			buttonStopSchedule.setDisable(true);
		}
	}
	
	private String getTimeLabel(String interval){
		long time = Long.valueOf(interval);
		long g = 0;
		if((g =time/86400000)>1 && g%1 ==0){
			return g+ " days";
		}
		if((g =time/3600000)>1 && g %1 == 0){
			return g+ " hours";
		}
		if((g =time/60000)>1 && g %1 ==0){
			return g+ " minutes";
		}
		return null;
	}
	
}
