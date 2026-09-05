<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><title>Admin Dashboard — NexaMart</title></head>
<body>
<h2>Admin Dashboard</h2>
<p id="msg"></p>

<h3>Users</h3>
<div id="userList"></div>

<h3>All Products</h3>
<div id="productList"></div>

<h3>All Orders</h3>
<div id="orderList"></div>

<script>
async function loadUsers() {
    const res = await fetch('api/v1/admin/users');
    const data = await res.json();
    const list = document.getElementById('userList');
    list.innerHTML = '';
    (data.data || []).forEach(u => {
        const div = document.createElement('div');
        div.innerText = '#' + u.id + ' — ' + u.name + ' (' + u.email + ') — ' + u.role;
        list.appendChild(div);
    });
}

async function loadProducts() {
    const res = await fetch('api/v1/admin/products');
    const data = await res.json();
    const list = document.getElementById('productList');
    list.innerHTML = '';
    (data.data || []).forEach(p => {
        const div = document.createElement('div');
        div.innerText = p.name + ' — ₹' + p.price + ' (' + p.stockQty + ' in stock) — sellerId ' + p.sellerId + ' ';
        const delBtn = document.createElement('button');
        delBtn.innerText = 'Delete';
        delBtn.onclick = () => deleteProduct(p.id);
        div.appendChild(delBtn);
        list.appendChild(div);
    });
}

async function deleteProduct(id) {
    if (!confirm('Delete this product?')) return;
    const res = await fetch('api/v1/admin/products/' + id, { method: 'DELETE' });
    const data = await res.json();
    document.getElementById('msg').innerText = data.success ? 'Product deleted' : data.error.message;
    if (data.success) loadProducts();
}

async function loadOrders() {
    const res = await fetch('api/v1/admin/orders');
    const data = await res.json();
    const list = document.getElementById('orderList');
    list.innerHTML = '';
    (data.data || []).forEach(o => {
        const div = document.createElement('div');
        let itemsText = (o.items || []).map(i => i.productName + ' x' + i.quantity).join(', ');
        div.innerText = 'Order #' + o.id + ' — buyerId ' + o.buyerId + ' — ' + o.status + ' — ₹' + o.totalAmount + ' — ' + itemsText;
        list.appendChild(div);
    });
}

loadUsers();
loadProducts();
loadOrders();
</script>
</body>
</html>