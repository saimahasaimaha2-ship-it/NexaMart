<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>NexaMart</title>
  <link rel="stylesheet" href="css/style.css">
  <style>
    body { margin: 0; font-family: 'Segoe UI', Arial, sans-serif; background: #faf9fd; }
    .landing-nav { background: linear-gradient(90deg, #5b21b6, #4c1d95); padding: 22px 40px; }
    .landing-nav span { color: #fff; font-size: 1.6rem; font-weight: 800; }
    .landing-hero {
      min-height: calc(100vh - 80px);
      display: flex; flex-direction: column;
      align-items: center; justify-content: center;
      text-align: center; padding: 0 20px;
    }
    .landing-hero h1 { margin: 0 0 12px; font-size: 3.2rem; color: #4c1d95; }
    .landing-hero p { margin: 0 0 28px; font-size: 1.15rem; color: #6b6b80; }
    .landing-actions { display: flex; gap: 14px; flex-wrap: wrap; justify-content: center; }
    .landing-btn {
      padding: 12px 28px; border-radius: 999px; text-decoration: none;
      font-weight: 600; font-size: 1rem;
      border: 2px solid #5b21b6; color: #5b21b6; background: #fff;
    }
    .landing-btn.primary { background: linear-gradient(90deg, #7c3aed, #5b21b6); color: #fff; }
    .landing-btn:hover { opacity: .88; }
  </style>
</head>
<body>
  <div class="landing-nav"><span>NexaMart</span></div>
  <div class="landing-hero">
    <h1>Welcome to NexaMart</h1>
    <p>Buy and sell products from sellers all in one marketplace.</p>
    <div class="landing-actions">
      <a class="landing-btn primary" href="products.jsp">Browse products</a>
      <a class="landing-btn" href="login.jsp">Login</a>
      <a class="landing-btn" href="register.jsp">Register</a>
    </div>
  </div>
</body>
</html>