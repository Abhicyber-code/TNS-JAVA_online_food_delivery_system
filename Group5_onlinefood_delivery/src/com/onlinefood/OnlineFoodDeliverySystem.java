package com.onlinefood;

import java.util.*;

 */
public class OnlineFoodDeliverySystem {

    // ====== ENTITIES ======

    // 1. FoodItem
    static class FoodItem {
        private int id;
        private String name;
        private double price;

        public FoodItem(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }

        public void setId(int id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setPrice(double price) { this.price = price; }

        @Override
        public String toString() {
            return "FoodItem{id=" + id + ", name='" + name + "', price=" + price + "}";
        }

        // Needed because FoodItem is used as a key in Map
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FoodItem foodItem = (FoodItem) o;
            return id == foodItem.id;
        }

        @Override
        public int hashCode() { return Objects.hash(id); }
    }

    // 2. User (Base Class)
    static class User {
        private int userId;
        private String username;
        private long contactNo;

        public User(int userId, String username, long contactNo) {
            this.userId = userId;
            this.username = username;
            this.contactNo = contactNo;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public long getContactNo() { return contactNo; }

        @Override
        public String toString() {
            return "User{" +
                    "userId=" + userId +
                    ", username='" + username + '\'' +
                    ", contactNo=" + contactNo +
                    '}';
        }
    }

    // 3. Cart
    static class Cart {
        private Map<FoodItem, Integer> items = new LinkedHashMap<>();

        public void addItem(FoodItem foodItem, int quantity) {
            if (foodItem == null || quantity <= 0) return;
            items.merge(foodItem, quantity, Integer::sum);
        }

        public void removeItem(FoodItem foodItem) {
            if (foodItem == null) return;
            items.remove(foodItem);
        }

        public Map<FoodItem, Integer> getItems() { return items; }

        public double getTotalCost() {
            return items.entrySet().stream()
                    .mapToDouble(e -> e.getKey().getPrice() * e.getValue())
                    .sum();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (items.isEmpty()) {
                sb.append("Cart is empty\n");
            } else {
                for (Map.Entry<FoodItem, Integer> e : items.entrySet()) {
                    FoodItem fi = e.getKey();
                    int qty = e.getValue();
                    double cost = fi.getPrice() * qty;
                    sb.append("Food Item: ").append(fi.getName())
                      .append(", Quantity: ").append(qty)
                      .append(", Cost: Rs. ").append(cost)
                      .append("\n");
                }
                sb.append("Total Cost: Rs. ").append(getTotalCost()).append("\n");
            }
            return sb.toString();
        }
    }

    // 4. Customer (Inherits from User)
    static class Customer extends User {
        private Cart cart = new Cart();

        public Customer(int userId, String username, long contactNo) {
            super(userId, username, contactNo);
        }

        public Cart getCart() { return cart; }
    }

    // 5. DeliveryPerson
    static class DeliveryPerson {
        private int deliveryPersonId;
        private String name;
        private long contactNo;

        public DeliveryPerson(int deliveryPersonId, String name, long contactNo) {
            this.deliveryPersonId = deliveryPersonId;
            this.name = name;
            this.contactNo = contactNo;
        }

        public int getDeliveryPersonId() { return deliveryPersonId; }
        public String getName() { return name; }
        public long getContactNo() { return contactNo; }

        @Override
        public String toString() {
            return "DeliveryPerson{" +
                    "deliveryPersonId=" + deliveryPersonId +
                    ", name='" + name + '\'' +
                    ", contactNo=" + contactNo +
                    '}';
        }
    }

    // 6. Restaurant
    static class Restaurant {
        private int id;
        private String name;
        private List<FoodItem> menu = new ArrayList<>();

        public Restaurant(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public List<FoodItem> getMenu() { return menu; }

        public void addFoodItem(FoodItem item) {
            // replace if same id exists
            menu.removeIf(fi -> fi.getId() == item.getId());
            menu.add(item);
        }

        public void removeFoodItem(int foodItemId) {
            menu.removeIf(fi -> fi.getId() == foodItemId);
        }

        @Override
        public String toString() {
            return "Restaurant{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", menuSize=" + menu.size() +
                    '}';
        }
    }

    // 7. Order
    static class Order {
        private int orderId;
        private Customer customer;
        private Map<FoodItem, Integer> items = new LinkedHashMap<>();
        private String status = "Pending"; // default - Pending
        private DeliveryPerson deliveryPerson; // can be null
        private String deliveryAddress;

        public Order(int orderId, Customer customer) {
            this.orderId = orderId;
            this.customer = customer;
        }

        public int getOrderId() { return orderId; }
        public Customer getCustomer() { return customer; }
        public Map<FoodItem, Integer> getItems() { return items; }
        public String getStatus() { return status; }
        public DeliveryPerson getDeliveryPerson() { return deliveryPerson; }
        public String getDeliveryAddress() { return deliveryAddress; }

        public void setStatus(String status) { this.status = status; }
        public void setDeliveryPerson(DeliveryPerson deliveryPerson) { this.deliveryPerson = deliveryPerson; }
        public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

        public void addItem(FoodItem item, int qty) {
            if (item == null || qty <= 0) return;
            items.merge(item, qty, Integer::sum);
        }

        @Override
        public String toString() {
            String dp = (deliveryPerson == null) ? "Not Assigned" : deliveryPerson.getName();
            return "Order{" +
                    "orderId=" + orderId +
                    ", customer=" + customer.getUsername() +
                    ", items=" + items +
                    ", status='" + status + '\'' +
                    ", deliveryPerson=" + dp +
                    '}';
        }
    }

    // ====== DATA STORE (in-memory) ======
    static class DataStore {
        Map<Integer, Restaurant> restaurants = new LinkedHashMap<>();
        Map<Integer, Customer> customers = new LinkedHashMap<>();
        Map<Integer, DeliveryPerson> deliveryPeople = new LinkedHashMap<>();
        Map<Integer, Order> orders = new LinkedHashMap<>();
        int nextOrderId = 1;
    }

    // ====== SERVICES ======

    // Food & Restaurant management
    static class FoodService {
        private final DataStore db;
        public FoodService(DataStore db) { this.db = db; }

        public boolean addRestaurant(int id, String name) {
            if (db.restaurants.containsKey(id)) return false;
            db.restaurants.put(id, new Restaurant(id, name));
            return true;
        }

        public boolean addFoodItemToRestaurant(int restaurantId, int foodId, String name, double price) {
            Restaurant r = db.restaurants.get(restaurantId);
            if (r == null) return false;
            r.addFoodItem(new FoodItem(foodId, name, price));
            return true;
        }

        public boolean removeFoodItemFromRestaurant(int restaurantId, int foodId) {
            Restaurant r = db.restaurants.get(restaurantId);
            if (r == null) return false;
            int before = r.getMenu().size();
            r.removeFoodItem(foodId);
            return r.getMenu().size() < before;
        }

        public void printRestaurantsAndMenus() {
            if (db.restaurants.isEmpty()) {
                System.out.println("No restaurants available.");
                return;
            }
            System.out.println("Restaurants and Menus:");
            for (Restaurant r : db.restaurants.values()) {
                System.out.println("Restaurant ID: " + r.getId() + ", Name: " + r.getName());
                for (FoodItem fi : r.getMenu()) {
                    System.out.println("- Food Item ID: " + fi.getId() + ", Name: " + fi.getName() + ", Price: Rs. " + fi.getPrice());
                }
            }
        }

        public FoodItem findFoodItem(int restaurantId, int foodItemId) {
            Restaurant r = db.restaurants.get(restaurantId);
            if (r == null) return null;
            return r.getMenu().stream().filter(fi -> fi.getId() == foodItemId).findFirst().orElse(null);
        }
    }

    // Customer management
    static class CustomerService {
        private final DataStore db;
        public CustomerService(DataStore db) { this.db = db; }

        public boolean addCustomer(int userId, String username, long contactNo) {
            if (db.customers.containsKey(userId)) return false;
            db.customers.put(userId, new Customer(userId, username, contactNo));
            return true;
        }

        public Customer getCustomer(int userId) { return db.customers.get(userId); }

        public void addFoodToCart(int customerId, FoodItem item, int quantity) {
            Customer c = db.customers.get(customerId);
            if (c != null && item != null && quantity > 0) {
                c.getCart().addItem(item, quantity);
            }
        }

        public void viewCart(int customerId) {
            Customer c = db.customers.get(customerId);
            if (c == null) {
                System.out.println("Customer not found!");
                return;
            }
            System.out.println("Cart:\n" + c.getCart().toString());
        }
    }

    // Orders & delivery management
    static class OrderService {
        private final DataStore db;
        public OrderService(DataStore db) { this.db = db; }

        public int placeOrder(int customerId, String deliveryAddress) {
            Customer c = db.customers.get(customerId);
            if (c == null) return -1;
            if (c.getCart().getItems().isEmpty()) return -1;

            int oid = db.nextOrderId++;
            Order o = new Order(oid, c);
            for (Map.Entry<FoodItem, Integer> e : c.getCart().getItems().entrySet()) {
                o.addItem(e.getKey(), e.getValue());
            }
            o.setDeliveryAddress(deliveryAddress);
            db.orders.put(oid, o);
            // clear cart after placing order
            c.getCart().getItems().clear();
            return oid;
        }

        public boolean addDeliveryPerson(int id, String name, long contactNo) {
            if (db.deliveryPeople.containsKey(id)) return false;
            db.deliveryPeople.put(id, new DeliveryPerson(id, name, contactNo));
            return true;
        }

        public boolean assignDeliveryPersonToOrder(int orderId, int deliveryPersonId) {
            Order o = db.orders.get(orderId);
            DeliveryPerson dp = db.deliveryPeople.get(deliveryPersonId);
            if (o == null || dp == null) return false;
            o.setDeliveryPerson(dp);
            return true;
        }

        public void printOrders() {
            if (db.orders.isEmpty()) {
                System.out.println("No orders found.");
                return;
            }
            System.out.println("Orders:");
            for (Order o : db.orders.values()) {
                System.out.println(o);
            }
        }

        public void printOrdersForCustomer(int customerId) {
            boolean any = false;
            for (Order o : db.orders.values()) {
                if (o.getCustomer().getUserId() == customerId) {
                    if (!any) {
                        System.out.println("Orders:");
                        any = true;
                    }
                    System.out.println(o);
                }
            }
            if (!any) System.out.println("No orders for this customer.");
        }
    }

    // ====== CLI (Menus) ======

    private final DataStore db = new DataStore();
    private final FoodService foodService = new FoodService(db);
    private final CustomerService customerService = new CustomerService(db);
    private final OrderService orderService = new OrderService(db);

    private final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        new OnlineFoodDeliverySystem().start();
    }

    private void start() {
        while (true) {
            System.out.println("1. Admin Menu");
            System.out.println("2. Customer Menu");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int choice = readInt();
            switch (choice) {
                case 1 -> adminMenu();
                case 2 -> customerMenu();
                case 3 -> {
                    System.out.println("Exiting application");
                    return;
                }
                default -> System.out.println("Invalid choice!\n");
            }
        }
    }

    // ====== Admin Menu ======
    private void adminMenu() {
        while (true) {
            System.out.println();
            System.out.println("Admin Menu:");
            System.out.println("1. Add Restaurant");
            System.out.println("2. Add Food Item to Restaurant");
            System.out.println("3. Remove Food Item from Restaurant");
            System.out.println("4. View Restaurants and Menus");
            System.out.println("5. View Orders");
            System.out.println("6. Add Delivery Person");
            System.out.println("7. Assign Delivery Person to Order");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");
            int choice = readInt();
            switch (choice) {
                case 1 -> addRestaurantFlow();
                case 2 -> addFoodItemFlow();
                case 3 -> removeFoodItemFlow();
                case 4 -> foodService.printRestaurantsAndMenus();
                case 5 -> orderService.printOrders();
                case 6 -> addDeliveryPersonFlow();
                case 7 -> assignDeliveryPersonFlow();
                case 8 -> {
                    System.out.println("Exiting Admin Module\n");
                    return;
                }
                default -> System.out.println("Invalid choice!\n");
            }
        }
    }

    private void addRestaurantFlow() {
        System.out.print("Enter Restaurant ID: ");
        int id = readInt();
        System.out.print("Enter Restaurant Name: ");
        String name = readLine();
        boolean ok = foodService.addRestaurant(id, name);
        if (ok) System.out.println("Restaurant added successfully!\n");
        else System.out.println("Restaurant ID already exists!\n");
    }

    private void addFoodItemFlow() {
        System.out.print("Enter Restaurant ID: ");
        int rid = readInt();
        System.out.print("Enter Food Item ID: ");
        int fid = readInt();
        System.out.print("Enter Food Item Name: ");
        String fname = readLine();
        System.out.print("Enter Food Item Price: ");
        double price = readDouble();
        boolean ok = foodService.addFoodItemToRestaurant(rid, fid, fname, price);
        if (ok) System.out.println("Food item added successfully!\n");
        else System.out.println("Restaurant not found!\n");
    }

    private void removeFoodItemFlow() {
        System.out.print("Enter Restaurant ID: ");
        int rid = readInt();
        System.out.print("Enter Food Item ID: ");
        int fid = readInt();
        boolean ok = foodService.removeFoodItemFromRestaurant(rid, fid);
        if (ok) System.out.println("Food item removed successfully!\n");
        else System.out.println("Restaurant or Food Item not found!\n");
    }

    private void addDeliveryPersonFlow() {
        System.out.print("Enter Delivery Person ID: ");
        int id = readInt();
        System.out.print("Enter Delivery Person Name: ");
        String name = readLine();
        System.out.print("Enter Contact No.: ");
        long contact = readLong();
        boolean ok = orderService.addDeliveryPerson(id, name, contact);
        if (ok) System.out.println("Delivery person added successfully!\n");
        else System.out.println("Delivery person ID already exists!\n");
    }

    private void assignDeliveryPersonFlow() {
        System.out.print("Enter Order ID: ");
        int oid = readInt();
        System.out.print("Enter Delivery Person ID: ");
        int did = readInt();
        boolean ok = orderService.assignDeliveryPersonToOrder(oid, did);
        if (ok) System.out.println("Delivery person assigned to order successfully!\n");
        else System.out.println("Order or Delivery Person not found!\n");
    }

    // ====== Customer Menu ======
    private void customerMenu() {
        while (true) {
            System.out.println();
            System.out.println("Customer Menu:");
            System.out.println("1. Add Customer");
            System.out.println("2. View Food Items");
            System.out.println("3. Add Food to Cart");
            System.out.println("4. View Cart");
            System.out.println("5. Place Order");
            System.out.println("6. View Orders");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");
            int choice = readInt();
            switch (choice) {
                case 1 -> addCustomerFlow();
                case 2 -> foodService.printRestaurantsAndMenus();
                case 3 -> addFoodToCartFlow();
                case 4 -> viewCartFlow();
                case 5 -> placeOrderFlow();
                case 6 -> viewOrdersFlow();
                case 7 -> {
                    System.out.println("Exiting Customer Module\n");
                    return;
                }
                default -> System.out.println("Invalid choice!\n");
            }
        }
    }

    private void addCustomerFlow() {
        System.out.print("Enter User ID: ");
        int id = readInt();
        System.out.print("Enter Username: ");
        String name = readLine();
        System.out.print("Enter Contact No.: ");
        long contact = readLong();
        boolean ok = customerService.addCustomer(id, name, contact);
        if (ok) System.out.println("Customer created successfully!\n");
        else System.out.println("User ID already exists!\n");
    }

    private void addFoodToCartFlow() {
        System.out.print("Enter Customer ID: ");
        int cid = readInt();
        if (db.customers.get(cid) == null) {
            System.out.println("Customer not found!\n");
            return;
        }
        System.out.print("Enter Restaurant ID: ");
        int rid = readInt();
        System.out.print("Enter Food Item ID: ");
        int fid = readInt();
        System.out.print("Enter Quantity: ");
        int qty = readInt();
        FoodItem item = foodService.findFoodItem(rid, fid);
        if (item == null) {
            System.out.println("Restaurant or Food Item not found!\n");
            return;
        }
        customerService.addFoodToCart(cid, item, qty);
        System.out.println("Food item added to cart!\n");
    }

    private void viewCartFlow() {
        System.out.print("Enter Customer ID: ");
        int cid = readInt();
        customerService.viewCart(cid);
        System.out.println();
    }

    private void placeOrderFlow() {
        System.out.print("Enter Customer ID: ");
        int cid = readInt();
        System.out.print("Enter Delivery Address: ");
        String addr = readLine();
        int oid = orderService.placeOrder(cid, addr);
        if (oid > 0) System.out.println("Order placed successfully! Your order ID is: " + oid + "\n");
        else System.out.println("Unable to place order. Ensure customer exists and cart is not empty.\n");
    }

    private void viewOrdersFlow() {
        System.out.print("Enter Customer ID: ");
        int cid = readInt();
        orderService.printOrdersForCustomer(cid);
        System.out.println();
    }

    // ====== Input helpers ======
    private int readInt() {
        while (true) {
            try {
                String s = sc.nextLine().trim();
                return Integer.parseInt(s);
            } catch (Exception e) {
                System.out.print("Please enter a valid integer: ");
            }
        }
    }

    private long readLong() {
        while (true) {
            try {
                String s = sc.nextLine().trim();
                return Long.parseLong(s);
            } catch (Exception e) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private double readDouble() {
        while (true) {
            try {
                String s = sc.nextLine().trim();
                return Double.parseDouble(s);
            } catch (Exception e) {
                System.out.print("Please enter a valid price: ");
            }
        }
    }

    private String readLine() {
        String s = sc.nextLine();
        if (s == null) return "";
        return s.trim();
    }
}
