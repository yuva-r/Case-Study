# E-Commerce Application (Java + JDBC + SQL)

This is a case study project developed as part of the **Hexaware Java Training Program**. The goal of the project is to build a fully functional **E-Commerce Application** using **Java**, **JDBC**, and **SQL** while applying key programming concepts such as OOP, exception handling, collections, and unit testing.

---

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [Schema Design](#schema-design)
- [Unit Testing](#unit-testing)
- [Author](#author)

---

## Features

- **Customer Management**: Register, update, delete customers; view order history.
- **Product Management**: Add, view, and delete products.
- **Cart Management**: Add/remove products from shopping cart.
- **Order Management**: Place orders, calculate totals, manage order items.
- **Exception Handling**: User-defined exceptions for invalid inputs.
- **Menu-Driven App**: User-friendly command-line menu to operate all functions.

---

## Technologies Used

- Java (OOP Concepts, Exception Handling)
- JDBC
- MySQL
- SQL
- JUnit for Unit Testing

---

## Project Structure

![image](https://github.com/user-attachments/assets/bd161ac0-322c-463e-9155-d08a93b2872d)

---

## Setup Instructions

- **Clone the repository**  
  git clone https://github.com/your-username/ecommerce-app.git  
  cd ecommerce-app

- **Set up the MySQL database**  
  Create the following tables:

  - customers (customer_id, name, email, password)  
  - products (product_id, name, price, description, stockQuantity)  
  - cart (cart_id, customer_id, product_id, quantity)  
  - orders (order_id, customer_id, order_date, total_price, shipping_address)  
  - order_items (order_item_id, order_id, product_id, quantity)

- **Configure DB connection**  
  Edit the `db.properties` file inside the `resources/` folder:

  hostname=localhost  
  port=3306  
  dbname=ecommerce  
  username=root  
  password=yourpassword

- **Compile and Run**  
  javac main/EcomApp.java  
  java main.EcomApp

---

## Schema Design

- **customers**  
  customer_id, name, email, password

- **products**  
  product_id, name, price, description, stockQuantity

- **cart**  
  cart_id, customer_id, product_id, quantity

- **orders**  
  order_id, customer_id, order_date, total_price, shipping_address

- **order_items**  
  order_item_id, order_id, product_id, quantity

---

## Unit Testing

- **Test Cases Include:**  
  - Product creation  
  - Product addition to cart  
  - Order placement

- **Exception Handling:**  
  - CustomerNotFoundException  
  - ProductNotFoundException  
  - OrderNotFoundException

- **How to Run Tests:**  
  - Using Maven: mvn test  
  - Using IDE: Right-click the test class and select "Run"

---

## Developed By

- **Yuvasheee R**  
  Trainee @ Hexaware Technologies

---

