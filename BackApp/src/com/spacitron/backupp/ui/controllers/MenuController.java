package com.spacitron.backupp.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
	
public class MenuController  extends BorderPane implements Initializable{
	@FXML
	private BorderPane borderPaneMain;
	
	@FXML
	private AnchorPane anchorPaneMain;
	
	@FXML
	private MenuBar menuBar;
	
	@FXML
	private Menu menuFile;
	@FXML
	private Menu menuEdit;
	@FXML
	protected static MenuItem itemNewBackup;
	@FXML
	protected static MenuItem itemEdit;
	@FXML
	protected static MenuItem itemProperties;
	@FXML
	protected static MenuItem itemDelete;
	@FXML
	protected static MenuItem itemRemoveFile;
	
	public MenuController(){
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			AnchorPane anchor = (AnchorPane) new FXMLLoader().load(getClass().getResource("views/ScheduleView.fxml"));
			borderPaneMain.setCenter(anchor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void onMakeShcedule(){
		try {
			new FXMLLoader().load(getClass().getResource("views/MakeScheduleView.fxml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//This will be used to display the edit views
	@FXML
	private void onEditClick(){
	}
	
	//This will be used to display the property views
	@FXML
	private void onPropertiesClick(){
	}
	
	//This will be used to delete stuff
	@FXML
	private void onDeleteClick(){
		ScheduleViewController.deleteSelected();
	}
	
	@FXML
	private void onRemoveFileClick(){
		ScheduleViewController.removeSelectedFile();
	}
	
	protected static void setEnableEdit(){
		itemEdit.setDisable(false);
	}
	
	protected static void setDisableEdit(){
		itemEdit.setDisable(true);
	}
	protected static void setEnableProperties(){
		itemProperties.setDisable(false);
	}
	protected static void setDisableProperties(){
		itemProperties.setDisable(true);
	}
	protected static void setEnableDelete(){
		itemDelete.setDisable(false);
	}
	
	protected static void setDisableDelete(){
		itemDelete.setDisable(true);
	}
	protected static void setDisableFileRemove(){
		itemRemoveFile.setDisable(true);
	}
	protected static void setEnableFileRemove(){
		itemRemoveFile.setDisable(false);
	}
}