package org.example.lab6;

import domain.Message;
import domain.Prietenie;
import domain.validators.MessageValidator;
import domain.validators.PrietenieValidator;
import domain.validators.UtilizatorValidator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import domain.Utilizator;
import org.example.lab6.controller.LoginView;
import repository.PrieteniRepository;
import repository.Repository;
import repository.database.MessageDBRepository;
import repository.database.PrieteniDBRepository;
import repository.database.UtilizatorDBRepository;
import service.ServiceApp;

import java.io.IOException;

public class HelloApplication extends Application {
    Repository<Long, Utilizator> utilizatorRepository;
    ServiceApp service;

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Reading data from file");
        Repository<Long,Utilizator> repoDBUtilizatori = new UtilizatorDBRepository(new UtilizatorValidator(),"jdbc:postgresql://localhost:5432/network", "postgres","postgres21");
        PrieteniRepository repoDBPrietenie = new PrieteniDBRepository(new PrietenieValidator(), "jdbc:postgresql://localhost:5432/network", "postgres","postgres21");
        Repository<Long, Message> repoDBMesaje = new MessageDBRepository("jdbc:postgresql://localhost:5432/network", "postgres","postgres21", new MessageValidator(), repoDBUtilizatori);
        service = new ServiceApp(repoDBUtilizatori, repoDBPrietenie, repoDBMesaje);
        //repoDBPrietenie.findAll().forEach(System.out::println);
        initView(stage);
        stage.show();
    }

    private void initView(Stage primaryStage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/lab6/login-view.fxml"));

        AnchorPane userLayout = fxmlLoader.load();
        primaryStage.setScene(new Scene(userLayout));

        LoginView userController = fxmlLoader.getController();
        userController.setUtilizatorService(service);

    }

    public static void main(String[] args) {
        launch(args);
    }
}