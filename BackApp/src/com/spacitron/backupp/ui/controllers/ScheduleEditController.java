package com.spacitron.backupp.ui.controllers;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.spacitron.backupp.core.BackupManager;
import com.spacitron.backupp.core.Schedule;

public class ScheduleEditController implements Initializable{

	@FXML
	private AnchorPane anchorDialog;
	@FXML
	private Label labelScheduleName;
	@FXML
	private Label labelDest;
	@FXML
	private Label labelDateCreated;
	@FXML
	private Label labelStarted;
	@FXML
	private TextField textInterval;
	@FXML
	private TextField textVesionLimit;
	@FXML
	private ScrollBar scrollInterval;
	@FXML
	private ScrollBar scrollVersionLimit;
	@FXML
	private ComboBox<Text> comboInterval; 
	@FXML
	private Button buttonSave;
	@FXML
	private Button buttCancel;
	
	private Text days;
	private Text hours;
	private Text minutes;
	private int timeMultiplier; 
	
	private static String scheduleName;
	
	private Stage stage;
	
	
	public ScheduleEditController(){
		stage = new Stage();
		days = new Text("Days");
		days.setId("86400000");
		hours = new Text("Hours");
		hours.setId("3600000");
		minutes = new Text("Minutes");
		minutes.setId("3600000");
		timeMultiplier = 0;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		HashMap<String, String> map = BackupManager.getScheduleData(scheduleName);
		labelScheduleName.setText(scheduleName);
		labelDest.setText(map.get(Schedule.DESTINATION));
		
		String dateString = map.get(Schedule.DATECREATED);
		Date date = new Date(Long.valueOf(dateString));
		SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yy 'at' hh.mm.ss a");
		labelDateCreated.setText(formatDate.format(date));
		
		if(BackupManager.scheduleIsStarted(scheduleName)){
			labelStarted.setText("Running");
		}else{
			labelStarted.setText("Stopped");
		}

		textVesionLimit.setText(map.get(Schedule.VERSIONLIMIT));
		scrollVersionLimit.setValue(Long.valueOf(map.get(Schedule.VERSIONLIMIT)));
		
		Scene s = new Scene(anchorDialog);
		stage.setScene(s);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.show();

		comboInterval.getItems().removeAll(comboInterval.getItems());
		comboInterval.getItems().addAll(days, hours, minutes);
		makeInterval(Long.valueOf(map.get(Schedule.INTERVAL)));
		
	}
	
	
	@FXML
	private void onClickScrollInterval(){
		textInterval.setText(String.valueOf((int) scrollInterval.getValue()));
	}
	@FXML
	private void onClickScrollVersion(){
		textVesionLimit.setText(String.valueOf((int) scrollVersionLimit.getValue()));
	}

	@FXML
	private void setTimeMultiplier(){
		Text text = comboInterval.getSelectionModel().getSelectedItem();
		timeMultiplier = Integer.valueOf(text.getId());
	}
	@FXML
	private void onSaveButtClick(){
		int version = Integer.valueOf(textVesionLimit.getText());
		int interval = Integer.valueOf(textInterval.getText());
		long newInterval = interval*timeMultiplier;
		BackupManager.setScheduleInterval(scheduleName, newInterval);
		BackupManager.setScheduleVersionLimit(scheduleName,version);
		stage.close();
	}
	
	@FXML
	private void onCancelButtClick(){
		stage.close();
	}
	
	//Method called by the controller that calls this view.
	protected static void setScheduleName(String name){
		scheduleName= name;
	}
	
	protected void makeInterval(long time){
		long g = 0;
		if((g =time/86400000)>1 && g%1 ==0){
			comboInterval.getSelectionModel().select(days);
			textInterval.setText(String.valueOf((int)g));
			scrollInterval.setValue(g);
			return;
		}
		if((g =time/3600000)>1 && g %1 == 0){
			comboInterval.getSelectionModel().select(hours);
			textInterval.setText(String.valueOf((int)g));
			scrollInterval.setValue(g);
			return;
		}
		if((g =time/60000)>1 && g %1 ==0){
			comboInterval.getSelectionModel().select(minutes);
			textInterval.setText(String.valueOf((int)g));
			scrollInterval.setValue(g);
			return;
		}
	}

}
