package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointment;
import model.Contact;
import model.ReportType;
import model.User;
import utils.DBConnection;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.ResourceBundle;

import static utils.DBConnection.closeConnection;

/**
 * The Reports screen controller class.
 *
 * @author Christian Lopez
 * Software II - C195
 */
public class ReportsScreenController implements Initializable {
    private static final Connection conn = DBConnection.getConn();
    /**
     * The Stage.
     */
    Stage stage;
    /**
     * The Scene.
     */
    Parent scene;
    /**
     * The Report list.
     */
    ObservableList<ReportType> reportList = FXCollections.observableArrayList();
    /**
     * The Contact list report.
     */
    ObservableList<Contact> contactListReport = FXCollections.observableArrayList();
    /**
     * The Appointment list report.
     */
    ObservableList<Appointment> AppointmentListReport = FXCollections.observableArrayList();

    @FXML
    private TableView<ReportType> typeDayTable;
    @FXML
    private TableView<ReportType> typeMonthTable;
    @FXML
    private TableColumn<ReportType, String> reportTypeColumn;
    @FXML
    private TableColumn<ReportType, String> monthColumn;
    @FXML
    private TableColumn<ReportType, Integer> totalColumn;
    @FXML
    private ComboBox<String> contactComboBox;
    @FXML
    private TableView<Appointment> ContactTable;
    @FXML
    private TableColumn<Appointment, Integer> appointmentColumn;
    @FXML
    private TableColumn<Appointment, String> titleColumn;
    @FXML
    private TableColumn<Appointment, String> typeColumn;
    @FXML
    private TableColumn<Appointment, String> descriptionColumn;
    @FXML
    private TableColumn<Appointment, LocalDateTime> startColumn;
    @FXML
    private TableColumn<Appointment, LocalDateTime> endColumn;
    @FXML
    private TableColumn<Appointment, Integer> customerIdColumn;
    @FXML
    private TableColumn<ReportType, String> dayColumn;
    @FXML
    private TableColumn<ReportType, String> customReportTotalColumn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dayColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
        monthColumn.setCellValueFactory(new PropertyValueFactory<>("month"));
        reportTypeColumn.setCellValueFactory(new PropertyValueFactory<>("Type"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        customReportTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));


        appointmentColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));

        populateTypeMonthTab();
        populateContactsComboBox();
        populateContactsTab();
        populateCustomReport();
    }

    private void populateTypeMonthTab() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT MONTHNAME(Start) AS Month, Type, "
                            + "COUNT(Type) AS TOTAL "
                            + "FROM appointments "
                            + "GROUP BY Month, Type");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String month = rs.getString("Month");
                String type = rs.getString("Type");
                int total = rs.getInt("TOTAL");

                reportList.add(new ReportType(month, type, total));

            }
            typeMonthTable.setItems(reportList);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("SQL Error!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Non-Sql error!");
        }
    }

    private void populateContactsTab() {
        try {
            AppointmentListReport.clear();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM appointments, customers, users, contacts "
                            + "WHERE appointments.User_ID = users.User_ID "
                            + "AND appointments.Contact_ID = contacts.Contact_ID "
                            + "AND appointments.Customer_ID = customers.Customer_ID "
                            + "ORDER BY Start");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int appointmentID = rs.getInt("Appointment_ID");
                String title = rs.getString("Title");
                String description = rs.getString("Description");
                String location = rs.getString("Location");
                String type = rs.getString("Type");
                LocalDateTime start = rs.getTimestamp("Start").toLocalDateTime();
                LocalDateTime end = rs.getTimestamp("End").toLocalDateTime();
                int customerID = rs.getInt("Customer_ID");
                String contactName = rs.getString("Contact_Name");

                LocalDateTime createdDate = rs.getTimestamp("Create_Date").toLocalDateTime();
                String createdBy = rs.getString("Created_By");
                Timestamp lastUpdate = rs.getTimestamp("Last_Update");
                String lastUpdatedBy = rs.getString("Last_Updated_By");
                int contactID = rs.getInt("Contact_ID");
                int userID = User.getUserId();

                AppointmentListReport.add(new Appointment(appointmentID, title, description,
                        location, type, start, end, createdDate, createdBy, lastUpdate,
                        lastUpdatedBy, customerID, userID, contactID, contactName));
            }
            ContactTable.setItems(AppointmentListReport);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("SQL error!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }

    }

    private void populateContactsComboBox() {
        ObservableList<String> contactCombo = FXCollections.observableArrayList();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * "
                    + "FROM contacts");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int contactID = rs.getInt("Contact_ID");
                String contactName = rs.getString("Contact_Name");
                String email = rs.getString("Email");

                contactCombo.add(rs.getString("Contact_Name"));

                contactListReport.add(new Contact(contactID, contactName, email));
            }
            contactComboBox.setItems(contactCombo);
            contactComboBox.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select Contact");
                    } else {
                        setText(item);
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("SQL error!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }

    }

    /**
     * On action contact.
     *
     * @param event the event
     */
    @FXML
    void onActionContact(ActionEvent event) {
        try {
            AppointmentListReport.clear();
            String selectedContact = contactComboBox.getValue();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM appointments, customers, users, contacts "
                            + "WHERE appointments.User_ID = users.User_ID "
                            + "AND appointments.Contact_ID = contacts.Contact_ID "
                            + "AND appointments.Customer_ID = customers.Customer_ID AND "
                            + "Contact_Name = ? "
                            + "ORDER BY Start");

            ps.setString(1, selectedContact);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int appointmentID = rs.getInt("Appointment_ID");
                String title = rs.getString("Title");
                String description = rs.getString("Description");
                String location = rs.getString("Location");
                String type = rs.getString("Type");
                LocalDateTime start = rs.getTimestamp("Start").toLocalDateTime();
                LocalDateTime end = rs.getTimestamp("End").toLocalDateTime();
                int customerID = rs.getInt("Customer_ID");
                String contactName = rs.getString("Contact_Name");
                String customerName = rs.getString("Customer_Name");

                LocalDateTime createdDate = rs.getTimestamp("Create_Date").toLocalDateTime();
                String createdBy = rs.getString("Created_By");
                Timestamp lastUpdate = rs.getTimestamp("Last_Update");
                String lastUpdatedBy = rs.getString("Last_Updated_By");
                int contactID = rs.getInt("Contact_ID");

                int userID = User.getUserId();

                AppointmentListReport.add(new Appointment(appointmentID, title, description,
                        location, type, start, end, createdDate, createdBy, lastUpdate,
                        lastUpdatedBy, customerID, userID, contactID, contactName, customerName));
            }

            ContactTable.setItems(AppointmentListReport);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("SQL error!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }
    }

    private void populateCustomReport() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT DAYNAME(Start) AS Days, COUNT(*) AS Total "
                            + "FROM appointments "
                            + "GROUP BY Days ");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String day = rs.getString("Days");
                int total = rs.getInt("Total");

                reportList.add(new ReportType(day, total));

            }
            typeDayTable.setItems(reportList);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("SQL Error!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Non-Sql error!");
        }
    }

    /**
     * On action main return.
     *
     * @param event the event
     * @throws IOException the io exception
     */
    @FXML
    void onActionMainReturn(ActionEvent event) throws IOException {
        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/MainMenuScreen.fxml")));
        stage.setScene(new Scene(scene));
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * On action exit.
     *
     * @param event the event
     */
    @FXML
    void onActionExit(ActionEvent event) {
        closeConnection();
        System.exit(0);
    }
}
