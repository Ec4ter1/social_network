package org.example.lab6.controller;

import domain.Prietenie;
import domain.Utilizator;
import events.PrietenieEntityChangeEvent;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import observer.Observer;
import service.ServiceApp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FriendRequestView implements Observer<PrietenieEntityChangeEvent> {

    public TableColumn<Prietenie,String> tableColumnUsername;
    public TableColumn<Prietenie,String> tableColumnStatus;
    public TableColumn<Prietenie,String> tableColumnDate;
    public TableView<Prietenie> tableview;
    private ServiceApp service;
    private Utilizator currentUser;
    ObservableList<Prietenie> model = FXCollections.observableArrayList();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    @FXML
    public void initialize() {
        tableColumnUsername.setCellValueFactory(cellData -> {
            Prietenie prietenie = (Prietenie) cellData.getValue();
            Long friendId = prietenie.getNodPrietenie1().equals(currentUser.getId())
                    ? prietenie.getNodPrietenie2()
                    : prietenie.getNodPrietenie1();
            Utilizator friend = service.findUtilizatoriById(friendId);
            return new SimpleStringProperty(friend.getFirstName() + " " + friend.getLastName());
        });

        tableColumnStatus.setCellValueFactory(cellData -> {
            Prietenie prietenie = (Prietenie) cellData.getValue();
            return new SimpleStringProperty(prietenie.getStatus());
        });

        tableColumnDate.setCellValueFactory(cellData -> {
            Prietenie prietenie = (Prietenie) cellData.getValue();
            return new SimpleStringProperty(prietenie.getFriendsFrom().format(formatter));
        });

        tableview.setItems(model);
    }



    public void setUtilizatorService(ServiceApp service, Utilizator utilizator) {
        this.service = service;
        service.addObserver(this);
        this.currentUser = utilizator;
        initModel();
    }

    private void initModel() {
        List<Prietenie> friendships = StreamSupport.stream(service.getFriendsRequest(currentUser).spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(friendships);
    }


    @Override
    public void update(PrietenieEntityChangeEvent e) {
        initModel();
    }
}
