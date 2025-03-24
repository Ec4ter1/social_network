package org.example.lab6;

import domain.FilterUserDTO;
import domain.Prietenie;
import domain.Utilizator;
import events.ChangeEventType;
import events.PrietenieEntityChangeEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import observer.Observer;
import org.example.lab6.controller.FriendRequestView;
import paginare.Page;
import paginare.Pageable;
import service.ServiceApp;
import org.example.lab6.controller.MessageAlert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;



public class MainView implements Observer<PrietenieEntityChangeEvent> {

    public Button previousButton;
    public Button nextButton;
    private int pageSize = 2;
    private int currentPage = 0;
    private int totalNumberOfElements = 0;
    public TextField userField;
    public Label labelPage;
    ServiceApp service;
    ObservableList<Utilizator> model = FXCollections.observableArrayList();

    @FXML
    TableView<Utilizator> tableView;

    FilterUserDTO filter = new FilterUserDTO();

    @FXML
    TableColumn<Utilizator,String> tableColumnFirstName;
    @FXML
    TableColumn<Utilizator,String> tableColumnLastName;

    Utilizator currentUser;

    public void setUtilizatorService(ServiceApp service, Utilizator utilizator) {
        this.service = service;
        this.service.addObserver(this);
        this.currentUser = utilizator;
        initModel();
        //checkNewFriendRequest();
    }

    private void checkNewFriendRequest() {
        String notification = service.getLastFriendRequest(currentUser);
        System.out.println(notification);
        if (notification != null) {
            showPopUp(notification);
        }
    }

    private void showPopUp(String notification) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nouă cerere de prietenie");
        alert.setHeaderText(null);
        alert.setContentText(notification);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        tableView.setItems(model);

    }

    private void initModel() {
        filter.setIdUser(Optional.ofNullable(currentUser.getId()));
        Page<Utilizator> page = service.findAllOnPage(new Pageable(currentPage, pageSize), filter);
        System.out.println(totalNumberOfElements);
        int maxPage = (int) Math.ceil((double) page.getTotalNumberOfElements() / pageSize) - 1;
        if (maxPage == -1) {
            maxPage = 0;
        }
        if (currentPage > maxPage) {
            currentPage = maxPage;
            page = service.findAllOnPage(new Pageable(currentPage, pageSize), filter);
        }
        totalNumberOfElements = page.getTotalNumberOfElements();
        previousButton.setDisable(currentPage == 0);
        nextButton.setDisable((currentPage + 1) * pageSize >= totalNumberOfElements);
        List<Utilizator> lista = StreamSupport.stream(page.getElementsOnPage().spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(lista);
        labelPage.setText("Page " + (currentPage + 1) + " of " + (maxPage + 1));

    }

    public void adaugaPrietenie(ActionEvent actionEvent) {
        String username = userField.getText();
        Utilizator u = service.findUtilizatoriByUserName(username);
        if(u == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.show();
        }
        else {
            try {
                service.adaugaPrietenie(u.getId(), currentUser.getId());
                initModel();
            }
            catch (Exception e) {

            }
        }
    }

    public void stergePrietenie(ActionEvent actionEvent) {
        Utilizator user=(Utilizator) tableView.getSelectionModel().getSelectedItem();
        if (user!=null) {
            service.removePrietenie(user.getId(), currentUser.getId());
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION,"Delete user","Userul a fost sters");
            initModel();
        }
        else MessageAlert.showErrorMessage(null, "NU ati selectat nici un utilizator");
    }

    @Override
    public void update(PrietenieEntityChangeEvent e) {
        initModel();
        if(e.getType()== ChangeEventType.ADD){
            checkNewFriendRequest();
        }
    }


    public void showFriendRequests(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/org/example/lab6/friend-request-view.fxml"));
            AnchorPane userLayout = loader.load();
            Scene scene = new Scene(userLayout);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(currentUser.getFirstName() + " " + currentUser.getLastName());
            FriendRequestView userController = loader.getController();
            userController.setUtilizatorService(service, currentUser);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void chatButton(ActionEvent actionEvent) {
        Utilizator user=(Utilizator) tableView.getSelectionModel().getSelectedItem();
        if (user!=null) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/org/example/lab6/chat-view.fxml"));
                AnchorPane userLayout = loader.load();
                Scene scene = new Scene(userLayout);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setWidth(350);
                stage.setTitle(user.getFirstName() + " " + user.getLastName());
                ChatView userController = loader.getController();
                userController.setUtilizatorService(service, currentUser, user);
                stage.show();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else MessageAlert.showErrorMessage(null, "NU ati selectat nici un utilizator");

    }

    public void onPrevious(ActionEvent actionEvent) {
        currentPage --;
        initModel();
    }

    public void onNext(ActionEvent actionEvent) {
        currentPage ++;
        initModel();
    }

    public void openProfile(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/org/example/lab6/profile-view.fxml"));
            AnchorPane userLayout = loader.load();
            Scene scene = new Scene(userLayout);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setWidth(350);
            stage.setTitle(currentUser.getFirstName() + " " + currentUser.getLastName());
            ProfileView userController = loader.getController();
            userController.setUtilizatorService(service, currentUser);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

