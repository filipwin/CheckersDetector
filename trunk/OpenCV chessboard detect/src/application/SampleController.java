package application;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class SampleController implements Initializable  {
	
	@FXML
	private GridPane szachownica;
	@FXML
	public TextArea Historia;
	@FXML
	public Button DebugButton;
	@FXML
	public ImageView 
	A1,A2,A3,A4,A5,A6,A7,A8,
	B1,B2,B3,B4,B5,B6,B7,B8,
	C1,C2,C3,C4,C5,C6,C7,C8,
	D1,D2,D3,D4,D5,D6,D7,D8,
	E1,E2,E3,E4,E5,E6,E7,E8,
	F1,F2,F3,F4,F5,F6,F7,F8,
	G1,G2,G3,G4,G5,G6,G7,G8,
	H1,H2,H3,H4,H5,H6,H7,H8;
	
	@FXML
	 private void handleButtonAction(ActionEvent event) throws IOException{
		Stage stage; 
	     Parent root;
	        //get reference to the button's stage         
	        stage=(Stage) DebugButton.getScene().getWindow();
	        //load up OTHER FXML document
	        root = FXMLLoader.load(getClass().getResource("ObjRecognition.fxml"));
	      
	     
	     //create a new scene with root and set the stage
	      Scene scene = new Scene(root);
	      stage.setScene(scene);
	      stage.show();
	      System.out.println("lol");
	    }
	
	
	@FXML
	
	Image czarny = new Image ("file:///C:/Games/black.gif");
	Image bialy = new Image ("file:///C:/Games/white.gif");
	
	void updateboard(ImageView podany,int kolor){
		if(kolor==0)podany.setImage(czarny);
		if(kolor==1)podany.setImage(bialy);
	};
	
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		updateboard(E3,0);
		updateboard(C2,1);
		
		
		/*
        DebugButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
            	 Stage stage; 
                 Parent root;
                 stage=(Stage) DebugButton.getScene().getWindow();
                 //root = FXMLLoader.load(getClass().getResource("ObjRecognition.fxml"));
				
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            	System.out.print("test");
            	
            }
        });*/
	}
	
	
}
