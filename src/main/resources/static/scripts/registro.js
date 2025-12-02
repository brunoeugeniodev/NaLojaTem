// ========== REGISTRO PAGE - Scripts específicos ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('Registro page - Script carregado');

    // ========== FORMULÁRIO DE REGISTRO ==========
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            e.preventDefault();

            // Validações
            const senha = document.getElementById('senha').value;
            const confirmarSenha = document.getElementById('confirmar-senha').value;

            if (senha !== confirmarSenha) {
                const messageDiv = document.getElementById('registro-message');
                if (messageDiv) {
                    messageDiv.innerHTML = '<i class="fas fa-exclamation-circle"></i> As senhas não coincidem!';
                    messageDiv.className = 'alert alert-error';
                    messageDiv.style.display = 'block';
                }
                return;
            }

            if (senha.length < 6) {
                const messageDiv = document.getElementById('registro-message');
                if (messageDiv) {
                    messageDiv.innerHTML = '<i class="fas fa-exclamation-circle"></i> A senha deve ter no mínimo 6 caracteres!';
                    messageDiv.className = 'alert alert-error';
                    messageDiv.style.display = 'block';
                }
                return;
            }

            // Coleta dados do formulário
            const usuario = {
                nome: document.getElementById('nome').value,
                cpf: document.getElementById('cpf').value.replace(/\D/g, ''),
                email: document.getElementById('email').value,
                senha: senha,
                telefone: document.getElementById('telefone').value.replace(/\D/g, ''),
                tipo: document.getElementById('tipo').value,
                roles: ['ROLE_USER'] // Adiciona role padrão
            };

            const button = document.getElementById('register-button');
            const originalText = button.innerHTML;
            button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Criando conta...';
            button.disabled = true;

            // Limpa mensagem anterior
            const messageDiv = document.getElementById('registro-message');
            if (messageDiv) {
                messageDiv.style.display = 'none';
            }

            // Faz registro via API
            fetch('/api/auth/registro', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(usuario)
            })
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    throw new Error(data.error);
                }

                if (messageDiv) {
                    messageDiv.innerHTML = '<i class="fas fa-check-circle"></i> Conta criada com sucesso! Faça login.';
                    messageDiv.className = 'alert alert-success';
                    messageDiv.style.display = 'block';
                }

                // Redireciona para login após 2 segundos
                setTimeout(() => {
                    window.location.href = '/login?registroSucesso=true';
                }, 2000);
            })
            .catch(error => {
                if (messageDiv) {
                    messageDiv.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + error.message;
                    messageDiv.className = 'alert alert-error';
                    messageDiv.style.display = 'block';
                }

                button.innerHTML = originalText;
                button.disabled = false;
            });
        });
    }

    // ========== REDIRECIONAR PARA LOGIN ==========
    const loginLink = document.querySelector('a[href*="/login"]');
    if (loginLink) {
        loginLink.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/login';
        });
    }

    console.log('Registro page scripts inicializados');
});