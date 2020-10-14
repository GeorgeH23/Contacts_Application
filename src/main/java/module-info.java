module Contacts_Application {

    requires javafx.media;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.web;

    opens sample;
    opens sample.datamodel;

    exports sample;
    exports sample.datamodel;
}