// ========== LOJAS PAGE - Scripts específicos ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('Lojas page - Script carregado');

    // ========== FILTROS ==========
    const btnFiltrar = document.getElementById('btn-filtrar');
    if (btnFiltrar) {
        btnFiltrar.addEventListener('click', function() {
            aplicarFiltros();
        });
    }

    function aplicarFiltros() {
        const categoria = document.getElementById('categoria').value;
        const localizacao = document.getElementById('localizacao').value.trim();
        const ordenacao = document.getElementById('ordenacao').value;

        // Simula filtragem (em produção, faria uma requisição AJAX)
        showNotification('Filtros aplicados!', 'success');

        // TODO: Implementar lógica real de filtragem
        console.log('Filtros:', { categoria, localizacao, ordenacao });
    }

    // ========== PAGINAÇÃO ==========
    const paginationLinks = document.querySelectorAll('.pagination a');
    paginationLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();

            // Remove active de todos os links
            paginationLinks.forEach(l => l.classList.remove('active'));

            // Adiciona active ao link clicado
            this.classList.add('active');

            // Simula carregamento de página (em produção, faria AJAX)
            showNotification('Carregando página...', 'info');

            // TODO: Implementar carregamento real da página
            console.log('Navegando para página:', this.textContent);
        });
    });

    // ========== VISITAR LOJA ==========
    const linksVisitarLoja = document.querySelectorAll('.btn-visitar');
    linksVisitarLoja.forEach(link => {
        link.addEventListener('click', function(e) {
            // Navegação normal já está configurada no href
            console.log('Visitando loja:', this.href);
        });
    });

    // ========== CADASTRAR LOJA ==========
    const btnCadastrarLoja = document.querySelector('.btn-primary[href*="cadastro-loja"]');
    if (btnCadastrarLoja) {
        btnCadastrarLoja.addEventListener('click', function(e) {
            const token = localStorage.getItem('jwtToken');
            if (!token) {
                e.preventDefault();
                showNotification('Você precisa estar logado para cadastrar uma loja!', 'error');
                setTimeout(() => {
                    window.location.href = '/login?redirect=' + encodeURIComponent('/cadastro-loja');
                }, 1500);
            }
        });
    }

    console.log('Lojas page scripts inicializados');
});