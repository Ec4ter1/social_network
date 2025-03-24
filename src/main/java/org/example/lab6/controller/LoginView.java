package org.example.lab6.controller;

import domain.Utilizator;
import domain.validators.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.lab6.HelloApplication;
import org.example.lab6.Main;
import org.example.lab6.MainView;
import org.mindrot.jbcrypt.BCrypt;
import service.ServiceApp;

import java.io.IOException;
import java.util.Objects;

public class LoginView {

    @FXML
    public javafx.scene.control.TextField usernameField1;
    public javafx.scene.control.TextField passwordField1;
    public AnchorPane signUpAnchor;
    public AnchorPane signInAnchor;
    public Button signUpButton1;
    public TextField numeTextField2;
    public TextField prenumeTextField3;
    public TextField parolaTextField2;
    public TextField usernameTextField2;


    private ServiceApp service;
    private Utilizator currentUser;

    public void setUtilizatorService(ServiceApp service) {
        this.service = service;
    }

    @FXML
    public void onSignInLoginButtonAction(){

        String username = usernameField1.getText();
        String password = passwordField1.getText();
        currentUser = service.findUtilizatoriByUserName(username);
        //TODO: cu verificare parola
        if(currentUser == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.show();
        }
        else {
            try {
                System.out.println("a");
                if (BCrypt.checkpw(password, currentUser.getPassword()))
                {
                    System.out.println("b");
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/org/example/lab6/main-view.fxml"));
                    AnchorPane userLayout = loader.load();
                    Scene scene = new Scene(userLayout);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle(currentUser.getFirstName() + " " + currentUser.getLastName());
                    MainView userController = loader.getController();
                    userController.setUtilizatorService(service, currentUser);
                    stage.show();
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.show();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onSignUpLoginButtonAction(ActionEvent actionEvent) {
        signUpAnchor.setVisible(true);
        signInAnchor.setVisible(false);
        signUpButton1.setVisible(false);
    }

    public void signInLoginButtonAnchor2(ActionEvent actionEvent) {
        String username = usernameTextField2.getText();
        String nume = numeTextField2.getText();
        String prenume = prenumeTextField3.getText();
        String parola = parolaTextField2.getText();

        String parolaHashuita = BCrypt.hashpw(parola, BCrypt.gensalt());
        boolean este = service.adaugaUtilizator(nume, prenume, username, parolaHashuita);
        currentUser = service.findUtilizatoriByUserName(username);
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/org/example/lab6/main-view.fxml"));
            AnchorPane userLayout = loader.load();
            Scene scene = new Scene(userLayout);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(currentUser.getFirstName() + " " + currentUser.getLastName());
            MainView userController = loader.getController();
            userController.setUtilizatorService(service, currentUser);
            stage.show();

        }
        catch (IOException e) {
            throw new RuntimeException(e);
            }
    }
}
