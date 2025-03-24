package org.example.lab6;

import domain.Message;
import domain.Utilizator;
import events.PrietenieEntityChangeEvent;
import javafx.event.ActionEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import observer.Observer;
import service.ServiceApp;

import java.util.List;

public class ChatView implements Observer<PrietenieEntityChangeEvent> {
    public TextField messageTextField;
    public TextArea messagesArea;
    public ListView listViewForMessages;
    private ServiceApp service;
    private Utilizator currentUser;
    private Utilizator user_to_chat;

    public void setUtilizatorService(ServiceApp service, Utilizator utilizator1, Utilizator utilizator2) {
        this.service = service;
        service.addObserver(this);
        this.currentUser = utilizator1;
        this.user_to_chat = utilizator2;
        messageTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleEnterKeyPress();
            }
        });
        initModel();
    }

    private void initModel()
    {
        listViewForMessages.getItems().clear();
        List<Message> messages = service.getMessagesBetweenUsers(currentUser, user_to_chat);

        for (Message message : messages) {
            boolean isCurrentUser = message.getFrom().getId().equals(currentUser.getId());
            HBox messageBubble = createMessageBubble(message.getMessage(), isCurrentUser, message.getId());
            listViewForMessages.getItems().add(messageBubble);
            listViewForMessages.scrollTo(messageBubble);
        }
    }

    private void handleEnterKeyPress() {
        sendMessage(new ActionEvent());
    }

    private HBox createMessageBubble(String message, boolean isCurrentUser, Long messageId) {
        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new javafx.geometry.Insets(10));
        textFlow.setMaxWidth(300);

        // Create HBox to hold the bubble
        HBox bubbleContainer = new HBox(textFlow);
        bubbleContainer.setPadding(new javafx.geometry.Insets(5));

        // Align the bubble (left for received, right for sent)
        if (isCurrentUser) {
            bubbleContainer.setStyle("-fx-alignment: center-right;");
        } else {
            bubbleContainer.setStyle("-fx-alignment: center-left;");
        }
        bubbleContainer.setUserData(messageId);
        return bubbleContainer;
    }

    public void sendMessage(ActionEvent actionEvent) {
        String message = messageTextField.getText();
        HBox selectedBubble = (HBox) listViewForMessages.getSelectionModel().getSelectedItem();
        if (!message.isEmpty() && selectedBubble == null) {
            service.sendMessage(currentUser, user_to_chat, message);
            messageTextField.clear();
        }
        if(! message.isEmpty() && selectedBubble != null) {
            service.sendReplay(currentUser, user_to_chat, message, (Long) selectedBubble.getUserData());
            messageTextField.clear();
        }
    }

    @Override
    public void update(PrietenieEntityChangeEvent e) {
        initModel();
    }

    public void onCleckedShowReplay(MouseEvent mouseEvent) {
        HBox selectedBubble = (HBox) listViewForMessages.getSelectionModel().getSelectedItem();
        Long messageId = (Long) selectedBubble.getUserData();
        Message m = service.findMessageById(messageId);
        Message replay = m.getReply();
        if (m != null && m.getReply() != null) {
            String replyToMessage = m.getReply().getMessage();
            Text replyText = new Text("Replying to: " + replyToMessage);
            replyText.setStyle("-fx-font-style: italic; -fx-fill: pink;");
            TextFlow replyTextFlow = new TextFlow(replyText);
            replyTextFlow.setPadding(new javafx.geometry.Insets(5));

            if (selectedBubble.getChildren().size() == 1) {
                selectedBubble.getChildren().add(0, replyTextFlow);
            }
        }
    }
}
