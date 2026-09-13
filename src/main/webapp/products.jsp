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

<!-- Chat Widget -->
<button id="chatToggleBtn" onclick="toggleChat()" style="position:fixed; bottom:20px; right:20px; padding:12px 18px; border-radius:50px; background:#333; color:white; border:none; cursor:pointer; font-size:16px; z-index:1000;">💬 Chat</button>

<div id="chatPanel" style="display:none; position:fixed; bottom:80px; right:20px; width:300px; height:400px; border:1px solid #ccc; background:white; box-shadow:0 2px 10px rgba(0,0,0,0.2); border-radius:8px; z-index:1000; flex-direction:column;">
    <div style="background:#333; color:white; padding:10px; border-radius:8px 8px 0 0; font-weight:bold;">NexaMart Assistant</div>
    <div id="chatMessages" style="flex:1; overflow-y:auto; padding:10px; font-size:14px;"></div>
    <div style="display:flex; border-top:1px solid #ccc;">
        <input id="chatInput" placeholder="Ask a question..." style="flex:1; border:none; padding:8px;" onkeydown="if(event.key==='Enter') sendChatMessage()">
        <button onclick="sendChatMessage()" style="border:none; background:#333; color:white; padding:8px 12px; cursor:pointer;">Send</button>
    </div>
</div>

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

// Chat widget functions
function toggleChat() {
    const panel = document.getElementById('chatPanel');
    if (panel.style.display === 'none' || panel.style.display === '') {
        panel.style.display = 'flex';
        if (document.getElementById('chatMessages').children.length === 0) {
            addChatMessage('assistant', "Hi! I'm the NexaMart assistant. Ask me about accounts, selling, searching, checkout, payments, or reviews.");
        }
    } else {
        panel.style.display = 'none';
    }
}

function addChatMessage(sender, text) {
    const messages = document.getElementById('chatMessages');
    const div = document.createElement('div');
    div.style.margin = '6px 0';
    div.style.textAlign = sender === 'user' ? 'right' : 'left';
    const bubble = document.createElement('span');
    bubble.style.display = 'inline-block';
    bubble.style.padding = '6px 10px';
    bubble.style.borderRadius = '10px';
    bubble.style.background = sender === 'user' ? '#333' : '#eee';
    bubble.style.color = sender === 'user' ? 'white' : 'black';
    bubble.style.maxWidth = '80%';
    bubble.innerText = text;
    div.appendChild(bubble);
    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
}

async function sendChatMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();
    if (!message) return;
    addChatMessage('user', message);
    input.value = '';

    try {
        const res = await fetch('api/v1/chat', {
            method: 'POST', headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ message })
        });
        const data = await res.json();
        addChatMessage('assistant', data.success ? data.data.reply : data.error.message);
    } catch (e) {
        addChatMessage('assistant', "Sorry, I'm having trouble answering right now. Please try again later.");
    }
}

loadProducts();
loadCart();
</script>
</body>
</html>