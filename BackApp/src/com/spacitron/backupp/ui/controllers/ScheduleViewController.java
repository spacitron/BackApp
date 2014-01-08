package com.spacitron.backupp.ui.controllers;

import java.util.HashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;

import com.spacitron.backupp.core.BackupControl;

public class ScheduleViewController extends AnchorPane{
	
	BackupControl bc;
	
	@FXML
	private Accordion scheduleAccordion;
	@FXML
	private AnchorPane detailAnchor;
	@FXML
	private TableView<HashMap<String, String>> scheduleDataTable;
	@FXML
	private TableColumn<HashMap<String, String>,String> propertyiesColumn;
	@FXML
	private TableColumn<HashMap<String, String>,String> detailsColumn;
	
	public ScheduleViewController(){
		bc = new BackupControl();
	}
	
	
	@FXML
	private void initialize() {
		bc.addSchedule("ciao","C:\\Users\\paolo\\Desktop\\py", 1000,2);
		bc.addSchedule("POLLo","C:\\Users\\paolo\\Desktop\\py", 1000,2);
		bc.addSchedule("poppio","C:\\Users\\paolo\\Desktop\\py", 1000,2);
		ObservableList<String> scheduleList = FXCollections.observableArrayList(bc.getScheduleNames()); 
		
		for(String name:scheduleList){
			scheduleAccordion.getPanes().add(buildTitledPane(name));
		}
		scheduleAccordion.setVisible(true);
	}
	
	private TitledPane buildTitledPane(final String text){
		final TitledPane tp = new TitledPane();
		tp.setText(text);
		tp.expandedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if(tp.isExpanded()){
				getScheduleData(tp);
				}
			}
		});
		return tp;
	}
	
	
	@FXML
	private void getScheduleData(TitledPane tp){
		AnchorPane detailAnchor = null;
		try {
			detailAnchor = (AnchorPane)FXMLLoader.load(getClass().getResource("ScheduleDetail.fxml"));
			tp.setContent(detailAnchor);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
