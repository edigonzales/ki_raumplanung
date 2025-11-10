<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Chat Debug Console</title>
</head>
<body>
<h1>Chat Debug Console</h1>
<form id="chat-form">
    <label for="question">Question:</label><br>
    <textarea id="question" name="question" rows="6" cols="60" required></textarea><br>
    <button type="submit">Send</button>
</form>
<script>
    const form = document.getElementById('chat-form');
    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const question = document.getElementById('question').value;
        try {
            await fetch('/api/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ question })
            });
        } catch (error) {
            console.error('Failed to send question', error);
        }
    });
</script>
</body>
</html>
