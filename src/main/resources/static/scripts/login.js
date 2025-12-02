// ========== LOGIN PAGE - Scripts específicos ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('Login page - Script carregado');

    // Verifica se já está logado
    const token = localStorage.getItem('jwtToken');
    if (token) {
        // Se já tem token, redireciona para home
        window.location.href = '/';
        return;
    }

    // Verifica se veio de um registro bem-sucedido
    const urlParams = new URLSearchParams(window.location.search);
    const registroSucesso = urlParams.get('registroSucesso');

    if (registroSucesso === 'true') {
        showNotification('Conta criada com sucesso! Faça login para continuar.', 'success');
    }

    // ========== FORMULÁRIO DE LOGIN ==========
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const button = document.getElementById('login-button');

            // Salva estado do botão
            const originalText = button.innerHTML;
            button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Entrando...';
            button.disabled = true;

            // Limpa mensagem anterior
            const messageDiv = document.getElementById('login-message');
            if (messageDiv) {
                messageDiv.style.display = 'none';
            }

            // Faz login via API
            fazerLogin(email, password)
                .catch(error => {
                    // Mostra mensagem de erro
                    if (messageDiv) {
                        messageDiv.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + error.message;
                        messageDiv.className = 'alert alert-error';
                        messageDiv.style.display = 'block';
                    }

                    // Restaura botão
                    button.innerHTML = originalText;
                    button.disabled = false;
                });
        });
    }

    // ========== ESQUECI MINHA SENHA ==========
    const forgotPasswordLink = document.getElementById('forgot-password');
    if (forgotPasswordLink) {
        forgotPasswordLink.addEventListener('click', function(e) {
            e.preventDefault();
            showNotification('Em breve implementaremos a recuperação de senha!', 'warning');
        });
    }

    console.log('Login page scripts inicializados');
});