// ========== LOJA INDIVIDUAL PAGE - Scripts específicos ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('Loja individual page - Script carregado');

    // ========== NAVEGAÇÃO DA LOJA ==========
    const storeNavLinks = document.querySelectorAll('.store-nav a');
    if (storeNavLinks.length > 0) {
        storeNavLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();

                // Atualizar link ativo
                storeNavLinks.forEach(l => l.classList.remove('active'));
                this.classList.add('active');

                const targetId = this.getAttribute('href').substring(1);
                const targetElement = document.getElementById(targetId);

                if (targetElement) {
                    // Scroll suave
                    window.scrollTo({
                        top: targetElement.offsetTop - 100,
                        behavior: 'smooth'
                    });
                }
            });
        });
    }

    // ========== SEGUIR LOJA ==========
    const btnFollow = document.querySelector('.btn-follow');
    if (btnFollow) {
        btnFollow.addEventListener('click', function() {
            const isFollowing = this.classList.contains('seguindo');
            const icon = this.querySelector('i');

            if (isFollowing) {
                this.classList.remove('seguindo');
                this.innerHTML = '<i class="far fa-heart"></i> Seguir Loja';
                showNotification('Loja removida dos favoritos', 'info');
            } else {
                this.classList.add('seguindo');
                this.innerHTML = '<i class="fas fa-heart"></i> Seguindo';
                showNotification('Loja adicionada aos favoritos', 'success');
            }

            // TODO: Integrar com API para seguir/parar de seguir
            const lojaId = window.location.pathname.split('/').pop();
            const action = isFollowing ? 'unfollow' : 'follow';

            fetch(`/api/lojas/${lojaId}/${action}`, {
                method: 'POST',
                headers: getAuthHeader()
            })
            .catch(error => {
                console.error('Erro ao seguir loja:', error);
                // Reverter visualmente se der erro
                if (isFollowing) {
                    this.classList.add('seguindo');
                    this.innerHTML = '<i class="fas fa-heart"></i> Seguindo';
                } else {
                    this.classList.remove('seguindo');
                    this.innerHTML = '<i class="far fa-heart"></i> Seguir Loja';
                }
            });
        });
    }

    // ========== CONTATAR LOJA ==========
    const btnContact = document.querySelector('.btn-contact');
    if (btnContact) {
        btnContact.addEventListener('click', function() {
            showNotification('Em breve implementaremos o sistema de contato!', 'info');
        });
    }

    // ========== ORDENAÇÃO DE PRODUTOS ==========
    const selectOrdenacao = document.querySelector('select');
    if (selectOrdenacao) {
        selectOrdenacao.addEventListener('change', function() {
            const ordenacao = this.value;
            showNotification(`Ordenando por: ${this.options[this.selectedIndex].text}`, 'info');

            // TODO: Implementar ordenação real via AJAX
            console.log('Ordenação selecionada:', ordenacao);
        });
    }

    console.log('Loja individual page scripts inicializados');
});