// Menu mobile
document.querySelector('.mobile-menu-btn').addEventListener('click', function() {
    document.getElementById('nav-menu').classList.toggle('show');
});

// Barra de pesquisa
document.querySelector('.search-bar button').addEventListener('click', function() {
    const searchTerm = document.querySelector('.search-bar input').value;
    if (searchTerm.trim() !== '') {
        alert('Buscando por: ' + searchTerm);
    }
});
