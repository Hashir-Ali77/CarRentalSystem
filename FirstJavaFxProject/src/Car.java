import java.util.ArrayList;
import java.util.List;

public class Car {

    private String id;
    private String model;
    private String customer;
    private String rentalDate;
    private String returnDate;
    private boolean rented;

    private final List<String> rentalHistory = new ArrayList<>();

    // Constructor for a new car (initially available)
    public Car(String id, String model) {
        this.id = id;
        this.model = model;
        this.customer = "";
        this.rentalDate = "";
        this.returnDate = "";
        this.rented = false;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(String rentalDate) {
        this.rentalDate = rentalDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isRented() {
        return rented;
    }

    public void setRented(boolean rented) {
        this.rented = rented;
    }

    // Method to rent the car (with history logging)
    public void rent(String customer, String rentalDate, String returnDate) {
        this.customer = customer;
        this.rentalDate = rentalDate;
        this.returnDate = returnDate;
        this.rented = true;
        rentalHistory.add("Rented by " + customer + " from " + rentalDate + " to " + returnDate);
    }

    // Method to return the car
    public void returnCar() {
        this.customer = "";
        this.rentalDate = "";
        this.returnDate = "";
        this.rented = false;
    }

    // Method to get rental history
    public List<String> getRentalHistory() {
        return rentalHistory;
    }

    // Method to display car information
    @Override
    public String toString() {
        return "Car ID: " + id +
               ", Model: " + model +
               ", Rented: " + (rented ? "Yes" : "No") +
               (rented ? ", Customer: " + customer +
               ", Rental Date: " + rentalDate +
               ", Return Date: " + returnDate : "");
    }

    // Method to check if the car ID matches the search
    public boolean matchesId(String searchId) {
        return this.id.equalsIgnoreCase(searchId);
    }

    // Method to check if the model matches the search
    public boolean matchesModel(String searchModel) {
        return this.model.equalsIgnoreCase(searchModel);
    }
}
