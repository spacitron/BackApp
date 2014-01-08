package com.spacitron.backupp.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
	
public class MenuController implements Initializable{
	@FXML
	private BorderPane borderPaneMain;
	
	@FXML
	private AnchorPane anchorPaneMain;
	
	@FXML
	private MenuBar menuBar;
	
	@FXML
	private Menu menuFile;
	
	public MenuController(){
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		menuFile.getItems().add(menuItem("Backups"));
		menuFile.getItems().add(menuItem("New Backup"));
	}
	
	private MenuItem menuItem(String g){
		final MenuItem item = new MenuItem(g);
		//ID will be used so that languages can be switched 
		item.setId(g);
		item.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					openView(item.getId());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		});
		return item;
	}
	
	
	private void openView(String name) throws IOException{
		AnchorPane anchor = null;
		switch(name){
			case"Backups":
				anchor =(AnchorPane) new FXMLLoader().load(getClass().getResource("views/ScheduleView.fxml"));
				break;
			case "New Backup":
				//This will open as a dialog and does not neecd to be added to the BorderPane
				AnchorPane dialog = (AnchorPane) new FXMLLoader().load(getClass().getResource("views/MakeScheduleView.fxml"));
				Scene s = new Scene(dialog);
				Stage stage = new Stage();
				stage.setScene(s);
				stage.show();
				break;
		}
		
		if(anchor!=null){
		borderPaneMain.getChildren().add(anchor);
		borderPaneMain.getChildren().remove(anchorPaneMain);
		}
	}
	
}