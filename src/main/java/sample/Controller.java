package sample;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import sample.datamodel.Contact;
import sample.datamodel.ContactData;

import java.io.IOException;
import java.util.Optional;


public class Controller {

    @FXML
    private BorderPane mainPanel;
    @FXML
    private TableView<Contact> contactsTable;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ContextMenu addContextMenu;

    private ContactData data;

    public void initialize() {
        data = new ContactData();
        data.loadContacts();
        contactsTable.setItems(data.getContacts());

        // Implementare Meniu Contextual
        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                deleteContact();
            }
        });
        MenuItem editMenuItem = new MenuItem("Edit");
        editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                showEditContactDialog();
            }
        });

        addContextMenu = new ContextMenu();
        MenuItem addMenuItem = new MenuItem("Add");
        addMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                showAddContactDialog();
            }
        });

        // Adaugare elemente la meniul contextual (edit + delete / add)
        listContextMenu.getItems().addAll(deleteMenuItem, editMenuItem);
        addContextMenu.getItems().addAll(addMenuItem);

        // Setare vizibilitate meniu contextual doar in cazul in care se apasa click dreapta deasupra unui contact existent
        contactsTable.setRowFactory(new Callback<TableView<Contact>, TableRow<Contact>>() {
            @Override
            public TableRow<Contact> call(TableView<Contact> tableView) {
                final TableRow<Contact> row = new TableRow<>();
                row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty())).then(listContextMenu).otherwise(addContextMenu));
                return row;
            }
        });

//**************** ADAUGAT PENTRU A PUTEA EDITA CU DUBLU CLICK*****************************************************
        contactsTable.setEditable(true);

        for (TableColumn column : contactsTable.getColumns()) {
            //column.setStyle("-fx-alignment: CENTER;");
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Contact, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Contact, String> t) {
                    if (column.equals(contactsTable.getColumns().get(0))) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setFirstName(t.getNewValue());
                    } else if (column.equals(contactsTable.getColumns().get(1))) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setLastName(t.getNewValue());
                    } else if (column.equals(contactsTable.getColumns().get(2))) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setPhoneNumber(t.getNewValue());
                    } else if (column.equals(contactsTable.getColumns().get(3))) {
                        t.getTableView().getItems().get(t.getTablePosition().getRow()).setNotes(t.getNewValue());
                    }
                    data.saveContacts();
                }
            });
        }
//*******************************************************************************************************************
    }

    @FXML
    public void showAddContactDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Add New Contact");
        dialog.setHeaderText("Use this dialog to add a new contact.");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxmlFiles/contactDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            ContactDialogController contactDialogController = fxmlLoader.getController();
            // Adaugare contact nou in lista de contacte
            Contact newContact = contactDialogController.getNewContact();
            data.addContact(newContact);
            data.saveContacts();
        }
    }

    @FXML
    public void showEditContactDialog(){
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if (selectedContact == null){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Contact Selected!");
            alert.setHeaderText(null);
            alert.setContentText("Please select the contact you want to edit.");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainPanel.getScene().getWindow());
        dialog.setTitle("Edit Contact");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxmlFiles/contactDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        ContactDialogController contactDialogController = fxmlLoader.getController();
        contactDialogController.editContact(selectedContact);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK){
            contactDialogController.updateContact(selectedContact);
            data.saveContacts();
        }
    }

    @FXML
    public void deleteContact(){
        Contact selectedContact = contactsTable.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Contact Selected!");
            alert.setHeaderText(null);
            alert.setContentText("Please select the contact you want to delete.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Contact");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete the selected contact: " +
                selectedContact.getFirstName() + " " + selectedContact.getLastName() + "?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            data.deleteContact(selectedContact);
            data.saveContacts();
        }
    }

    @FXML
    public void exitApp(){
        Platform.exit();
    }
}
