package com.nexamart.nexamart.service;

public class MockChatProvider implements ChatProvider {

    @Override
    public String getReply(String userMessage, String context) {
        String msg = userMessage.toLowerCase();

        if (msg.contains("what is nexamart") || msg.contains("about nexamart")) {
            return "NexaMart is a multi-seller marketplace where sellers list products and buyers can browse, search, and purchase them.";
        }
        if (msg.contains("create") && msg.contains("account") || msg.contains("register") || msg.contains("sign up")) {
            return "To create an account, go to the Register page, enter your name, email, and password, and choose whether you're a Buyer or a Seller.";
        }
        if (msg.contains("sell") || (msg.contains("list") && msg.contains("product"))) {
            return "To sell a product, register or log in as a Seller, then go to your Seller Dashboard and use the 'Add New Product' form.";
        }
        if (msg.contains("search") || msg.contains("find product") || msg.contains("filter")) {
            return "You can search for products using the search box on the Products page. You can search by keyword or filter by category.";
        }
        if (msg.contains("checkout") || msg.contains("place order") || msg.contains("buy")) {
            return "Add items to your cart from the Products page, then click Checkout. This places your order using our mock payment confirmation.";
        }
        if (msg.contains("contact") && msg.contains("seller")) {
            return "Currently, buyers cannot directly message sellers. Reviews are the main way to leave feedback on a product.";
        }
        if (msg.contains("payment") && (msg.contains("method") || msg.contains("secure") || msg.contains("safe"))) {
            return "NexaMart uses a mock payment confirmation for this demo project — no real payment gateway or card details are processed.";
        }
        if (msg.contains("review") || msg.contains("rating") || msg.contains("rate")) {
            return "You can leave a review and star rating on any product you've purchased, from the product's Reviews section.";
        }
        if (msg.contains("track") && msg.contains("order")) {
            return "You can view your past orders and their status on the Order History page after logging in as a Buyer.";
        }
        if (msg.contains("hello") || msg.contains("hi") || msg.equals("hey")) {
            return "Hello! I'm the NexaMart assistant. Ask me about creating an account, selling products, searching, checkout, or reviews.";
        }

        return "I'm not sure about that. I can help with questions about accounts, selling products, searching, checkout, payments, and reviews.";
    }
}