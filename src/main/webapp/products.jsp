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

        const reviewBtn = document.createElement('button');
        reviewBtn.innerText = 'Reviews';
        reviewBtn.onclick = () => toggleReviews(p.id);
        div.appendChild(reviewBtn);

        const reviewSection = document.createElement('div');
        reviewSection.id = 'reviews-' + p.id;
        reviewSection.style.display = 'none';
        reviewSection.style.marginLeft = '20px';
        div.appendChild(reviewSection);

        list.appendChild(div);
    });
}

async function toggleReviews(productId) {
    const section = document.getElementById('reviews-' + productId);
    if (section.style.display === 'none') {
        await loadReviews(productId);
        section.style.display = 'block';
    } else {
        section.style.display = 'none';
    }
}

async function loadReviews(productId) {
    const section = document.getElementById('reviews-' + productId);
    section.innerHTML = 'Loading...';

    const res = await fetch('api/v1/reviews?productId=' + productId);
    const data = await res.json();

    section.innerHTML = '';
    (data.data || []).forEach(r => {
        const div = document.createElement('div');
        div.innerText = '★'.repeat(r.rating) + ' — ' + (r.comment || '');
        section.appendChild(div);
    });

    const ratingInput = document.createElement('input');
    ratingInput.type = 'number';
    ratingInput.min = 1;
    ratingInput.max = 5;
    ratingInput.placeholder = 'Rating 1-5';
    ratingInput.id = 'rating-' + productId;
    section.appendChild(ratingInput);

    const commentInput = document.createElement('input');
    commentInput.placeholder = 'Comment';
    commentInput.id = 'comment-' + productId;
    section.appendChild(commentInput);

    const submitBtn = document.createElement('button');
    submitBtn.innerText = 'Submit Review';
    submitBtn.onclick = () => submitReview(productId);
    section.appendChild(submitBtn);
}

async function submitReview(productId) {
    const rating = parseInt(document.getElementById('rating-' + productId).value);
    const comment = document.getElementById('comment-' + productId).value;

    const res = await fetch('api/v1/reviews', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ productId, rating, comment })
    });
    const data = await res.json();
    document.getElementById('msg').innerText = data.success ? 'Review submitted' : data.error.message;
    if (data.success) loadReviews(productId);
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