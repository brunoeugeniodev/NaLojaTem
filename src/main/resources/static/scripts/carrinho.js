// ========== CARRINHO PAGE - Scripts específicos ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('Carrinho page - Script carregado');

    // ========== CARREGAR ITENS DO CARRINHO ==========
    function loadCartItems() {
        fetch('/api/carrinho/itens', {
            method: 'GET',
            headers: getAuthHeader()
        })
        .then(response => {
            if (response.status === 401 || response.status === 403) {
                showCartEmptyState();
                return;
            }
            if (!response.ok) {
                throw new Error('Erro ao carregar carrinho');
            }
            return response.json();
        })
        .then(data => {
            if (data && data.itens && data.itens.length > 0) {
                renderCartItems(data.itens);
                updateCartSummary(data.total);
            } else {
                showCartEmptyState();
            }
        })
        .catch(error => {
            console.error('Erro ao carregar carrinho:', error);
            showCartEmptyState();
        });
    }

    // ========== RENDERIZAR ITENS DO CARRINHO ==========
    function renderCartItems(items) {
        const cartItemsContainer = document.querySelector('.cart-items');
        if (!cartItemsContainer) return;

        cartItemsContainer.innerHTML = '';

        items.forEach(item => {
            const cartItemHTML = `
                <div class="cart-item" data-item-id="${item.id}">
                    <img src="${item.produto.fotoUrl || '/imagens/placeholder.png'}"
                         alt="${item.produto.nome}"
                         class="cart-item-img">

                    <div class="cart-item-info">
                        <h3>${item.produto.nome}</h3>
                        <p>${item.produto.descricao || 'Produto de qualidade'}</p>
                        <div class="cart-item-price">R$ ${item.precoUnitario.toFixed(2)}</div>

                        <div class="cart-item-quantity">
                            <button class="quantity-btn quantity-minus">-</button>
                            <input type="number"
                                   class="quantity-input"
                                   value="${item.quantidade}"
                                   min="1"
                                   max="${item.produto.quantidade}"
                                   data-item-id="${item.id}">
                            <button class="quantity-btn quantity-plus">+</button>
                        </div>
                    </div>

                    <div class="cart-item-actions">
                        <button class="btn-remove" data-item-id="${item.id}">
                            <i class="fas fa-trash"></i> Remover
                        </button>
                    </div>
                </div>
            `;

            cartItemsContainer.insertAdjacentHTML('beforeend', cartItemHTML);
        });

        // Adiciona eventos aos novos elementos
        addCartItemEvents();
    }

    // ========== MOSTRAR CARRINHO VAZIO ==========
    function showCartEmptyState() {
        const cartContent = document.querySelector('.cart-content');
        if (!cartContent) return;

        cartContent.innerHTML = `
            <div class="cart-empty">
                <i class="fas fa-shopping-cart"></i>
                <h3>Seu carrinho está vazio</h3>
                <p>Adicione produtos para ver seus itens aqui.</p>
                <a href="/" class="btn btn-primary mt-20">
                    <i class="fas fa-store"></i> Continuar Comprando
                </a>
            </div>
        `;
    }

    // ========== ATUALIZAR RESUMO DO CARRINHO ==========
    function updateCartSummary(total) {
        const subtotalElement = document.querySelector('.summary-row:nth-child(1) .value');
        const freteElement = document.querySelector('.summary-row:nth-child(2) .value');
        const totalElement = document.querySelector('.summary-row.total .value');

        if (subtotalElement) subtotalElement.textContent = `R$ ${total.toFixed(2)}`;
        if (freteElement) freteElement.textContent = 'R$ 0,00'; // Frete grátis por enquanto
        if (totalElement) totalElement.textContent = `R$ ${total.toFixed(2)}`;
    }

    // ========== ADICIONAR EVENTOS AOS ITENS DO CARRINHO ==========
    function addCartItemEvents() {
        // Botões de quantidade
        document.querySelectorAll('.quantity-btn').forEach(button => {
            button.addEventListener('click', function() {
                const input = this.closest('.cart-item-quantity').querySelector('.quantity-input');
                let value = parseInt(input.value) || 0;

                if (this.classList.contains('quantity-minus')) {
                    value = Math.max(1, value - 1);
                } else if (this.classList.contains('quantity-plus')) {
                    const max = parseInt(input.getAttribute('max')) || 99;
                    value = Math.min(max, value + 1);
                }

                input.value = value;

                // Atualizar no servidor
                const itemId = input.getAttribute('data-item-id');
                updateCartItemQuantity(itemId, value);
            });
        });

        // Input de quantidade (mudança manual)
        document.querySelectorAll('.quantity-input').forEach(input => {
            input.addEventListener('change', function() {
                const value = parseInt(this.value) || 1;
                const max = parseInt(this.getAttribute('max')) || 99;
                const min = parseInt(this.getAttribute('min')) || 1;

                const validValue = Math.max(min, Math.min(max, value));
                this.value = validValue;

                const itemId = this.getAttribute('data-item-id');
                updateCartItemQuantity(itemId, validValue);
            });
        });

        // Botões de remover
        document.querySelectorAll('.btn-remove').forEach(button => {
            button.addEventListener('click', function() {
                const itemId = this.getAttribute('data-item-id');
                removeCartItem(itemId);
            });
        });
    }

    // ========== ATUALIZAR QUANTIDADE NO SERVIDOR ==========
    function updateCartItemQuantity(itemId, quantidade) {
        fetch(`/api/carrinho/itens/${itemId}`, {
            method: 'PUT',
            headers: getAuthHeader(),
            body: JSON.stringify({ quantidade: quantidade })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Erro ao atualizar quantidade');
            }
            return response.json();
        })
        .then(data => {
            // Atualizar preço do item
            const cartItem = document.querySelector(`.cart-item[data-item-id="${itemId}"]`);
            if (cartItem) {
                const priceElement = cartItem.querySelector('.cart-item-price');
                if (priceElement && data.precoTotal) {
                    priceElement.textContent = `R$ ${data.precoTotal.toFixed(2)}`;
                }
            }

            // Atualizar resumo do carrinho
            updateCartSummary(data.totalCarrinho);
            loadCartCount();
        })
        .catch(error => {
            console.error('Erro:', error);
            showNotification('Erro ao atualizar quantidade', 'error');
        });
    }

    // ========== REMOVER ITEM DO CARRINHO ==========
    function removeCartItem(itemId) {
        if (!confirm('Tem certeza que deseja remover este item do carrinho?')) {
            return;
        }

        fetch(`/api/carrinho/itens/${itemId}`, {
            method: 'DELETE',
            headers: getAuthHeader()
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Erro ao remover item');
            }

            // Remover elemento do DOM
            const cartItem = document.querySelector(`.cart-item[data-item-id="${itemId}"]`);
            if (cartItem) {
                cartItem.remove();
            }

            showNotification('Item removido do carrinho', 'success');
            loadCartCount();

            // Verificar se ainda há itens
            const remainingItems = document.querySelectorAll('.cart-item').length;
            if (remainingItems === 0) {
                showCartEmptyState();
            } else {
                // Recalcular total (em produção, viria da resposta)
                updateCartSummary(0); // TODO: Obter total atualizado da API
            }
        })
        .catch(error => {
            console.error('Erro:', error);
            showNotification('Erro ao remover item do carrinho', 'error');
        });
    }

    // ========== FINALIZAR COMPRA ==========
    const btnFinalizarCompra = document.getElementById('btn-finalizar-compra');
    if (btnFinalizarCompra) {
        btnFinalizarCompra.addEventListener('click', function() {
            const token = localStorage.getItem('jwtToken');
            if (!token) {
                showNotification('Você precisa estar logado para finalizar a compra!', 'error');
                setTimeout(() => {
                    window.location.href = '/login?redirect=' + encodeURIComponent('/carrinho');
                }, 1500);
                return;
            }

            // Verificar se há itens no carrinho
            const cartItems = document.querySelectorAll('.cart-item').length;
            if (cartItems === 0) {
                showNotification('Seu carrinho está vazio!', 'error');
                return;
            }

            // Redirecionar para checkout
            showNotification('Redirecionando para checkout...', 'info');
            setTimeout(() => {
                window.location.href = '/checkout';
            }, 1000);
        });
    }

    // ========== CONTINUAR COMPRANDO ==========
    const btnContinuarComprando = document.querySelector('.btn[href="/"]');
    if (btnContinuarComprando) {
        btnContinuarComprando.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/';
        });
    }

    // Carregar itens do carrinho ao iniciar
    loadCartItems();

    console.log('Carrinho page scripts inicializados');
});