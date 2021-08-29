package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import model.User;
import utils.DBConnection;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static utils.DBConnection.closeConnection;

/**
 * The Appointments screen controller class.
 *
 * @author Christian Lopez
 * Software II - C195
 */
public class AppointmentsScreenController implements Initializable {
    private static final Connection conn = DBConnection.getConn();
    private final ObservableList<Appointment> appointmentList = FXCollections.observableArrayList();
    private final ObservableList<Contact> contactList = FXCollections.observableArrayList();
    private final ObservableList<LocalTime> startTime = FXCollections.observableArrayList();
    private final ObservableList<LocalTime> endTime = FXCollections.observableArrayList();
    /**
     * The Stage.
     */
    Stage stage;
    /**
     * The Scene.
     */
    Parent scene;
    /**
     * The Parent Scene.
     */
    PreparedStatement ps;
    /**
     * The Contact id.
     */
    int contactID = -1;
    /**
     * The Customer id.
     */
    int customerID = -1;
    /**
     * The Appointment id 1.
     */
    int appointmentID1 = 0;
    /**
     * The Start time.
     */
    Timestamp start = null;
    /**
     * The End time.
     */
    Timestamp end = null;
    private Appointment selectedAppointment;
    @FXML
    private RadioButton allButton;
    @FXML
    private TableView<Appointment> appointmentTable;
    @FXML
    private TableColumn<Appointment, Integer> appointmentColumn;
    @FXML
    private TableColumn<Appointment, String> titleColumn;
    @FXML
    private TableColumn<Appointment, String> descriptionColumn;
    @FXML
    private TableColumn<Appointment, String> locationColumn;
    @FXML
    private TableColumn<Appointment, String> contactColumn;
    @FXML
    private TableColumn<Appointment, String> typeColumn;
    @FXML
    private TableColumn<Appointment, String> startColumn;
    @FXML
    private TableColumn<Appointment, String> endColumn;
    @FXML
    private TableColumn<Appointment, Integer> customerIDColumn;
    @FXML
    private Label appointmentLabel;
    @FXML
    private TextField appointmentID;
    @FXML
    private TextField appointmentTitle;
    @FXML
    private TextField appointmentDescription;
    @FXML
    private ComboBox<String> locationComboBox;
    @FXML
    private ComboBox<String> contactComboBox;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private DatePicker appointmentDatePicker;
    @FXML
    private ComboBox<LocalTime> startComboBox;
    @FXML
    private ComboBox<LocalTime> endComboBox;
    @FXML
    private ComboBox<Integer> customerIDComboBox;
    @FXML
    private TextField customerName;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        appointmentColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));

        populateTable();
        populateLocationComboBox();
        populateContactComboBox();
        populateTypeComboBox();
        populateDatTimeComboBoxes();
        populateCustomerComboBox();
    }

    /**
     * Populate appointments table method.
     */
    @FXML
    public void populateTable() {
        try {
            appointmentList.clear();
            allButton.setSelected(true);

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
                String customerName = rs.getString("Customer_Name");

                LocalDateTime createdDate = rs.getTimestamp("Create_Date").toLocalDateTime();
                String createdBy = rs.getString("Created_By");
                Timestamp lastUpdate = rs.getTimestamp("Last_Update");
                String lastUpdatedBy = rs.getString("Last_Updated_By");
                int contactID = rs.getInt("Contact_ID");
                String email = rs.getString("Email");

                int userID = User.getUserId();

                appointmentList.add(new Appointment(appointmentID, title, description,
                        location, type, start, end, createdDate, createdBy, lastUpdate,
                        lastUpdatedBy, customerID, userID, contactID, contactName, customerName));
            }

            appointmentTable.setItems(appointmentList);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("SQL error!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }
    }

    /**
     * All radio button.
     *
     * @param event the event
     */
    @FXML
    public void allRadio(ActionEvent event) {
        populateTable();
    }

    /**
     * Month radio button.
     *
     * @param event the event
     */
    @FXML
    public void monthRadio(ActionEvent event) {
        filterByCurrentMonth(appointmentList);
    }

    /**
     * Week radio button.
     *
     * @param event the event
     */
    @FXML
    public void weekRadio(ActionEvent event) {
        filterByCurrentWeek(appointmentList);
    }

    /**
     * On action add.
     *
     * @param event the event
     */
    @FXML
    public void onActionAdd(ActionEvent event) {
        appointmentLabel.setText("Add New Appointment");
        onActionClearAllFields(event);
    }

    /**
     * On action update.
     *
     * @param event the event
     */
    @FXML
    public void onActionUpdate(ActionEvent event) {
        if (appointmentTable.getSelectionModel().getSelectedItem() != null) {
            onActionClearAllFields(event);
            appointmentLabel.setText("Update Appointment");
            selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();

            appointmentID.setText(Integer.toString(selectedAppointment.getAppointmentID()));
            appointmentTitle.setText(selectedAppointment.getTitle());
            appointmentDescription.setText(selectedAppointment.getDescription());
            locationComboBox.setValue(selectedAppointment.getLocation());
            contactComboBox.setValue(selectedAppointment.getContactName());
            typeComboBox.setValue(selectedAppointment.getType());

            appointmentDatePicker.setValue(LocalDate.parse(selectedAppointment.getStart().toLocalDate().toString()));
            startComboBox.getSelectionModel().select(selectedAppointment.getStart().toLocalTime());
            endComboBox.getSelectionModel().select(selectedAppointment.getEnd().toLocalTime());
            customerIDComboBox.setValue(selectedAppointment.getCustomerID());
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setContentText("No appointment selected for this action.");
            alert.showAndWait();
        }
    }

    /**
     * On action save.
     *
     * @param event the event
     */
    @FXML
    public void onActionSave(ActionEvent event) {
        try {
            String title = appointmentTitle.getText();
            String description = appointmentDescription.getText();
            String location = locationComboBox.getValue();
            String type = typeComboBox.getValue();

            LocalTime startBox = startComboBox.getValue();
            LocalTime endBox = endComboBox.getValue();

            if (startBox != null && endBox != null) {
                LocalDate date = appointmentDatePicker.getValue();
                LocalDateTime appointmentStart = LocalDateTime.of(date, startBox);
                LocalDateTime appointmentEnd = LocalDateTime.of(date, endBox);
                start = Timestamp.valueOf(appointmentStart);
                end = Timestamp.valueOf(appointmentEnd);
            }
            String lastUpdatedBy = User.getUserName();

            if (customerIDComboBox.getValue() != null) {
                customerID = customerIDComboBox.getValue();
            }

            int userID = User.getUserId();
            String tempContact = contactComboBox.getValue();

            if (tempContact != null) {
                contactID = getContactIDFromList(tempContact);

            }

            if (title.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Please select an appointment title.");
                alert.showAndWait();
                return;
            }

            if (description.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Please select an appointment description.");
                alert.showAndWait();
                return;
            }

            if (location == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Please select an appointment location.");
                alert.showAndWait();
                return;
            }

            if (tempContact == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Please select an appointment contact.");
                alert.showAndWait();
                return;
            }

            if (type == null) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Please select an appointment type.");
                alert.showAndWait();
                return;
            }

            if (startBox == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Please select an appointment start time.");
                alert.showAndWait();
                return;
            }

            if (endBox == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Please select an appointment end time.");
                alert.showAndWait();
                return;
            }

            if (startBox.isAfter(endBox) || startBox.equals(endBox)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("The appointment time cannot not begin before or at the\n" +
                        "same time as the appointment start time.");
                alert.showAndWait();
                return;
            }

            if (checkBusinessHours(startBox) || checkBusinessHours(endBox)) {
                return;
            }

            if (customerIDComboBox.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Dialog");
                alert.setContentText("Please select a customer ID.");
                alert.showAndWait();
                return;
            }

            if (overlappingAppointments(start, end)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Selected appointment time is unavailable");
                alert.setContentText("Please check the appointment list for available times and then "
                        + "try any of the following:\n"
                        + "1. Please select a different appointment start and end time.\n"
                        + "2. Please select a different appointment date.\n");
                alert.showAndWait();
                return;
            }

            if (appointmentLabel.getText().contentEquals("Add New Appointment")) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO appointments"
                                + "(Title, Description, Location, Type, Start, "
                                + "End, Create_Date, Created_By, Last_Update, Last_Updated_By, "
                                + "Customer_ID, User_ID, Contact_ID) "
                                + "VALUES(?, ?, ?, ?, ?, ?, NOW(), ?, NOW(), ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, title);
                ps.setString(2, description);
                ps.setString(3, location);
                ps.setString(4, type);
                ps.setTimestamp(5, start);
                ps.setTimestamp(6, end);
                ps.setString(7, lastUpdatedBy);
                ps.setString(8, lastUpdatedBy);
                ps.setInt(9, customerID);
                ps.setInt(10, userID);
                ps.setInt(11, contactID);

                int result = ps.executeUpdate();
                if (result > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("Appointment has been created successfully!");
                    alert.showAndWait();
                } else {
                    System.out.println("\nNo save? Check DB.\n");
                }

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {

                    int autoKey = rs.getInt(1);
                    System.out.println("Generated Appointment ID: " + autoKey);
                }

                populateTable();
                onActionClearAllFields(event);
            }

            if (appointmentLabel.getText().contentEquals("Update Appointment")) {
                PreparedStatement ps = conn.prepareStatement("UPDATE appointments "
                        + "SET Title = ?, Description = ?, Location = ?, "
                        + "Type = ?, Start = ?, End = ?, Last_Update = NOW(), "
                        + "Last_Updated_By = ?, Customer_ID = ?, User_ID = ?, Contact_ID = ? "
                        + "WHERE Appointment_ID = ?");

                ps.setString(1, title);
                ps.setString(2, description);
                ps.setString(3, location);
                ps.setString(4, type);
                ps.setTimestamp(5, start);
                ps.setTimestamp(6, end);
                ps.setString(7, lastUpdatedBy);
                ps.setInt(8, customerID);
                ps.setInt(9, userID);
                ps.setInt(10, contactID);
                ps.setInt(11, Integer.parseInt(appointmentID.getText()));

                int result = ps.executeUpdate();
                if (result == 1) {
                    System.out.println("\nSave Succesful!\n");
                } else {
                    System.out.println("\nNo save? Check DB.\n");
                }

                populateTable();
                onActionClearAllFields(event);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("SQL error!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }
    }

    /**
     * On action clear all fields.
     *
     * @param event the event
     */
    @FXML
    public void onActionClearAllFields(ActionEvent event) {
        clearAllFields();
    }

    /**
     * Clear all fields.
     */
    public void clearAllFields() {
        appointmentID.clear();
        appointmentID.setPromptText("Generated Automatically");
        appointmentTitle.clear();
        appointmentDescription.clear();
        locationComboBox.setValue(null);
        contactComboBox.setValue(null);
        typeComboBox.setValue(null);
        appointmentDatePicker.setValue(LocalDate.now());
        startComboBox.setValue(null);
        endComboBox.setValue(null);
    }

    /**
     * On action delete.
     *
     * @param event the event
     */
    @FXML
    public void onActionDelete(ActionEvent event) {
        selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();

        if (selectedAppointment != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("Cancel Appointment " + selectedAppointment.getAppointmentID() + "?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                try {
                    PreparedStatement ps = conn.prepareStatement(
                            "DELETE appointments.* FROM appointments "
                                    + "WHERE appointments.Appointment_ID = ?");
                    ps.setInt(1, selectedAppointment.getAppointmentID());

                    int rs = ps.executeUpdate();
                    if (rs > 0) {
                        Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                        alert2.setHeaderText("Cancelled appointment #" + selectedAppointment.getAppointmentID());
                    } else {
                        System.out.println("\nOne item failed to delete.\n");
                    }
                    populateTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out.println("SQL error!");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                    System.out.println("Non-SQL error!");
                }
            } else {
                return;
            }
        }
        if (selectedAppointment == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setContentText("Please select an appointment to delete.");
            alert.showAndWait();
        }
    }

    /**
     * On action main return.
     *
     * @param event the event
     * @throws IOException the io exception
     */
    @FXML
    public void onActionMainReturn(ActionEvent event) throws IOException {
        stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        scene = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/MainMenuScreen.fxml")));
        stage.setScene(new Scene(scene));
        stage.centerOnScreen();
        stage.show();
    }

    private void populateLocationComboBox() {
        ObservableList<String> locationCombo = FXCollections.observableArrayList();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT first_level_divisions.Division "
                            + "FROM first_level_divisions");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                locationCombo.add(rs.getString("Division"));
            }
            locationComboBox.setItems(locationCombo);

            locationComboBox.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select Location");
                    } else {
                        setText(item);
                    }
                }
            });
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("SQL error!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }
    }

    /**
     * Populate contact combo box.
     */
    @FXML
    public void populateContactComboBox() {
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

                contactList.add(new Contact(contactID, contactName, email));
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
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }
    }

    /**
     * Populate type combo box.
     */
    @FXML
    public void populateTypeComboBox() {
        ObservableList<String> typeCombo = FXCollections.observableArrayList();

        typeCombo.addAll("Planning Session", "De-Briefing", "New Customer",
                "Progress Review", "Close Account");
        typeComboBox.setItems(typeCombo);
        typeComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Type");
                } else {
                    setText(item);
                }
            }
        });

    }

    /**
     * Populate dat time combo boxes.
     */
    @FXML
    public void populateDatTimeComboBoxes() {
        LocalTime times = LocalTime.of(0, 0);

        while (!times.equals(LocalTime.of(23, 30))) {
            startTime.add(times);
            endTime.add(times);
            times = times.plusMinutes(30);
        }

        appointmentDatePicker.setValue(LocalDate.now());
        startComboBox.setItems(startTime);
        endComboBox.setItems(endTime);
    }

    /**
     * Populate customer combo box.
     */
    @FXML
    public void populateCustomerComboBox() {
        ObservableList<Integer> customerIDCombo = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT customers.Customer_ID "
                    + "FROM customers ORDER BY Customer_ID");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                customerIDCombo.add(rs.getInt("Customer_ID"));
            }

            customerIDComboBox.setItems(customerIDCombo);

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
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }
    }

    /**
     * Populate customer name.
     *
     * @param event the event
     */
    @FXML
    public void populateCustomerName(ActionEvent event) {
        try {
            int searchID = customerIDComboBox.getValue();

            PreparedStatement ps = conn.prepareStatement("SELECT * FROM customers "
                    + "WHERE Customer_ID = ?");

            ps.setInt(1, searchID);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                customerName.setText(rs.getString("Customer_Name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("SQL error!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }
    }

    private int getContactIDFromList(String temp) {
        for (Contact look : contactList) {
            if (look.getContactName().trim().toLowerCase().contains(temp.trim().toLowerCase())) {
                return look.getContactID();
            }
        }
        return -1;
    }

    private boolean checkBusinessHours(LocalTime startTime) {
        LocalTime officeClosed = LocalTime.of(22, 00);
        LocalTime officeOpen = LocalTime.of(8, 00);
        LocalDate date = appointmentDatePicker.getValue();

        ZoneId zoneEST = ZoneId.of("US/Eastern");

        LocalDateTime combined = LocalDateTime.of(date, startTime);
        ZonedDateTime convert = combined.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(zoneEST);
        LocalTime eastern = convert.toLocalTime();

        if (eastern.isBefore(officeOpen) || eastern.isAfter(officeClosed)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Appointment must be within business hours");
            alert.setContentText("Selected Time: " + startTime + "\nEastern Time: "
                    + eastern + "\nBusiness hours: 08:00 to 22:00 US/Eastern");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    private boolean overlappingAppointments(Timestamp startA, Timestamp endB) {
        try {
            if (appointmentID.getText().isBlank()) {
                appointmentID1 = 0;
            } else {
                appointmentID1 = Integer.parseInt(appointmentID.getText().trim());
            }

            int customerID1 = customerIDComboBox.getValue();

            ps = conn.prepareStatement(
                    "SELECT * FROM appointments "
                            + "WHERE (? BETWEEN Start AND End OR ? BETWEEN Start AND End OR ? < Start AND ? > End) "
                            + "AND (Customer_ID = ? AND Appointment_ID != ?)");

            ps.setTimestamp(1, startA);
            ps.setTimestamp(2, endB);
            ps.setTimestamp(3, startA);
            ps.setTimestamp(4, endB);
            ps.setInt(5, customerID1);
            ps.setInt(6, appointmentID1);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("SQL error!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.out.println("Non-SQL error!");
        }
        return false;
    }

    /**
     * Filter appointments by the current week.
     * <p>
     * Lambda usage: A lambda expression is used here because the original method uses a prepared SQL statement which
     * was inefficient. We use a filtered list here which results in an "always true" predicate which is more efficient
     * in it's use in this class and uses less lines of code making this method easily readable. This method is
     * primarily used in the weekRadio() method. This method filters the given list based on the contents of the
     * appointments table and by the current day and end of the week.
     * </p>
     * @param aList the appointments list
     */
    private void filterByCurrentWeek(ObservableList aList) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime week = LocalDateTime.now().plusDays(7);

        FilteredList<Appointment> resultCurrentWeek = new FilteredList<>(aList);
        resultCurrentWeek.setPredicate(a ->
        {
            LocalDateTime date = a.getStart();
            return (date.isEqual(now) || date.isAfter(now)) && (date.isBefore(week) || date.isEqual(week));
        });
        appointmentTable.setItems(resultCurrentWeek);
    }

    /**
     * Filter appointments by the current month.
     * <p>
     * Lambda usage: Just like the filterByCurrentWeek() method, a lambda expression is used here because the original
     * method uses a prepared SQL statement which was inefficient. We use a filtered list here which results in an
     * "always true" predicate which is more efficient in it's use in this class and uses less lines of code making this
     * method easily readable. This method is primarily used in the monthRadio() method. This method filters the given
     * list based on the contents of the appointments table and by the current day and end of the month.
     * </p>
     * @param aList the appointments list
     */
    private void filterByCurrentMonth(ObservableList aList) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());

        FilteredList<Appointment> resultEndOfMonth = new FilteredList<>(aList);
        resultEndOfMonth.setPredicate(a ->
        {
            LocalDateTime date = a.getStart();
            return (date.isEqual(now) || date.isAfter(now)) && (date.isBefore(endOfMonth) || date.isEqual(endOfMonth));
        });
        appointmentTable.setItems(resultEndOfMonth);
    }

    @FXML
    private void onActionExit(ActionEvent e) {
        closeConnection();
        System.exit(0);
    }
}
