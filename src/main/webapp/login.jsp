<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<title>Login — NexaMart</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
<h2>Login</h2>
<form id="loginForm">
    <input name="email" type="email" placeholder="Email" required><br>
    <input name="password" type="password" placeholder="Password" required><br>
    <button type="submit">Login</button>
</form>
<p id="msg"></p>
<script>
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(e.target);
    const body = Object.fromEntries(fd.entries());
    const res = await fetch('api/v1/auth/login', {
        method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)
    });
    const data = await res.json();
    if (data.success) {
        const role = data.data.role;
        if (role === 'SELLER') {
            window.location.href = 'seller-dashboard.jsp';
        } else if (role === 'ADMIN') {
            window.location.href = 'admin-dashboard.jsp';
        } else {
            window.location.href = 'products.jsp';
        }
    } else {
        document.getElementById('msg').innerText = data.error.message;
    }
});
</script>
</body>
</html>