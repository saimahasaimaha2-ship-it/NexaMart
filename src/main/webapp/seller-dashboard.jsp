<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<title>Seller Dashboard — NexaMart</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="navbar">
    <a class="brand" href="products.jsp">NexaMart</a>
    <div class="nav-links">
        <a href="products.jsp">Shop</a>
        <a href="seller-dashboard.jsp">Seller Dashboard</a>
    </div>
</div>
<div class="page-content">
<h2>Seller Dashboard</h2>
<p id="msg" style="text-align:center;"></p>

<h3>Add New Product</h3>
<div class="form-center-wrap">
<form onsubmit="return false;">
    <input id="pName" placeholder="Name"><br>
    <input id="pDesc" placeholder="Description"><br>
    <input id="pPrice" placeholder="Price" type="number"><br>
    <input id="pStock" placeholder="Stock Qty" type="number"><br>
    <input id="pCategory" placeholder="Category"><br>
    <input id="pImage" placeholder="Image URL (e.g. https://...)"><br>
    <button onclick="createProduct()">Create Product</button>
</form>
</div>

<h3>My Products</h3>
<div id="myProducts"></div>

<h3>Incoming Orders</h3>
<div id="orderList"></div>
</div>

<script>
const currentSellerId = ${empty sessionScope.userId ? 'null' : sessionScope.userId};

async function createProduct() {
    const body = {
        name: document.getElementById('pName').value,
        description: document.getElementById('pDesc').value,
        price: parseFloat(document.getElementById('pPrice').value),
        stockQty: parseInt(document.getElementById('pStock').value),
        category: document.getElementById('pCategory').value,
        imageUrl: document.getElementById('pImage').value
    };
    const res = await fetch('api/v1/products', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body)
    });
    const data = await res.json();
    document.getElementById('msg').innerText = data.success ? 'Product created' : data.error.message;
    if (data.success) loadMyProducts();
}

async function loadMyProducts() {
    const res = await fetch('api/v1/products');
    const data = await res.json();
    const list = document.getElementById('myProducts');
    list.innerHTML = '';
    (data.data || []).filter(p => p.sellerId === currentSellerId).forEach(p => {
        const div = document.createElement('div');

        const img = document.createElement('img');
        img.className = 'my-product-thumb';
        img.src = (p.imageUrl && p.imageUrl.trim() !== '') ? p.imageUrl : '';
        img.alt = p.name;
        img.onerror = function() { this.style.display = 'none'; };
        if (!p.imageUrl || p.imageUrl.trim() === '') img.style.display = 'none';
        div.appendChild(img);

        const info = document.createElement('span');
        info.innerText = p.name + ' — ₹' + p.price + ' (' + p.stockQty + ' in stock) ';
        div.appendChild(info);

        const editBtn = document.createElement('button');
        editBtn.innerText = 'Edit Price';
        editBtn.onclick = () => editProduct(p);
        div.appendChild(editBtn);

        const delBtn = document.createElement('button');
        delBtn.innerText = 'Delete';
        delBtn.onclick = () => deleteProduct(p.id);
        div.appendChild(delBtn);

        list.appendChild(div);
    });
}

async function editProduct(p) {
    const newPrice = prompt('New price for ' + p.name, p.price);
    if (newPrice === null) return;
    const body = {
        name: p.name, description: p.description, price: parseFloat(newPrice),
        stockQty: p.stockQty, category: p.category, imageUrl: p.imageUrl
    };
    const res = await fetch('api/v1/products/' + p.id, {
        method: 'PUT', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(body)
    });
    const data = await res.json();
    document.getElementById('msg').innerText = data.success ? 'Product updated' : data.error.message;
    if (data.success) loadMyProducts();
}

async function deleteProduct(id) {
    if (!confirm('Delete this product?')) return;
    const res = await fetch('api/v1/products/' + id, { method: 'DELETE' });
    const data = await res.json();
    document.getElementById('msg').innerText = data.success ? 'Product deleted' : data.error.message;
    if (data.success) loadMyProducts();
}

async function loadOrders() {
    const res = await fetch('api/v1/orders');
    const data = await res.json();
    const list = document.getElementById('orderList');
    list.innerHTML = '';
    (data.data || []).forEach(o => {
        const div = document.createElement('div');
        let itemsText = (o.items || []).map(i => i.productName + ' x' + i.quantity).join(', ');
        div.innerText = 'Order #' + o.id + ' — ' + o.status + ' — ₹' + o.totalAmount + ' — ' + itemsText;
        list.appendChild(div);
    });
}

loadMyProducts();
loadOrders();
</script>
</body>
</html>