package org.example.lab6;

import domain.Utilizator;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import service.ServiceApp;

import java.util.List;

public class ProfileView {
    public Label nr_prieteni;
    public ImageView profileImage;
    Utilizator currentUser;
    ServiceApp service;

    public void setUtilizatorService(ServiceApp service, Utilizator utilizator) {
        this.service = service;
        this.currentUser = utilizator;
        initModel();
    }

    private void initModel() {
        Iterable<Utilizator> l = service.getPrieteniUser(currentUser);
        int lung = 0;
        if (l != null) {
            for (Utilizator u : l) {
                lung++;
            }
        }
        nr_prieteni.setText(String.valueOf(lung));

        String imagePath = currentUser.getImagine();
        try {
            Image image = new Image("file:" + imagePath);
            profileImage.setImage(image);
            profileImage.setFitWidth(100);
            profileImage.setFitHeight(100);
            double radius = Math.min(profileImage.getFitWidth(), profileImage.getFitHeight()) / 2;
            Circle clip = new Circle(profileImage.getFitWidth() / 2, profileImage.getFitHeight() / 2, radius);
            profileImage.setClip(clip);

        } catch (Exception e) {
            System.out.println("Eroare la încărcarea imaginii din URL: " + e.getMessage());
        }
    }

}
