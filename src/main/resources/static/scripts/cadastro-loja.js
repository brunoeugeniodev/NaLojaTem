// ========== CADASTRO LOJA PAGE - Scripts específicos ==========

document.addEventListener('DOMContentLoaded', function() {
    console.log('Cadastro loja page - Script carregado');

    // ========== VERIFICAÇÃO DE LOGIN ==========
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        alert('Você precisa estar logado para cadastrar uma loja!');
        window.location.href = '/login?redirect=' + encodeURIComponent('/cadastro-loja');
        return;
    }

    // ========== LÓGICA DO FORMULÁRIO PASSO A PASSO ==========
    const formSteps = document.querySelectorAll('.form-step');
    const stepIndicators = document.querySelectorAll('.step');
    let currentStep = 0;

    // Mostrar passo específico
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

    // Validação de passo
    function validateStep(stepIndex) {
        const currentFormStep = formSteps[stepIndex];
        const requiredInputs = currentFormStep.querySelectorAll('[required]');

        for (let input of requiredInputs) {
            if (!input.value.trim()) {
                const label = input.previousElementSibling?.textContent || 'campo obrigatório';
                showNotification(`Por favor, preencha o campo: ${label}`, 'error');
                input.focus();
                return false;
            }
        }

        // Validação específica do CNPJ
        if (stepIndex === 0) {
            const cnpjInput = document.getElementById('cnpj');
            if (cnpjInput && !validarCNPJ(cnpjInput.value)) {
                showNotification('CNPJ inválido! Por favor, verifique o número.', 'error');
                cnpjInput.focus();
                return false;
            }
        }

        return true;
    }

    // Função para validar CNPJ
    function validarCNPJ(cnpj) {
        cnpj = cnpj.replace(/[^\d]+/g, '');

        if (cnpj.length !== 14) return false;

        // Elimina CNPJs invalidos conhecidos
        if (cnpj === "00000000000000" ||
            cnpj === "11111111111111" ||
            cnpj === "22222222222222" ||
            cnpj === "33333333333333" ||
            cnpj === "44444444444444" ||
            cnpj === "55555555555555" ||
            cnpj === "66666666666666" ||
            cnpj === "77777777777777" ||
            cnpj === "88888888888888" ||
            cnpj === "99999999999999")
            return false;

        // Valida DVs
        let tamanho = cnpj.length - 2;
        let numeros = cnpj.substring(0, tamanho);
        let digitos = cnpj.substring(tamanho);
        let soma = 0;
        let pos = tamanho - 7;

        for (let i = tamanho; i >= 1; i--) {
            soma += numeros.charAt(tamanho - i) * pos--;
            if (pos < 2) pos = 9;
        }

        let resultado = soma % 11 < 2 ? 0 : 11 - soma % 11;
        if (resultado != digitos.charAt(0)) return false;

        tamanho = tamanho + 1;
        numeros = cnpj.substring(0, tamanho);
        soma = 0;
        pos = tamanho - 7;

        for (let i = tamanho; i >= 1; i--) {
            soma += numeros.charAt(tamanho - i) * pos--;
            if (pos < 2) pos = 9;
        }

        resultado = soma % 11 < 2 ? 0 : 11 - soma % 11;
        if (resultado != digitos.charAt(1)) return false;

        return true;
    }

    // Botões próximo/anterior
    document.querySelectorAll('.btn-next').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            if (validateStep(currentStep)) {
                if (currentStep < formSteps.length - 1) {
                    showStep(currentStep + 1);
                }
            }
        });
    });

    document.querySelectorAll('.btn-prev').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            if (currentStep > 0) {
                showStep(currentStep - 1);
            }
        });
    });

    // ========== UPLOAD DE IMAGEM ==========
    const fileUploads = document.querySelectorAll('.file-upload');
    fileUploads.forEach(upload => {
        const input = upload.querySelector('input[type="file"]');
        const preview = upload.querySelector('.preview-image');

        if (upload && input) {
            upload.addEventListener('click', () => input.click());

            input.addEventListener('change', function(e) {
                const file = e.target.files[0];
                if (file) {
                    // Validação do arquivo
                    const validTypes = ['image/jpeg', 'image/png', 'image/jpg'];
                    const maxSize = 5 * 1024 * 1024; // 5MB

                    if (!validTypes.includes(file.type)) {
                        showNotification('Formato de arquivo inválido. Use JPG ou PNG.', 'error');
                        return;
                    }

                    if (file.size > maxSize) {
                        showNotification('Arquivo muito grande. Máximo 5MB.', 'error');
                        return;
                    }

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

    // ========== SUBMIT DO FORMULÁRIO ==========
    const storeForm = document.getElementById('store-form');
    if (storeForm) {
        storeForm.addEventListener('submit', function(e) {
            e.preventDefault();

            if (!validateStep(currentStep)) {
                showNotification('Por favor, preencha todos os campos obrigatórios', 'error');
                return;
            }

            const button = this.querySelector('.btn-submit');
            const originalText = button.innerHTML;
            button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Cadastrando...';
            button.disabled = true;

            // Coleta dados do formulário
            const formData = new FormData();
            formData.append('nome', document.getElementById('nome-loja').value);
            formData.append('cnpj', document.getElementById('cnpj').value.replace(/\D/g, ''));
            formData.append('descricao', document.getElementById('descricao').value);

            // Endereço
            formData.append('endereco.rua', document.getElementById('rua').value);
            formData.append('endereco.numero', document.getElementById('numero').value);
            formData.append('endereco.bairro', document.getElementById('bairro').value);
            formData.append('endereco.cidade', document.getElementById('cidade').value);
            formData.append('endereco.estado', document.getElementById('estado').value);

            // Foto da loja
            const fotoFile = document.getElementById('logo-loja').files[0];
            if (fotoFile) {
                formData.append('foto', fotoFile);
            }

            // Envia para API
            fetch('/api/lojas/cadastrar', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(data => {
                        throw new Error(data.message || 'Erro ao cadastrar loja');
                    });
                }
                return response.json();
            })
            .then(data => {
                showNotification('Loja cadastrada com sucesso!', 'success');

                setTimeout(() => {
                    window.location.href = '/minha-loja';
                }, 2000);
            })
            .catch(error => {
                showNotification('Erro ao cadastrar loja: ' + error.message, 'error');
                button.innerHTML = originalText;
                button.disabled = false;
            });
        });
    }

    // ========== BUSCAR CEP AUTOMATICAMENTE ==========
    const cepInput = document.getElementById('cep');
    if (cepInput) {
        cepInput.addEventListener('blur', function() {
            const cep = this.value.replace(/\D/g, '');
            if (cep.length === 8) {
                buscarEnderecoPorCEP(cep);
            }
        });
    }

    console.log('Cadastro loja page scripts inicializados');
});