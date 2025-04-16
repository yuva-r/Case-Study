package dao;

import java.sql.*;
import java.util.*;
import entity.Customer;
import entity.Product;
import entity.OrderItem;
import util.DBConnUtil;
import exception.CustomerNotFoundException;
import exception.ProductNotFoundException;
import exception.OrderNotFoundException;

public class OrderProcessorRepositoryImpl implements OrderProcessorRepository {
    
    // Database connection
    private Connection connection;

    // Constructor to initialize DB connection
    public OrderProcessorRepositoryImpl() {
        this.connection = DBConnUtil.getConnection();
    }
    
        

    @Override
    public boolean createProduct(Product product) {
        String query = "INSERT INTO products (name, price, description, stock_quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, product.getName());
            stmt.setDouble(2, product.getPrice());
            stmt.setString(3, product.getDescription());
            stmt.setInt(4, product.getStockQuantity());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean createCustomer(Customer customer) {
        String query = "INSERT INTO customers (name, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPassword());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteProduct(int productId) throws ProductNotFoundException {
        if (!productExists(productId)) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }
        // Continue with the product deletion logic
        String query = "DELETE FROM products WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean deleteCustomer(int customerId) {
        String query = "DELETE FROM customers WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new CustomerNotFoundException("Customer with ID " + customerId + " not found.");
            }
            return true;
        } catch (SQLException | CustomerNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
   public boolean addToCart(Customer customer, Product product, int quantity) throws CustomerNotFoundException, ProductNotFoundException {
    // Check if customer exists
    if (!customerExists(customer.getCustomerId())) {
        throw new CustomerNotFoundException("Customer with ID " + customer.getCustomerId() + " not found.");
    }

    // Check if product exists
    if (!productExists(product.getProductId())) {
        throw new ProductNotFoundException("Product with ID " + product.getProductId() + " not found.");
    }

    // Now proceed to add to cart
    String query = "INSERT INTO cart (customer_id, product_id, quantity) VALUES (?, ?, ?)";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, customer.getCustomerId());
        stmt.setInt(2, product.getProductId());
        stmt.setInt(3, quantity);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
private boolean customerExists(int customerId) {
    String query = "SELECT 1 FROM customers WHERE customer_id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();  // If the result set has a value, the customer exists
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}
private boolean productExists(int productId) {
    String query = "SELECT 1 FROM products WHERE product_id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, productId);
        ResultSet rs = stmt.executeQuery();
        return rs.next();  // If the result set has a value, the product exists
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

    @Override
    public boolean removeFromCart(Customer customer, Product product) {
        String query = "DELETE FROM cart WHERE customer_id = ? AND product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customer.getCustomerId());
            stmt.setInt(2, product.getProductId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Product> getAllFromCart(Customer customer) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.product_id, p.name, p.price, c.quantity " +
                       "FROM cart c " +
                       "JOIN products p ON c.product_id = p.product_id " +
                       "WHERE c.customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customer.getCustomerId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int productId = rs.getInt("product_id");
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity"); // Ensure this matches the column name in the database
                products.add(new Product(productId, name, price, "", quantity));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
    

    @Override
    public boolean placeOrder(Customer customer, List<Map<Product, Integer>> productsWithQuantity, String shippingAddress) {
        String insertOrderQuery = "INSERT INTO orders (customer_id, order_date, total_price, shipping_address) VALUES (?, NOW(), ?, ?)";
        String insertOrderItemsQuery = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)"; // Added price here
        double totalPrice = 0;
    
        try {
            connection.setAutoCommit(false);  // Start transaction
    
            // Calculate total price
            for (Map<Product, Integer> productQuantityMap : productsWithQuantity) {
                for (Product product : productQuantityMap.keySet()) {
                    totalPrice += product.getPrice() * productQuantityMap.get(product);
                }
            }
    
            // Insert into orders table
            PreparedStatement orderStmt = connection.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, customer.getCustomerId());
            orderStmt.setDouble(2, totalPrice);
            orderStmt.setString(3, shippingAddress);
            orderStmt.executeUpdate();
    
            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
    
            // Insert into order_items table
            for (Map<Product, Integer> productQuantityMap : productsWithQuantity) {
                for (Product product : productQuantityMap.keySet()) {
                    try (PreparedStatement orderItemsStmt = connection.prepareStatement(insertOrderItemsQuery)) {
                        orderItemsStmt.setInt(1, orderId);
                        orderItemsStmt.setInt(2, product.getProductId());
                        orderItemsStmt.setInt(3, productQuantityMap.get(product));
                        orderItemsStmt.setDouble(4, product.getPrice()); // Set price for order item
                        orderItemsStmt.executeUpdate();
                    }
                }
            }
    
            connection.commit();  // Commit transaction
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();  // Rollback transaction on failure
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);  // Restore default commit behavior
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    @Override
public List<Map<Product, Integer>> getOrdersByCustomer(int customerId) {
    List<Map<Product, Integer>> orders = new ArrayList<>();
    String query = "SELECT oi.order_id, oi.product_id, oi.quantity, p.name, p.price, p.description "
                 + "FROM order_items oi "
                 + "JOIN products p ON oi.product_id = p.product_id "
                 + "JOIN orders o ON o.order_id = oi.order_id "
                 + "WHERE o.customer_id = ?";

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, customerId);
        ResultSet rs = stmt.executeQuery();

        // Loop through each result and populate the list
        while (rs.next()) {
            Product product = new Product(
                rs.getInt("product_id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("description"),
                0  // Stock quantity is not needed in the order details
            );
            int quantity = rs.getInt("quantity");

            // Create a map to store product and quantity
            Map<Product, Integer> productQuantity = new HashMap<>();
            productQuantity.put(product, quantity);

            // Add the map to the orders list
            orders.add(productQuantity);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return orders;
}
}