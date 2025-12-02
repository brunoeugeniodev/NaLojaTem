// ========== FUNÇÕES GLOBAIS ==========

// 1. Função para obter cabeçalhos de autenticação (JWT)
window.getAuthHeader = function() {
    const token = localStorage.getItem('jwtToken');
    return token ? {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    } : {
        'Content-Type': 'application/json'
    };
};

// 2. Função para fazer login via API (JWT)
window.fazerLogin = function(email, senha) {
    return fetch('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            email: email,
            senha: senha
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Credenciais inválidas');
        }
        return response.json();
    })
    .then(data => {
        // Salva o token JWT no localStorage
        localStorage.setItem('jwtToken', data.token);
        localStorage.setItem('usuario', JSON.stringify({
            username: data.username,
            authorities: data.authorities
        }));

        showNotification('Login realizado com sucesso!', 'success');

        // Redireciona para a página inicial após 1.5 segundos
        setTimeout(() => {
            window.location.href = '/';
        }, 1500);

        return data;
    });
};

// 3. Função para fazer logout
window.fazerLogout = function() {
    // Remove o token JWT do localStorage
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('usuario');

    // Tenta fazer logout no backend
    fetch('/api/auth/logout', {
        method: 'POST',
        headers: getAuthHeader()
    })
    .then(() => {
        showNotification('Logout realizado com sucesso!', 'success');

        // Redireciona para a página inicial
        setTimeout(() => {
            window.location.href = '/';
        }, 1500);
    })
    .catch(error => {
        console.error('Erro ao fazer logout:', error);
        // Mesmo se der erro no backend, limpa o frontend
        showNotification('Logout realizado!', 'success');
        setTimeout(() => {
            window.location.href = '/';
        }, 1500);
    });
};

// 4. Carregar contagem do carrinho
window.loadCartCount = function() {
    fetch('/api/carrinho', {
        method: 'GET',
        headers: getAuthHeader()
    })
    .then(response => {
        if (!response.ok) {
            return { totalItens: 0 };
        }
        return response.json();
    })
    .then(data => {
        const cartCountElement = document.getElementById('cart-count');
        if (cartCountElement) {
            cartCountElement.textContent = data.totalItens || 0;
        }
    })
    .catch(error => {
        console.log("Não foi possível carregar contagem do carrinho:", error);
    });
};

// 5. Mostrar notificação
window.showNotification = function(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type}`;
    notification.innerHTML = `
        <div style="display: flex; align-items: center; gap: 10px;">
            <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
            <span>${message}</span>
        </div>
        <button onclick="this.parentElement.remove()" style="background: none; border: none; color: inherit; cursor: pointer; margin-left: auto;">
            <i class="fas fa-times"></i>
        </button>
    `;

    document.body.appendChild(notification);

    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.zIndex = '1000';
    notification.style.minWidth = '300px';
    notification.style.maxWidth = '500px';
    notification.style.boxShadow = '0 4px 15px rgba(0,0,0,0.2)';

    setTimeout(() => {
        if (notification.parentElement) {
            notification.remove();
        }
    }, 5000);
};

// ========== FUNÇÕES DE FORMULÁRIO ==========

// Máscara para CPF
window.aplicarMascaraCPF = function(cpfInput) {
    if (cpfInput) {
        cpfInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 11) {
                value = value.replace(/(\d{3})(\d)/, '$1.$2')
                            .replace(/(\d{3})(\d)/, '$1.$2')
                            .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
                e.target.value = value;
            }
        });
    }
};

// Máscara para CNPJ
window.aplicarMascaraCNPJ = function(cnpjInput) {
    if (cnpjInput) {
        cnpjInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 14) {
                value = value.replace(/(\d{2})(\d)/, '$1.$2')
                            .replace(/(\d{3})(\d)/, '$1.$2')
                            .replace(/(\d{3})(\d)/, '$1/$2')
                            .replace(/(\d{4})(\d)/, '$1-$2');
                e.target.value = value;
            }
        });
    }
};

// Máscara para telefone
window.aplicarMascaraTelefone = function(telInput) {
    if (telInput) {
        telInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 10) {
                value = value.replace(/(\d{2})(\d)/, '($1) $2')
                            .replace(/(\d{4})(\d)/, '$1-$2');
            } else {
                value = value.replace(/(\d{2})(\d)/, '($1) $2')
                            .replace(/(\d{5})(\d)/, '$1-$2');
            }
            e.target.value = value;
        });
    }
};

// Máscara para CEP
window.aplicarMascaraCEP = function(cepInput) {
    if (cepInput) {
        cepInput.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, '');
            if (value.length <= 8) {
                value = value.replace(/(\d{5})(\d)/, '$1-$2');
                e.target.value = value;

                // Buscar endereço automaticamente quando CEP estiver completo
                if (value.length === 9) {
                    buscarEnderecoPorCEP(value);
                }
            }
        });
    }
};

window.buscarEnderecoPorCEP = function(cep) {
    const cepLimpo = cep.replace(/\D/g, '');
    if (cepLimpo.length === 8) {
        fetch(`https://viacep.com.br/ws/${cepLimpo}/json/`)
            .then(response => response.json())
            .then(data => {
                if (!data.erro) {
                    const logradouroInput = document.getElementById('logradouro');
                    const bairroInput = document.getElementById('bairro');
                    const cidadeInput = document.getElementById('cidade');
                    const estadoInput = document.getElementById('estado');

                    if (logradouroInput) logradouroInput.value = data.logradouro;
                    if (bairroInput) bairroInput.value = data.bairro;
                    if (cidadeInput) cidadeInput.value = data.localidade;
                    if (estadoInput) estadoInput.value = data.uf;

                    if (window.showNotification) {
                        window.showNotification('Endereço preenchido automaticamente!', 'success');
                    }
                }
            })
            .catch(error => {
                console.error('Erro ao buscar CEP:', error);
            });
    }
};

// ========== AJAX HELPER ==========
window.ajaxRequest = function(url, method = 'GET', data = null) {
    return new Promise((resolve, reject) => {
        const headers = {
            'Content-Type': 'application/json',
            ...window.getAuthHeader ? window.getAuthHeader() : {}
        };

        const options = {
            method: method,
            headers: headers
        };

        if (data && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
            options.body = JSON.stringify(data);
        }

        fetch(url, options)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => resolve(data))
            .catch(error => reject(error));
    });
};

// ========== INICIALIZAÇÃO GLOBAL ==========
document.addEventListener('DOMContentLoaded', function() {
    console.log('Na Loja Tem - Scripts globais carregados');

    // ========== MENU MOBILE ==========
    const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
    const navMenu = document.getElementById('nav-menu');

    if (mobileMenuBtn && navMenu) {
        mobileMenuBtn.addEventListener('click', function() {
            navMenu.classList.toggle('show');
            const icon = this.querySelector('i');
            if (icon) {
                icon.classList.toggle('fa-bars');
                icon.classList.toggle('fa-times');
            }
        });
    }

    // ========== BARRA DE PESQUISA ==========
    const searchButton = document.querySelector('.search-bar button');
    const searchInput = document.querySelector('.search-bar input');

    if (searchButton && searchInput) {
        searchButton.addEventListener('click', function() {
            performSearch();
        });

        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    }

    function performSearch() {
        const searchTerm = searchInput ? searchInput.value.trim() : '';
        if (searchTerm !== '') {
            window.location.href = `/busca-resultado?q=${encodeURIComponent(searchTerm)}`;
        }
    }

    // ========== INICIALIZAÇÃO DAS MÁSCARAS ==========
    const cpfInput = document.getElementById('cpf');
    if (cpfInput) aplicarMascaraCPF(cpfInput);

    const telefoneInput = document.getElementById('telefone');
    if (telefoneInput) aplicarMascaraTelefone(telefoneInput);

    const cepInput = document.getElementById('cep');
    if (cepInput) aplicarMascaraCEP(cepInput);

    const cnpjInput = document.getElementById('cnpj');
    if (cnpjInput) aplicarMascaraCNPJ(cnpjInput);

    // ========== CARREGAR CONTAGEM DO CARRINHO ==========
    loadCartCount();

    // ========== VERIFICAR STATUS DE LOGIN ==========
    function checkLoginStatus() {
        const token = localStorage.getItem('jwtToken');
        const userActionsContainer = document.getElementById('user-actions-container');

        if (userActionsContainer) {
            // Remove todos os links de usuário existentes (exceto carrinho)
            const userLinks = userActionsContainer.querySelectorAll('a:not([href*="carrinho"])');
            userLinks.forEach(link => link.remove());

            if (token) {
                try {
                    const usuario = JSON.parse(localStorage.getItem('usuario') || '{}');

                    // Adiciona links para usuário logado
                    const minhaContaLink = document.createElement('a');
                    minhaContaLink.href = '/minha-loja';
                    minhaContaLink.innerHTML = '<i class="fas fa-user-circle"></i> Minha Conta';
                    userActionsContainer.appendChild(minhaContaLink);

                    const logoutLink = document.createElement('a');
                    logoutLink.href = '#';
                    logoutLink.id = 'logout-btn';
                    logoutLink.innerHTML = '<i class="fas fa-sign-out-alt"></i> Sair';
                    logoutLink.onclick = function(e) {
                        e.preventDefault();
                        fazerLogout();
                    };
                    userActionsContainer.appendChild(logoutLink);
                } catch (e) {
                    console.error('Erro ao processar dados do usuário:', e);
                    // Fallback para não logado
                    addLoginLink(userActionsContainer);
                }
            } else {
                addLoginLink(userActionsContainer);
            }
        }
    }

    function addLoginLink(container) {
        const loginLink = document.createElement('a');
        loginLink.href = '/login';
        loginLink.id = 'login-btn';
        loginLink.innerHTML = '<i class="fas fa-user"></i> Entrar';
        container.appendChild(loginLink);
    }

    checkLoginStatus();

    // ========== ADICIONAR AO CARRINHO (global) ==========
    document.addEventListener('click', function(e) {
        const button = e.target.closest('.btn-comprar, .btn-buy-sm');
        if (button) {
            e.preventDefault();
            const produtoId = button.getAttribute('data-produto-id');

            if (!produtoId) {
                console.error("ID do produto não encontrado.");
                showNotification('Erro: Produto inválido.', 'error');
                return;
            }

            addToCart(produtoId);
        }
    });

    function addToCart(produtoId) {
        fetch('/api/carrinho/itens', {
            method: 'POST',
            headers: getAuthHeader(),
            body: JSON.stringify({ produtoId: produtoId, quantidade: 1 })
        })
        .then(response => {
            if (response.status === 401 || response.status === 403) {
                showNotification('Você precisa estar logado para adicionar itens ao carrinho.', 'error');
                setTimeout(() => {
                    window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
                }, 2000);
                return;
            }
            if (!response.ok) {
                throw new Error('Erro ao adicionar produto ao carrinho.');
            }
            return response.json();
        })
        .then(data => {
            showNotification('Produto adicionado ao carrinho!', 'success');
            loadCartCount();
        })
        .catch(error => {
            console.error('Erro:', error);
            showNotification('Ocorreu um erro ao adicionar o produto.', 'error');
        });
    }

    // ========== FAVORITOS (global) ==========
    document.addEventListener('click', function(e) {
        const button = e.target.closest('.btn-favorito, .btn-fav-sm');
        if (button) {
            e.preventDefault();
            toggleFavorite(button);
        }
    });

    function toggleFavorite(button) {
        const isActive = button.classList.contains('ativo');
        const icon = button.querySelector('i');

        // Alternar visualmente
        button.classList.toggle('ativo');
        if (icon) {
            if (button.classList.contains('ativo')) {
                icon.classList.remove('far');
                icon.classList.add('fas');
            } else {
                icon.classList.remove('fas');
                icon.classList.add('far');
            }
        }

        // TODO: Integrar com API de favoritos
        const produtoId = button.getAttribute('data-produto-id');
        if (produtoId) {
            const action = isActive ? 'remover' : 'adicionar';
            fetch(`/api/favoritos/${action}/${produtoId}`, {
                method: isActive ? 'DELETE' : 'POST',
                headers: getAuthHeader()
            })
            .then(response => {
                if (response.ok) {
                    showNotification(
                        isActive ? 'Produto removido dos favoritos' : 'Produto adicionado aos favoritos',
                        'success'
                    );
                }
            })
            .catch(error => {
                console.error('Erro ao atualizar favoritos:', error);
                // Reverter visualmente se der erro
                button.classList.toggle('ativo');
                if (icon) {
                    icon.classList.toggle('fas');
                    icon.classList.toggle('far');
                }
            });
        }
    }

    console.log('Scripts globais inicializados com sucesso');
});