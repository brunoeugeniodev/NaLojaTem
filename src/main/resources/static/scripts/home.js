// ========== HOME PAGE - Scripts específicos ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('Home page - Script carregado');

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

    // ========== BOTÃO CRIAR LOJA ==========
    const btnCriarLoja = document.getElementById('btn-criar-loja');
    if (btnCriarLoja) {
        btnCriarLoja.addEventListener('click', function() {
            const token = localStorage.getItem('jwtToken');
            if (!token) {
                showNotification('Você precisa estar logado para cadastrar uma loja!', 'error');
                setTimeout(() => {
                    window.location.href = '/login?redirect=' + encodeURIComponent('/cadastro-loja');
                }, 1500);
            } else {
                window.location.href = '/cadastro-loja';
            }
        });
    }

    // ========== EXPLORAR LOJAS ==========
    const btnExplorarLojas = document.querySelector('.btn-primary[href*="lojas"]');
    if (btnExplorarLojas) {
        btnExplorarLojas.addEventListener('click', function(e) {
            e.preventDefault();
            window.location.href = '/lojas';
        });
    }

    console.log('Home page scripts inicializados');
});