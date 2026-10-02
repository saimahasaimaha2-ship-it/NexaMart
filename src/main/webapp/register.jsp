<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<title>Register — NexaMart</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
<div class="navbar">
    <a class="brand" href="products.jsp">NexaMart</a>
    <div class="nav-links">
        <a href="products.jsp">Shop</a>
        <a href="login.jsp">Login</a>
    </div>
</div>
<div class="auth-wrap">
<form id="registerForm">
    <h2 style="margin-top:0;">Create your account</h2>
    <p style="color:var(--text-muted); margin-top:-10px; margin-bottom:18px; font-size:14px;">Join NexaMart to buy or sell</p>
    <input name="name" placeholder="Name" required><br>
    <input name="email" type="email" placeholder="Email" required><br>
    <input name="password" type="password" placeholder="Password" required><br>
    <select name="role">
        <option value="BUYER">Buyer</option>
        <option value="SELLER">Seller</option>
    </select><br>
    <button type="submit">Register</button>
</form>
</div>
<p id="msg" style="text-align:center;"></p>
<script src="js/app.js"></script>
<script>
document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const body = Object.fromEntries(fd.entries());
    const res = await fetch('api/v1/auth/register', {
        method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)
    });
    const data = await res.json();
    document.getElementById('msg').innerText = data.success ? 'Registered! You can log in now.' : data.error.message;
});
</script>
</body>
</html>