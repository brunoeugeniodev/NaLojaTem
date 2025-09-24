document.addEventListener('DOMContentLoaded', function() {
    // Menu mobile
    const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
    const navMenu = document.getElementById('nav-menu');
    if (mobileMenuBtn && navMenu) {
        mobileMenuBtn.addEventListener('click', function() {
            navMenu.classList.toggle('show');
        });
    }

    // Barra de pesquisa
    const searchButton = document.querySelector('.search-bar button');
    const searchInput = document.querySelector('.search-bar input');
    if (searchButton && searchInput) {
        searchButton.addEventListener('click', function() {
            const searchTerm = searchInput.value;
            if (searchTerm.trim() !== '') {
                alert('Buscando por: ' + searchTerm);
            }
        });
    }

    // Carrossel de produtos
    const carrossel = document.querySelector('.carrossel');
    const produtos = document.querySelectorAll('.produto-card');
    const btnPrev = document.querySelector('.carrossel-btn.prev');
    const btnNext = document.querySelector('.carrossel-btn.next');

    let currentIndex = 0;
    let produtosVisiveis = getProdutosVisiveis();

    function getProdutosVisiveis() {
        return window.innerWidth < 768 ? 1 : 3;
    }

    function updateCarrossel() {
        if (produtos.length === 0) return;
        const produtoWidth = produtos[0].offsetWidth + (parseFloat(window.getComputedStyle(produtos[0]).marginLeft) * 2);
        carrossel.style.transform = `translateX(-${currentIndex * produtoWidth}px)`;
    }

    if (btnNext && btnPrev && carrossel) {
        btnNext.addEventListener('click', function() {
            if (currentIndex < produtos.length - produtosVisiveis) {
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

        updateCarrossel();
    }


    // Botões de favorito
    const botoesFavorito = document.querySelectorAll('.btn-favorito');
    botoesFavorito.forEach(botao => {
        botao.addEventListener('click', function() {
            this.classList.toggle('ativo');
            const icone = this.querySelector('i');
            if (icone) {
                if (this.classList.contains('ativo')) {
                    icone.classList.remove('far');
                    icone.classList.add('fas');
                } else {
                    icone.classList.remove('fas');
                    icone.classList.add('far');
                }
            }
        });
    });

    // Botão criar loja
    const btnCriarLoja = document.getElementById('btn-criar-loja');
    if (btnCriarLoja) {
        btnCriarLoja.addEventListener('click', function() {
            alert('Redirecionando para a página de cadastro de loja...');
            // window.location.href = "/cadastro-loja.html";
        });
    }

});