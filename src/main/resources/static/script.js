document.addEventListener('DOMContentLoaded', () => {
    const chatHistory = document.getElementById('chat-history');
    const userInput = document.getElementById('user-input');
    const sendBtn = document.getElementById('send-btn');
    const modelSelect = document.getElementById('model-select');
    const metaPanel = document.getElementById('meta-insights-panel');
    const insightGemma = document.getElementById('insight-gemma');
    const insightDeepseek = document.getElementById('insight-deepseek');
    const themeToggle = document.getElementById('theme-toggle');

    // Theme Management
    const currentTheme = localStorage.getItem('theme') || 'dark';
    document.documentElement.setAttribute('data-theme', currentTheme);
    updateThemeIcon(currentTheme);

    themeToggle.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';

        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
        updateThemeIcon(newTheme);
    });

    function updateThemeIcon(theme) {
        const icon = themeToggle.querySelector('i');
        if (theme === 'dark') {
            icon.className = 'fas fa-sun';
        } else {
            icon.className = 'fas fa-moon';
        }
    }

    // About Modal Management
    const aboutBtn = document.getElementById('about-btn');
    const aboutModal = document.getElementById('about-modal');
    const modalClose = document.getElementById('modal-close');

    aboutBtn.addEventListener('click', () => {
        aboutModal.classList.add('show');
    });

    modalClose.addEventListener('click', () => {
        aboutModal.classList.remove('show');
    });

    // Close modal when clicking outside of it
    aboutModal.addEventListener('click', (e) => {
        if (e.target === aboutModal) {
            aboutModal.classList.remove('show');
        }
    });

    // Close modal with Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && aboutModal.classList.contains('show')) {
            aboutModal.classList.remove('show');
        }
    });

    // Initial state check
    updatePanelVisibility();

    modelSelect.addEventListener('change', () => {
        updatePanelVisibility();
        // Add selection animation
        modelSelect.style.transform = 'scale(1.05)';
        setTimeout(() => {
            modelSelect.style.transform = 'scale(1)';
        }, 200);
    });

    function updatePanelVisibility() {
        if (modelSelect.value === 'nvidia') {
            metaPanel.style.opacity = '1';
            metaPanel.style.transform = 'translateX(0)';
            metaPanel.style.pointerEvents = 'all';
        } else {
            metaPanel.style.opacity = '0.5';
            metaPanel.style.pointerEvents = 'none';
        }
    }

    // Auto-resize textarea
    userInput.addEventListener('input', () => {
        userInput.style.height = 'auto';
        userInput.style.height = userInput.scrollHeight + 'px';
    });

    // Handle Enter and Shift+Enter
    userInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault(); // Prevent new line
            handleSend();
        }
        // Shift+Enter will naturally create a new line
    });

    sendBtn.addEventListener('click', handleSend);

    async function handleSend() {
        const message = userInput.value.trim();
        if (!message) return;

        // Clear input
        userInput.value = '';
        userInput.style.height = 'auto'; // Reset height

        // Add User Message
        addMessage(message, 'user');

        // Loading state
        const loadingId = addLoadingMessage();

        const model = modelSelect.value;
        let mainEndpoint = `/api/${model}/${encodeURIComponent(message)}`;

        try {
            // Fetch Main Response
            const response = await fetch(mainEndpoint);
            if (!response.ok) throw new Error('Network response was not ok');
            const data = await response.text();

            // Remove loading and add AI message
            removeMessage(loadingId);
            addMessage(data, 'ai', getModelDisplayName(model));

            // If Meta-Agent (Nvidia) is selected, fetch hidden insights
            if (model === 'nvidia') {
                fetchInsights();
            } else {
                clearInsights();
            }

        } catch (error) {
            removeMessage(loadingId);
            addMessage("Error: Could not connect to the Intelligence.", 'ai', 'System');
            console.error('Error:', error);
        }
    }

    async function fetchInsights() {
        // Set loading state for insights
        insightGemma.innerHTML = '<span class="pulse">Extracting parameters...</span>';
        insightDeepseek.innerHTML = '<span class="pulse">Extracting parameters...</span>';

        // Parallel fetch for sub-responses
        // Note: The controller logic suggests these endpoints return the cached response 
        // from the LAST nvidia call. This implies a stateful backend which might be race-condition prone,
        // but we will match the provided API implementation.

        try {
            const [gemmaRes, deepseekRes] = await Promise.all([
                fetch('/api/nvidia/firstmodelresponse').then(r => r.text()),
                fetch('/api/nvidia/secondmodelresponse').then(r => r.text())
            ]);

            insightGemma.textContent = gemmaRes || "No response data available.";
            insightDeepseek.textContent = deepseekRes || "No response data available.";

        } catch (e) {
            insightGemma.textContent = "Failed to retrieve insight.";
            insightDeepseek.textContent = "Failed to retrieve insight.";
        }
    }

    function clearInsights() {
        insightGemma.textContent = "Waiting for query...";
        insightDeepseek.textContent = "Waiting for query...";
    }

    function addMessage(text, type, senderName = "You") {
        const div = document.createElement('div');
        div.className = `message ${type}`;

        const header = document.createElement('div');
        header.className = 'message-header';
        header.textContent = senderName;

        const content = document.createElement('div');
        content.textContent = text; // Secure text insertion

        div.appendChild(header);
        div.appendChild(content);
        chatHistory.appendChild(div);

        // Remove welcome message if it exists
        const welcome = document.querySelector('.welcome-message');
        if (welcome) welcome.remove();

        scrollToBottom();
    }

    function addLoadingMessage() {
        const id = 'loading-' + Date.now();
        const div = document.createElement('div');
        div.id = id;
        div.className = 'message ai';
        div.innerHTML = '<div class="message-header">AI is thinking...</div><div class="typing-indicator"><span>.</span><span>.</span><span>.</span></div>';
        chatHistory.appendChild(div);
        scrollToBottom();
        return id;
    }

    function removeMessage(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    function scrollToBottom() {
        chatHistory.scrollTop = chatHistory.scrollHeight;
    }

    function getModelDisplayName(model) {
        switch (model) {
            case 'nvidia': return 'Meta-Agent (Nvidia)';
            case 'gemma': return 'Gemma (Google)';
            case 'deepseek': return 'DeepSeek';
            default: return 'AI';
        }
    }
});


