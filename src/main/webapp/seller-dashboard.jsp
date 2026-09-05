<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><title>Seller Dashboard — NexaMart</title></head>
<body>
<h2>Seller Dashboard</h2>
<p id="msg"></p>

<h3>Add New Product</h3>
<input id="pName" placeholder="Name"><br>
<input id="pDesc" placeholder="Description"><br>
<input id="pPrice" placeholder="Price" type="number"><br>
<input id="pStock" placeholder="Stock Qty" type="number"><br>
<input id="pCategory" placeholder="Category"><br>
<input id="pImage" placeholder="Image URL"><br>
<button onclick="createProduct()">Create Product</button>

<h3>My Products</h3>
<div id="myProducts"></div>

<h3>Incoming Orders</h3>
<div id="orderList"></div>

<script>
const currentSellerId = ${sessionScope.userId};

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
        div.innerText = p.name + ' — ₹' + p.price + ' (' + p.stockQty + ' in stock) ';

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