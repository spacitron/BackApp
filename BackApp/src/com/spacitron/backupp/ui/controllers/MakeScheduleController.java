package com.spacitron.backupp.ui.controllers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.spacitron.backupp.core.BackupManager;

public class MakeScheduleController extends AnchorPane implements Initializable{
	
	

	@FXML
	private AnchorPane anchorDialog;
	@FXML
	private Button buttonCancel;
	@FXML
	private Button buttonSave;
	@FXML
	private Button buttonSaveAndStart;
	@FXML
	private Button buttonDirChooser;
	@FXML
	private Button buttonFileChooser;
	@FXML
	private TextField textName;
	@FXML
	private TextField textDestination;
	@FXML
	private TextField textInterval;
	@FXML
	private TextField textVesionLimit;
	@FXML
	private ScrollBar scrollInterval;
	@FXML
	private ScrollBar scrollVersionLimit;
	@FXML
	private Label labelNameErr;
	@FXML
	private Label labelNameExistsErr;
	@FXML
	private Label labelDestErr;
	@FXML
	private Label labelNoStartErr;
	@FXML
	private Label labelNoFileErr;
	@FXML
	private ComboBox<String> comboInterval;
	@FXML
	private ListView<File> listFiles;
	
	
	
	Stage stage;
	ObservableList<File> fileList;
	FileChooser fileChooser;
	DirectoryChooser dirChooser;
	int timeMultitplier;
	
	public MakeScheduleController(){
		fileChooser = new FileChooser();
		dirChooser = new DirectoryChooser();
		stage = new Stage();
		fileList = FXCollections.observableArrayList();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		listFiles.setItems((ObservableList<File>) fileList);
		comboInterval.getItems().removeAll(comboInterval.getItems());
		comboInterval.getItems().addAll("Minutes","Hours", "Days");
		comboInterval.getSelectionModel().select(1);
		scrollInterval.setValue(12.0);
		textInterval.setText("12");
		scrollVersionLimit.setValue(1.0);
		textVesionLimit.setText("1");
		Scene s = new Scene(anchorDialog);
		stage.setScene(s);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.show();
		
	}
	
	@FXML
	private void buttonCancelPressed(){
		stage.close();
	}
	
	@FXML
	private void scollIntervalPressed(){
		int value = (int) scrollInterval.getValue();
		if(value>0){
			textInterval.setText(String.valueOf(value));
		}
	}
	@FXML
	private void scollVersionPressed(){
		int value = (int) scrollVersionLimit.getValue();
		if(value>0){
			textVesionLimit.setText(String.valueOf(value));
		}
	}
	
	@FXML
	private void decreaseInterval(){
		int interval = Integer.valueOf(textInterval.getText());
		textInterval.setText(String.valueOf(interval-1));
	}

	@FXML
	private void buttonSavePressed(){
		String name = textName.getText();
		if(makeBackup(name, false)){
			stage.close();
		}
	}
	
	@FXML
	private void buttonSaveAndStartPressed(){
		String name = textName.getText();
		if(makeBackup(name, true) && BackupManager.startSchedule(name)){
			stage.close();
		}else{
			labelNoStartErr.setVisible(true);
		}
	}

	@FXML
	private void setTimeMulitplier(){
		String select = comboInterval.getSelectionModel().getSelectedItem();
		switch(select){
		case "Minutes":timeMultitplier=60000;
		break;
		case "Hours":timeMultitplier=3600000;
		break;
		case "Days":timeMultitplier=86400000;
		}
	}
	
	private boolean makeBackup(String name, boolean start){
		boolean validDest = checkDestination();
		boolean validName = checkName();
		String destination = textDestination.getText();
		long interval = Long.valueOf(textInterval.getText()) * timeMultitplier;
		int versionLimit = Integer.valueOf(textVesionLimit.getText());
		if(validDest && validName){
			List<File> addedFiles = listFiles.getItems();
			if(addedFiles.isEmpty() && start==true){
				labelNoFileErr.setVisible(true);
				return false;
			}
			if(BackupManager.addSchedule(name, destination, interval, versionLimit)){
				ScheduleViewController.addSchedule(name);
				for(File f:addedFiles){
					BackupManager.addToSchedule(name, f.getAbsolutePath());
				}
				return true;
			}else{
				labelNameExistsErr.setVisible(true);
			}
		}
		return false;
	}
	
	
	private boolean checkDestination(){
			String dest = textDestination.getText();
			if(new File(dest).isDirectory()){
				labelDestErr.setVisible(false);;
				return true;
			}
		labelDestErr.setVisible(true);;
		return false;
	}
		
	private boolean checkName(){
		File output = new File(textName.getText());
		if(output.mkdir()){
			output.delete();
			labelNameErr.setVisible(false);
			return true;
		}
		labelNameErr.setVisible(true);
		return false;
	}
	
	@FXML
	private void chooseDir(){
		Stage stageDirChooser = new Stage();
		dirChooser.setTitle("Select the output destination");
		File outDest = dirChooser.showDialog(stageDirChooser);
		if(!(outDest==null)){
			dirChooser.setInitialDirectory(outDest);
			labelDestErr.setVisible(false);
			textDestination.setText(outDest.getAbsolutePath());
		}
	}

	@FXML
	private void chooseFiles(){
		Stage stageFileChooser = new Stage();
		fileChooser.setTitle("Select files for backup");
		List<File> files = fileChooser.showOpenMultipleDialog(stageFileChooser);
		if(!(files == null)){
			for(File file:files){
				if(!fileList.contains(file)){
					fileChooser.setInitialDirectory(files.get(0).getParentFile());
					fileList.addAll(files);
				}
				labelNoFileErr.setVisible(false);
				labelNoStartErr.setVisible(false);
			}
		}
	}
	
	@FXML
	private void removeErrorName(){
		labelNameExistsErr.setVisible(false);
		labelNameErr.setVisible(false);
	}
	@FXML
	private void removeErrorDir(){
		labelDestErr.setVisible(false);
	}
	
}
