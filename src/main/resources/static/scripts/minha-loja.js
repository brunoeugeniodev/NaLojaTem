// ========== MINHA LOJA (Dashboard) PAGE - Scripts específicos ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('Minha loja page - Script carregado');

    // ========== VERIFICAÇÃO DE LOGIN ==========
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        alert('Você precisa estar logado para acessar esta página!');
        window.location.href = '/login?redirect=' + encodeURIComponent('/minha-loja');
        return;
    }

    // ========== MENU LATERAL ==========
    const sidebarLinks = document.querySelectorAll('.sidebar-nav a');
    sidebarLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();

            // Atualizar link ativo
            sidebarLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');

            // Carregar conteúdo da seção
            const section = this.getAttribute('href').substring(1);
            loadSectionContent(section);
        });
    });

    // ========== CARREGAR CONTEÚDO DA SEÇÃO ==========
    function loadSectionContent(section) {
        const dashboardContent = document.querySelector('.dashboard-content');
        if (!dashboardContent) return;

        // Mostrar loading
        dashboardContent.innerHTML = '<div class="loading"><div class="loading-spinner"></div></div>';

        // Simula carregamento (em produção, faria requisição AJAX)
        setTimeout(() => {
            let contentHTML = '';

            switch(section) {
                case 'dashboard':
                    contentHTML = getDashboardContent();
                    break;
                case 'pedidos':
                    contentHTML = getPedidosContent();
                    break;
                case 'produtos':
                    contentHTML = getProdutosContent();
                    break;
                case 'loja':
                    contentHTML = getLojaContent();
                    break;
                case 'vendas':
                    contentHTML = getVendasContent();
                    break;
                case 'mensagens':
                    contentHTML = getMensagensContent();
                    break;
                case 'configuracoes':
                    contentHTML = getConfiguracoesContent();
                    break;
                default:
                    contentHTML = getDashboardContent();
            }

            dashboardContent.innerHTML = contentHTML;

            // Adicionar eventos aos novos elementos
            addDashboardEvents();
        }, 500);
    }

    // ========== CONTEÚDOS DAS SEÇÕES ==========
    function getDashboardContent() {
        return `
            <div class="dashboard-header">
                <h1><i class="fas fa-tachometer-alt"></i> Dashboard</h1>
                <div>Última atualização: ${new Date().toLocaleDateString('pt-BR')}</div>
            </div>

            <div class="stats-grid">
                <div class="stat-card-dash">
                    <div class="stat-icon"><i class="fas fa-shopping-cart"></i></div>
                    <div class="stat-number-dash" id="pedidos-hoje">0</div>
                    <div class="stat-label-dash">Pedidos Hoje</div>
                </div>

                <div class="stat-card-dash">
                    <div class="stat-icon"><i class="fas fa-dollar-sign"></i></div>
                    <div class="stat-number-dash" id="vendas-dia">R$ 0</div>
                    <div class="stat-label-dash">Vendas do Dia</div>
                </div>

                <div class="stat-card-dash">
                    <div class="stat-icon"><i class="fas fa-eye"></i></div>
                    <div class="stat-number-dash" id="visualizacoes">0</div>
                    <div class="stat-label-dash">Visualizações</div>
                </div>

                <div class="stat-card-dash">
                    <div class="stat-icon"><i class="fas fa-heart"></i></div>
                    <div class="stat-number-dash" id="seguidores">0</div>
                    <div class="stat-label-dash">Novos Seguidores</div>
                </div>
            </div>

            <section class="recent-orders">
                <h2 class="section-title-dash">Pedidos Recentes</h2>
                <div class="table-responsive">
                    <table class="orders-table">
                        <thead>
                            <tr>
                                <th>ID Pedido</th>
                                <th>Cliente</th>
                                <th>Data</th>
                                <th>Valor</th>
                                <th>Status</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody id="pedidos-recentes">
                            <!-- Pedidos serão carregados via JavaScript -->
                        </tbody>
                    </table>
                </div>
            </section>

            <section class="store-info-section">
                <h2 class="section-title-dash">Informações da Loja</h2>
                <div class="info-grid" id="info-loja">
                    <!-- Informações da loja serão carregadas via JavaScript -->
                </div>
                <button class="btn-edit-store" id="btn-editar-loja">
                    <i class="fas fa-edit"></i> Editar Loja
                </button>
            </section>
        `;
    }

    function getPedidosContent() {
        return `
            <div class="dashboard-header">
                <h1><i class="fas fa-shopping-bag"></i> Pedidos</h1>
                <button class="btn btn-primary" id="btn-exportar-pedidos">
                    <i class="fas fa-download"></i> Exportar
                </button>
            </div>

            <div class="filters" style="margin-bottom: 30px;">
                <div class="filter-group">
                    <label for="filtro-status"><i class="fas fa-filter"></i> Status</label>
                    <select id="filtro-status">
                        <option value="">Todos</option>
                        <option value="pendente">Pendente</option>
                        <option value="processando">Processando</option>
                        <option value="enviado">Enviado</option>
                        <option value="entregue">Entregue</option>
                        <option value="cancelado">Cancelado</option>
                    </select>
                </div>

                <div class="filter-group">
                    <label for="filtro-data"><i class="fas fa-calendar"></i> Período</label>
                    <input type="date" id="filtro-data">
                </div>

                <button class="btn-filter">Filtrar</button>
            </div>

            <div class="table-responsive">
                <table class="orders-table">
                    <thead>
                        <tr>
                            <th>ID Pedido</th>
                            <th>Cliente</th>
                            <th>Data</th>
                            <th>Valor</th>
                            <th>Status</th>
                            <th>Ações</th>
                        </tr>
                    </thead>
                    <tbody id="lista-pedidos">
                        <!-- Lista de pedidos será carregada via JavaScript -->
                    </tbody>
                </table>
            </div>

            <div class="pagination" style="margin-top: 30px;">
                <a href="#" class="active">1</a>
                <a href="#">2</a>
                <a href="#">3</a>
                <a href="#">4</a>
                <a href="#">5</a>
                <a href="#"><i class="fas fa-chevron-right"></i></a>
            </div>
        `;
    }

    // Funções para outras seções (resumidas)
    function getProdutosContent() {
        return `<h1>Produtos</h1><p>Conteúdo dos produtos...</p>`;
    }

    function getLojaContent() {
        return `<h1>Minha Loja</h1><p>Conteúdo da loja...</p>`;
    }

    function getVendasContent() {
        return `<h1>Vendas</h1><p>Conteúdo das vendas...</p>`;
    }

    function getMensagensContent() {
        return `<h1>Mensagens</h1><p>Conteúdo das mensagens...</p>`;
    }

    function getConfiguracoesContent() {
        return `<h1>Configurações</h1><p>Conteúdo das configurações...</p>`;
    }

    // ========== CARREGAR DADOS DO DASHBOARD ==========
    function loadDashboardData() {
        // Carregar estatísticas
        fetch('/api/dashboard/estatisticas', {
            method: 'GET',
            headers: getAuthHeader()
        })
        .then(response => response.json())
        .then(data => {
            // Atualizar estatísticas
            document.getElementById('pedidos-hoje').textContent = data.pedidosHoje || 0;
            document.getElementById('vendas-dia').textContent = `R$ ${(data.vendasDia || 0).toFixed(2)}`;
            document.getElementById('visualizacoes').textContent = data.visualizacoes || 0;
            document.getElementById('seguidores').textContent = data.novosSeguidores || 0;
        })
        .catch(error => console.error('Erro ao carregar estatísticas:', error));

        // Carregar pedidos recentes
        fetch('/api/dashboard/pedidos-recentes', {
            method: 'GET',
            headers: getAuthHeader()
        })
        .then(response => response.json())
        .then(data => {
            renderPedidosRecentes(data);
        })
        .catch(error => console.error('Erro ao carregar pedidos recentes:', error));

        // Carregar informações da loja
        fetch('/api/minha-loja/informacoes', {
            method: 'GET',
            headers: getAuthHeader()
        })
        .then(response => response.json())
        .then(data => {
            renderInfoLoja(data);
        })
        .catch(error => console.error('Erro ao carregar informações da loja:', error));
    }

    // ========== RENDERIZAR PEDIDOS RECENTES ==========
    function renderPedidosRecentes(pedidos) {
        const tbody = document.getElementById('pedidos-recentes');
        if (!tbody || !pedidos) return;

        tbody.innerHTML = '';

        pedidos.forEach(pedido => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>#${pedido.id}</td>
                <td>${pedido.clienteNome}</td>
                <td>${new Date(pedido.data).toLocaleDateString('pt-BR')}</td>
                <td>R$ ${pedido.valor.toFixed(2)}</td>
                <td><span class="status-badge status-${pedido.status}">${getStatusText(pedido.status)}</span></td>
                <td>
                    <button class="btn-action btn-view" data-pedido-id="${pedido.id}">
                        <i class="fas fa-eye"></i> Ver
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    }

    // ========== RENDERIZAR INFORMAÇÕES DA LOJA ==========
    function renderInfoLoja(loja) {
        const infoGrid = document.getElementById('info-loja');
        if (!infoGrid || !loja) return;

        infoGrid.innerHTML = `
            <div class="info-item">
                <div class="info-label">Nome da Loja:</div>
                <div class="info-value">${loja.nome || 'Não cadastrada'}</div>
            </div>
            <div class="info-item">
                <div class="info-label">CNPJ:</div>
                <div class="info-value">${loja.cnpj || 'Não cadastrado'}</div>
            </div>
            <div class="info-item">
                <div class="info-label">Status:</div>
                <div class="info-value">
                    <span class="status-badge ${loja.ativa ? 'status-completed' : 'status-pending'}">
                        ${loja.ativa ? 'Ativa' : 'Inativa'}
                    </span>
                </div>
            </div>
            <div class="info-item">
                <div class="info-label">Produtos Cadastrados:</div>
                <div class="info-value">${loja.totalProdutos || 0}</div>
            </div>
            <div class="info-item">
                <div class="info-label">Vendas Totais:</div>
                <div class="info-value">${loja.totalVendas || 0}</div>
            </div>
            <div class="info-item">
                <div class="info-label">Avaliação Média:</div>
                <div class="info-value">${loja.avaliacaoMedia || '0.0'} ⭐</div>
            </div>
        `;
    }

    // ========== FUNÇÕES AUXILIARES ==========
    function getStatusText(status) {
        const statusMap = {
            'pending': 'Pendente',
            'processing': 'Processando',
            'shipped': 'Enviado',
            'delivered': 'Entregue',
            'cancelled': 'Cancelado'
        };
        return statusMap[status] || status;
    }

    // ========== ADICIONAR EVENTOS DO DASHBOARD ==========
    function addDashboardEvents() {
        // Botão editar loja
        const btnEditarLoja = document.getElementById('btn-editar-loja');
        if (btnEditarLoja) {
            btnEditarLoja.addEventListener('click', function() {
                window.location.href = '/cadastro-loja?editar=true';
            });
        }

        // Botões ver pedido
        document.querySelectorAll('.btn-view').forEach(button => {
            button.addEventListener('click', function() {
                const pedidoId = this.getAttribute('data-pedido-id');
                window.location.href = `/pedido/${pedidoId}`;
            });
        });

        // Botão exportar pedidos
        const btnExportar = document.getElementById('btn-exportar-pedidos');
        if (btnExportar) {
            btnExportar.addEventListener('click', function() {
                showNotification('Exportando pedidos...', 'info');
                // TODO: Implementar exportação
            });
        }

        // Filtros de pedidos
        const btnFiltrarPedidos = document.querySelector('#btn-filtrar-pedidos');
        if (btnFiltrarPedidos) {
            btnFiltrarPedidos.addEventListener('click', function() {
                aplicarFiltrosPedidos();
            });
        }
    }

    function aplicarFiltrosPedidos() {
        const status = document.getElementById('filtro-status').value;
        const data = document.getElementById('filtro-data').value;

        showNotification('Aplicando filtros...', 'info');
        // TODO: Implementar filtragem real
    }

    // ========== INICIALIZAÇÃO ==========
    // Carregar dados do dashboard inicial
    if (document.querySelector('.dashboard-content')) {
        loadDashboardData();
        addDashboardEvents();
    }

    console.log('Minha loja page scripts inicializados');
});