<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head><title>Register — NexaMart</title></head>
<body>
<h2>Register</h2>
<form id="registerForm">
    <input name="name" placeholder="Name" required><br>
    <input name="email" type="email" placeholder="Email" required><br>
    <input name="password" type="password" placeholder="Password" required><br>
    <select name="role">
        <option value="BUYER">Buyer</option>
        <option value="SELLER">Seller</option>
    </select><br>
    <button type="submit">Register</button>
</form>
<p id="msg"></p>
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
