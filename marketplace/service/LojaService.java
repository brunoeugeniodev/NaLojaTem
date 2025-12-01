package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.models.Endereco;
import io.github.brunoeugeniodev.marketplace.models.Loja;
import io.github.brunoeugeniodev.marketplace.repository.EnderecoRepository;
import io.github.brunoeugeniodev.marketplace.repository.LojaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class LojaService {
    private final LojaRepository lojaRepository;
    private final EnderecoRepository enderecoRepository;

    public LojaService(LojaRepository lojaRepository, EnderecoRepository enderecoRepository) {
        this.lojaRepository = lojaRepository;
        this.enderecoRepository = enderecoRepository;
    }

    public Loja salvarLoja(Loja loja){
        // Validações básicas
        if (loja.getNome() == null || loja.getNome().trim().isEmpty()) {
            throw new RuntimeException("Nome da loja é obrigatório");
        }

        if (loja.getCnpj() == null || loja.getCnpj().trim().isEmpty()) {
            throw new RuntimeException("CNPJ é obrigatório");
        }

        // Verifica se CNPJ já existe
        if (lojaRepository.existsByCnpj(loja.getCnpj())) {
            throw new RuntimeException("CNPJ já cadastrado: " + loja.getCnpj());
        }

        return lojaRepository.save(loja);
    }

    public Optional<Loja> editarLoja(Long id, Loja lojaNovo){
        return lojaRepository.findById(id)
                .map(lojaExistente -> {
                    // Verifica se CNPJ foi alterado e se já existe
                    if (!lojaExistente.getCnpj().equals(lojaNovo.getCnpj()) &&
                            lojaRepository.existsByCnpj(lojaNovo.getCnpj())) {
                        throw new RuntimeException("CNPJ já está em uso: " + lojaNovo.getCnpj());
                    }

                    lojaExistente.setNome(lojaNovo.getNome());
                    lojaExistente.setCnpj(lojaNovo.getCnpj());
                    lojaExistente.setEndereco(lojaNovo.getEndereco());
                    lojaExistente.setFoto(lojaNovo.getFoto());
                    lojaExistente.setDescricao(lojaNovo.getDescricao());
                    return lojaRepository.save(lojaExistente);
                });
    }

    // Novo método para atualizar apenas a foto
    public Optional<Loja> atualizarFoto(Long id, MultipartFile foto) {
        return lojaRepository.findById(id)
                .map(loja -> {
                    try {
                        // Processa o upload da foto
                        // String fotoUrl = fileStorageService.salvarFoto(foto);
                        // loja.setFoto(fotoUrl);

                        // Para teste, use um placeholder
                        loja.setFoto("/imagens/lojas/" + id + "/foto.jpg");

                        return lojaRepository.save(loja);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao salvar foto: " + e.getMessage());
                    }
                });
    }

    public List<Loja> listarLojas(){
        return lojaRepository.findAll();
    }

    public Optional<Loja> listarLojaId(Long id){
        return lojaRepository.findById(id);
    }

    public boolean removerLoja(Long id){
        if(lojaRepository.existsById(id)){
            lojaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Novo método para buscar lojas por nome
    public List<Loja> buscarPorNome(String nome) {
        return lojaRepository.findByNomeContainingIgnoreCase(nome);
    }

    // Novo método para listar lojas recomendadas
    public List<Loja> listarLojasRecomendadas() {
        // Pode implementar lógica de recomendação aqui
        // Por enquanto, retorna todas as lojas
        return lojaRepository.findAll();
    }

    // Novo método para listar lojas por usuário
    public List<Loja> listarLojasPorUsuario(String emailUsuario) {
        // Implemente esta lógica quando tiver o relacionamento com Usuario
        // Por enquanto, retorna todas as lojas
        return lojaRepository.findAll();
    }
}