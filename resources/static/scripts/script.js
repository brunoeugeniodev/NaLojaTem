document.addEventListener('DOMContentLoaded', function() {
    console.log('Na Loja Tem - Script carregado');

    // ========== FUNÇÕES GLOBAIS ==========

    // 1. Função para obter cabeçalhos de autenticação (JWT)
    function getAuthHeader() {
        const token = localStorage.getItem('jwtToken');
        return token ? {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        } : {
            'Content-Type': 'application/json'
        };
    }

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
    function loadCartCount() {
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
    }

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

    // ========== CARRINHO DE COMPRAS ==========

    // Adicionar ao carrinho
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-comprar') ||
            e.target.closest('.btn-comprar') ||
            e.target.classList.contains('btn-buy-sm')) {

            e.preventDefault();
            const button = e.target.classList.contains('btn-comprar') ||
                          e.target.classList.contains('btn-buy-sm') ?
                          e.target : e.target.closest('.btn-comprar, .btn-buy-sm');

            const produtoId = button.getAttribute('data-produto-id');
            const quantidade = 1;

            if (!produtoId) {
                console.error("ID do produto não encontrado.");
                showNotification('Erro: Produto inválido.', 'error');
                return;
            }

            fetch('/api/carrinho/itens', {
                method: 'POST',
                headers: getAuthHeader()
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
    });

    // ========== FAVORITOS ==========
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-favorito') ||
            e.target.closest('.btn-favorito') ||
            e.target.classList.contains('btn-fav-sm') ||
            e.target.closest('.btn-fav-sm')) {

            e.preventDefault();
            const button = e.target.classList.contains('btn-favorito') ||
                          e.target.classList.contains('btn-fav-sm') ?
                          e.target : e.target.closest('.btn-favorito, .btn-fav-sm');

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
    });

    // ========== CARROSSEL ==========
    const carrossel = document.querySelector('.carrossel');
    const produtos = document.querySelectorAll('.produto-card');
    const btnPrev = document.querySelector('.carrossel-btn.prev');
    const btnNext = document.querySelector('.carrossel-btn.next');

    if (carrossel && produtos.length > 0) {
        let currentIndex = 0;
        let produtosVisiveis = getProdutosVisiveis();

        function getProdutosVisiveis() {
            if (window.innerWidth < 768) return 1;
            if (window.innerWidth < 992) return 2;
            return 3;
        }

        function updateCarrossel() {
            if (produtos.length === 0) return;

            const produtoWidth = produtos[0].offsetWidth +
                (parseFloat(window.getComputedStyle(produtos[0]).marginLeft) || 0) +
                (parseFloat(window.getComputedStyle(produtos[0]).marginRight) || 0);

            const maxIndex = Math.max(0, produtos.length - produtosVisiveis);
            if (currentIndex > maxIndex) {
                currentIndex = maxIndex;
            }

            carrossel.style.transform = `translateX(-${currentIndex * produtoWidth}px)`;
        }

        if (btnNext && btnPrev) {
            btnNext.addEventListener('click', function() {
                const maxIndex = Math.max(0, produtos.length - produtosVisiveis);
                if (currentIndex < maxIndex) {
                    currentIndex++;
                    updateCarrossel();
                }
            });

            btnPrev.addEventListener('click', function() {
                if (currentIndex > 0) {
                    currentIndex--;
                    updateCarrossel();
                }
            });

            window.addEventListener('resize', function() {
                const novosProdutosVisiveis = getProdutosVisiveis();
                if (novosProdutosVisiveis !== produtosVisiveis) {
                    produtosVisiveis = novosProdutosVisiveis;
                    currentIndex = 0;
                    updateCarrossel();
                }
            });

            // Auto-rotacionar carrossel (só se tiver mais de 1 produto)
            if (produtos.length > 1) {
                setInterval(() => {
                    if (document.visibilityState === 'visible') {
                        const maxIndex = Math.max(0, produtos.length - produtosVisiveis);
                        currentIndex = (currentIndex + 1) % (maxIndex + 1);
                        updateCarrossel();
                    }
                }, 5000);
            }

            // Inicializar
            setTimeout(updateCarrossel, 100);
        }
    }

    // ========== FORMULÁRIOS ==========

    // Formulário de cadastro de loja (passo a passo)
    const formSteps = document.querySelectorAll('.form-step');
    const stepIndicators = document.querySelectorAll('.step');

    if (formSteps.length > 0) {
        let currentStep = 0;

        function showStep(stepIndex) {
            formSteps.forEach((step, index) => {
                step.classList.toggle('active', index === stepIndex);
            });

            stepIndicators.forEach((indicator, index) => {
                indicator.classList.remove('active', 'completed');
                if (index === stepIndex) {
                    indicator.classList.add('active');
                } else if (index < stepIndex) {
                    indicator.classList.add('completed');
                }
            });

            currentStep = stepIndex;
        }

        // Botões próximo/anterior
        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('btn-next')) {
                e.preventDefault();
                if (currentStep < formSteps.length - 1) {
                    showStep(currentStep + 1);
                }
            }

            if (e.target.classList.contains('btn-prev')) {
                e.preventDefault();
                if (currentStep > 0) {
                    showStep(currentStep - 1);
                }
            }
        });

        // Upload de imagem
        const fileUploads = document.querySelectorAll('.file-upload');
        fileUploads.forEach(upload => {
            const input = upload.querySelector('input[type="file"]');
            const preview = upload.querySelector('.preview-image');

            if (upload && input) {
                upload.addEventListener('click', () => input.click());

                input.addEventListener('change', function(e) {
                    const file = e.target.files[0];
                    if (file) {
                        const reader = new FileReader();
                        reader.onload = function(e) {
                            if (preview) {
                                preview.src = e.target.result;
                                preview.style.display = 'block';
                            }
                            const icon = upload.querySelector('i');
                            const text = upload.querySelector('p');
                            if (icon) icon.style.display = 'none';
                            if (text) text.style.display = 'none';
                        };
                        reader.readAsDataURL(file);
                    }
                });
            }
        });
    }

    // Validação de formulários
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // Validação básica de senhas iguais
            const password = form.querySelector('input[name="senha"]');
            const confirmPassword = form.querySelector('input[name="confirmarSenha"]');

            if (password && confirmPassword && password.value !== confirmPassword.value) {
                e.preventDefault();
                showNotification('As senhas não coincidem!', 'error');
                if (confirmPassword) confirmPassword.focus();
                return false;
            }

            // Validação de termos
            const termsCheckbox = form.querySelector('input[name="aceitar-termos"]');
            if (termsCheckbox && !termsCheckbox.checked) {
                e.preventDefault();
                showNotification('Você precisa aceitar os termos e condições.', 'error');
                return false;
            }

            // Mostrar loading
            const submitButton = form.querySelector('button[type="submit"]');
            if (submitButton) {
                const originalText = submitButton.innerHTML;
                submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processando...';
                submitButton.disabled = true;

                setTimeout(() => {
                    submitButton.innerHTML = originalText;
                    submitButton.disabled = false;
                }, 3000);
            }

            return true;
        });
    });

    // ========== QUANTIDADE NO CARRINHO ==========
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('quantity-btn')) {
            const cartItemQuantity = e.target.closest('.cart-item-quantity');
            if (cartItemQuantity) {
                const input = cartItemQuantity.querySelector('.quantity-input');
                if (input) {
                    let value = parseInt(input.value) || 0;

                    if (e.target.classList.contains('quantity-minus')) {
                        value = Math.max(1, value - 1);
                    } else if (e.target.classList.contains('quantity-plus')) {
                        value += 1;
                    }

                    input.value = value;

                    // TODO: Atualizar no carrinho via API
                    const itemId = input.getAttribute('data-item-id');
                    if (itemId) {
                        fetch(`/api/carrinho/itens/${itemId}?quantidade=${value}`, {
                            method: 'PUT',
                            headers: getAuthHeader()
                        })
                        .then(response => {
                            if (response.ok) {
                                return response.json();
                            }
                            throw new Error('Erro ao atualizar quantidade');
                        })
                        .then(data => {
                            // Atualizar preço total do item
                            const cartItem = e.target.closest('.cart-item');
                            if (cartItem) {
                                const priceElement = cartItem.querySelector('.cart-item-price');
                                if (priceElement && data.precoTotal) {
                                    priceElement.textContent = `R$ ${data.precoTotal.toFixed(2)}`;
                                }
                            }
                            loadCartCount();
                        })
                        .catch(error => {
                            console.error('Erro:', error);
                            showNotification('Erro ao atualizar quantidade', 'error');
                        });
                    }
                }
            }
        }
    });

    // ========== REMOVER ITEM DO CARRINHO ==========
    document.addEventListener('click', function(e) {
        const button = e.target.classList.contains('btn-remove') ?
                      e.target : e.target.closest('.btn-remove');

        if (button) {
            e.preventDefault();
            const itemId = button.getAttribute('data-item-id');
            if (itemId && confirm('Tem certeza que deseja remover este item do carrinho?')) {
                fetch(`/api/carrinho/itens/${itemId}`, {
                    method: 'DELETE',
                    headers: getAuthHeader()
                })
                .then(response => {
                    if (response.ok) {
                        // Remover elemento do DOM
                        const cartItem = button.closest('.cart-item');
                        if (cartItem) {
                            cartItem.remove();
                        }
                        showNotification('Item removido do carrinho', 'success');
                        loadCartCount();

                        // Atualizar total do carrinho
                        updateCartTotal();
                    } else {
                        throw new Error('Erro ao remover item');
                    }
                })
                .catch(error => {
                    console.error('Erro:', error);
                    showNotification('Erro ao remover item do carrinho', 'error');
                });
            }
        }
    });

    function updateCartTotal() {
        // TODO: Atualizar total do carrinho
    }

    // ========== NAVEGAÇÃO DA LOJA ==========
    const storeNavLinks = document.querySelectorAll('.store-nav a');
    storeNavLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetElement = document.getElementById(targetId);

            if (targetElement) {
                // Atualizar link ativo
                storeNavLinks.forEach(l => l.classList.remove('active'));
                this.classList.add('active');

                // Scroll suave
                window.scrollTo({
                    top: targetElement.offsetTop - 100,
                    behavior: 'smooth'
                });
            }
        });
    });

    // ========== INICIALIZAÇÃO ==========

    // Carregar contagem do carrinho
    loadCartCount();

    // Atualizar contagem do carrinho periodicamente
    setInterval(loadCartCount, 30000);

    // Verificar login status
    function checkLoginStatus() {
        fetch('/api/auth/check-auth', {
            headers: getAuthHeader()
        })
        .then(response => {
            if (response.ok) {
                return response.json();
            }
            throw new Error('Não autenticado');
        })
        .then(data => {
            if (data.authenticated) {
                // Usuário está logado
                console.log('Usuário autenticado:', data.username);
            }
        })
        .catch(() => {
            // Não autenticado - não faz nada
        });
    }

    // Verifica se tem token e atualiza interface
    function atualizarInterfaceLogin() {
        const token = localStorage.getItem('jwtToken');
        const userActionsContainer = document.getElementById('user-actions-container');

        if (userActionsContainer) {
            // Remove todos os links de usuário existentes (exceto carrinho)
            const userLinks = userActionsContainer.querySelectorAll('a:not([href*="carrinho"])');
            userLinks.forEach(link => link.remove());

            if (token) {
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
            } else {
                // Adiciona link para login
                const loginLink = document.createElement('a');
                loginLink.href = '/login';
                loginLink.id = 'login-btn';
                loginLink.innerHTML = '<i class="fas fa-user"></i> Entrar';
                userActionsContainer.appendChild(loginLink);
            }
        }
    }

    // Inicializa a interface de login
    atualizarInterfaceLogin();
    checkLoginStatus();

    // ========== FUNÇÕES GLOBAIS PARA O WINDOW ==========
    window.loadCartCount = loadCartCount;

    console.log('Script inicializado com sucesso');
});

// ========== FUNÇÕES EXTRAS ==========

// Máscara para CPF
function aplicarMascaraCPF(cpfInput) {
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
}

// Máscara para telefone
function aplicarMascaraTelefone(telInput) {
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
}

// Máscara para CEP
function aplicarMascaraCEP(cepInput) {
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
}

function buscarEnderecoPorCEP(cep) {
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
}

// Inicializar máscaras quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', function() {
    // Aplicar máscaras nos campos
    const cpfInput = document.getElementById('cpf');
    if (cpfInput) aplicarMascaraCPF(cpfInput);

    const telefoneInput = document.getElementById('telefone');
    if (telefoneInput) aplicarMascaraTelefone(telefoneInput);

    const cepInput = document.getElementById('cep');
    if (cepInput) aplicarMascaraCEP(cepInput);
});

// ========== AJAX HELPER ==========
function ajaxRequest(url, method = 'GET', data = null) {
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
}