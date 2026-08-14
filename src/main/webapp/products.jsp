<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><title>Products — NexaMart</title></head>
<body>
<h2>Products</h2>
<input id="searchBox" placeholder="Search...">
<button onclick="loadProducts()">Search</button>
<div id="productList"></div>
<h3>Cart</h3>
<div id="cartList"></div>
<button onclick="checkout()">Checkout</button>
<p id="msg"></p>

<script>
async function loadProducts() {
    const q = document.getElementById('searchBox').value;
    const res = await fetch('api/v1/products?q=' + encodeURIComponent(q));
    const data = await res.json();
    const list = document.getElementById('productList');
    list.innerHTML = '';
    (data.data || []).forEach(p => {
        const div = document.createElement('div');
        div.innerText = p.name + ' — ₹' + p.price + ' (' + p.stockQty + ' in stock) ';
        const btn = document.createElement('button');
        btn.innerText = 'Add to cart';
        btn.onclick = () => addToCart(p.id);
        div.appendChild(btn);
        list.appendChild(div);
    });
}

async function addToCart(productId) {
    const res = await fetch('api/v1/cart', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ productId, quantity: 1 })
    });
    const data = await res.json();
    document.getElementById('msg').innerText = data.success ? 'Added to cart' : data.error.message;
    loadCart();
}

async function loadCart() {
    const res = await fetch('api/v1/cart');
    const data = await res.json();
    const list = document.getElementById('cartList');
    list.innerHTML = '';
    (data.data || []).forEach(i => {
        const div = document.createElement('div');
        div.innerText = i.productName + ' x' + i.quantity + ' — ₹' + (i.unitPrice * i.quantity);
        list.appendChild(div);
    });
}

async function checkout() {
    const res = await fetch('api/v1/checkout', { method: 'POST' });
    const data = await res.json();
    document.getElementById('msg').innerText = data.success ? 'Order placed! ID ' + data.data.id : data.error.message;
    if (data.success) loadCart();
}

loadProducts();
loadCart();
</script>
</body>
</html>
