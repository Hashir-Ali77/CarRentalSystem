import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class App extends Application {

    private final ObservableList<Car> cars = FXCollections.observableArrayList();
    private final TableView<Car> tableView = new TableView<>();
    private final Label totalLabel = new Label("Total Cars: 0");
    private final Label rentedLabel = new Label("Rented: 0");
    private final Label availableLabel = new Label("Available: 0");

    @Override
    public void start(Stage stage) {
        tableView.setItems(cars);

        TableColumn<Car, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Car, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Car, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customer"));

        TableColumn<Car, String> rentDateCol = new TableColumn<>("Rent Date");
        rentDateCol.setCellValueFactory(new PropertyValueFactory<>("rentalDate"));

        TableColumn<Car, String> returnDateCol = new TableColumn<>("Return Date");
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));

        TableColumn<Car, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().isRented() ? "Rented" : "Available"));

        tableView.getColumns().addAll(idCol, modelCol, customerCol, rentDateCol, returnDateCol, statusCol);

        tableView.setRowFactory(tv -> new TableRow<Car>() {
            @Override
            protected void updateItem(Car car, boolean empty) {
                super.updateItem(car, empty);
                if (car == null || empty) {
                    setStyle("");
                } else if (car.isRented()) {
                    setStyle("-fx-background-color: #ffe5e5;");
                } else {
                    setStyle("");
                }
            }
        });

        TextField idField = new TextField();
        idField.setPromptText("Car ID");

        TextField modelField = new TextField();
        modelField.setPromptText("Car Model");

        TextField customerField = new TextField();
        customerField.setPromptText("Customer Name");

        DatePicker rentDate = new DatePicker();
        rentDate.setPromptText("Rent Date");

        DatePicker returnDate = new DatePicker();
        returnDate.setPromptText("Return Date");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by ID or Model");

        Button addBtn = new Button("➕ Add Car");
        addBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            String model = modelField.getText().trim();
            if (id.isEmpty() || model.isEmpty()) {
                showAlert(AlertType.WARNING, "Missing Input", "Please enter both Car ID and Model.");
                return;
            }
            for (Car car : cars) {
                if (car.getId().equalsIgnoreCase(id)) {
                    showAlert(AlertType.ERROR, "Duplicate ID", "A car with this ID already exists.");
                    return;
                }
            }
            cars.add(new Car(id, model));
            idField.clear();
            modelField.clear();
            updateStats();
        });

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setOnAction(e -> {
            Car selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cars.remove(selected);
                updateStats();
            } else {
                showAlert(AlertType.ERROR, "No Selection", "Please select a car to delete.");
            }
        });

        Button rentBtn = new Button("🚗 Rent");
        rentBtn.setOnAction(e -> {
            Car selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isRented() &&
                    rentDate.getValue() != null && returnDate.getValue() != null && !customerField.getText().isEmpty()) {
                selected.rent(customerField.getText(), rentDate.getValue().toString(), returnDate.getValue().toString());
                tableView.refresh();
                updateStats();
            } else {
                showAlert(AlertType.ERROR, "Invalid Action", "Make sure the car is available, dates are selected, and customer name is entered.");
            }
        });

        Button returnBtn = new Button("🔄 Return");
        returnBtn.setOnAction(e -> {
            Car selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.isRented()) {
                selected.returnCar();
                tableView.refresh();
                updateStats();
            } else {
                showAlert(AlertType.INFORMATION, "Invalid Action", "Car is not rented or not selected.");
            }
        });

        Button editBtn = new Button("✏️ Edit Car");
        editBtn.setOnAction(e -> {
            Car selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TextInputDialog dialog = new TextInputDialog(selected.getModel());
                dialog.setTitle("Edit Car Model");
                dialog.setHeaderText("Editing Car: " + selected.getId());
                dialog.setContentText("New Model:");

                dialog.showAndWait().ifPresent(newModel -> {
                    selected.setModel(newModel);
                    tableView.refresh();
                });
            } else {
                showAlert(AlertType.WARNING, "No Selection", "Please select a car to edit.");
            }
        });

        Button historyBtn = new Button("📜 View History");
        historyBtn.setOnAction(e -> {
            Car selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                List<String> logs = selected.getRentalHistory();
                if (logs.isEmpty()) {
                    showAlert(AlertType.INFORMATION, "Rental History", "No history found for this car.");
                } else {
                    showAlert(AlertType.INFORMATION, "Rental History", String.join("\n", logs));
                }
            } else {
                showAlert(AlertType.WARNING, "No Selection", "Select a car to view history.");
            }
        });

        CheckBox darkModeToggle = new CheckBox("Dark Mode");

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Rented", "Available");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> {
            String selected = statusFilter.getValue();
            ObservableList<Car> filtered = FXCollections.observableArrayList();
            switch (selected) {
                case "Rented":
                    for (Car c : cars) if (c.isRented()) filtered.add(c);
                    break;
                case "Available":
                    for (Car c : cars) if (!c.isRented()) filtered.add(c);
                    break;
                default:
                    tableView.setItems(cars);
                    return;
            }
            tableView.setItems(filtered);
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                filterList(newVal);
            } else {
                tableView.setItems(cars);
            }
        });

        Button clearSearchBtn = new Button("❌ Clear");
        clearSearchBtn.setOnAction(e -> searchField.clear());

        Button resetFormBtn = new Button("🧹 Reset Form");
        resetFormBtn.setOnAction(e -> {
            idField.clear();
            modelField.clear();
            customerField.clear();
            rentDate.setValue(null);
            returnDate.setValue(null);
        });

        Button exportBtn = new Button("📄 Export CSV");
        exportBtn.setOnAction(e -> exportToCSV());

        HBox addDeleteBox = new HBox(10, idField, modelField, addBtn, deleteBtn, editBtn, historyBtn);
        HBox rentReturnBox = new HBox(10, customerField, rentDate, returnDate, rentBtn, returnBtn);
        HBox searchDarkBox = new HBox(10, searchField, clearSearchBtn, statusFilter, darkModeToggle);
        HBox bottomBar = new HBox(20, totalLabel, rentedLabel, availableLabel, resetFormBtn, exportBtn);

        addDeleteBox.setAlignment(Pos.CENTER);
        rentReturnBox.setAlignment(Pos.CENTER);
        searchDarkBox.setAlignment(Pos.CENTER);
        bottomBar.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(10, addDeleteBox, rentReturnBox, searchDarkBox, bottomBar);
        form.setPadding(new Insets(10));

        VBox layout = new VBox(10, tableView, form);
        layout.setPadding(new Insets(10));
        layout.getStyleClass().add("root");

        Scene scene = new Scene(layout, 1000, 600);

        try {
            String css = getClass().getResource("carrental.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("Warning: Stylesheet not found. Continuing without custom CSS.");
        }

        darkModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!layout.getStyleClass().contains("dark")) {
                    layout.getStyleClass().add("dark");
                }
            } else {
                layout.getStyleClass().remove("dark");
            }
        });

        stage.setTitle("🚘 Car Rental System");
        stage.setScene(scene);
        stage.show();
    }

    private void filterList(String keyword) {
        ObservableList<Car> filtered = FXCollections.observableArrayList();
        for (Car car : cars) {
            if (car.getId().toLowerCase().contains(keyword.toLowerCase()) ||
                car.getModel().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(car);
            }
        }
        tableView.setItems(filtered);
    }

    private void updateStats() {
        int total = cars.size();
        int rented = (int) cars.stream().filter(Car::isRented).count();
        int available = total - rented;

        totalLabel.setText("Total Cars: " + total);
        rentedLabel.setText("Rented: " + rented);
        availableLabel.setText("Available: " + available);
    }

    private void exportToCSV() {
        try (FileWriter writer = new FileWriter("car_rental_data.csv")) {
            writer.write("ID,Model,Customer,Rent Date,Return Date,Status\n");
            for (Car car : cars) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        car.getId(), car.getModel(), car.getCustomer(),
                        car.getRentalDate(), car.getReturnDate(),
                        car.isRented() ? "Rented" : "Available"));
            }
            showAlert(AlertType.INFORMATION, "Export Success", "Data exported to car_rental_data.csv");
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Export Failed", "Could not export data to CSV.");
        }
    }

    private void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
