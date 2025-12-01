package io.github.brunoeugeniodev.marketplace.service;

import io.github.brunoeugeniodev.marketplace.models.Endereco;
import io.github.brunoeugeniodev.marketplace.repository.EnderecoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnderecoService {
    private final EnderecoRepository enderecoRepository;

    public EnderecoService(EnderecoRepository enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    public Endereco salvarEndereco(Endereco endereco){
        return enderecoRepository.save(endereco);
    }

    public Optional<Endereco> editarEndereco(Long id, Endereco enderecoNovo){
        return enderecoRepository.findById(id)
                .map(enderecoExistente -> {
                    enderecoExistente.setRua(enderecoNovo.getRua());
                    enderecoExistente.setBairro(enderecoNovo.getBairro());
                    enderecoExistente.setCidade(enderecoNovo.getCidade());
                    enderecoExistente.setNumero(enderecoNovo.getNumero());
                    enderecoExistente.setEstado(enderecoNovo.getEstado());
                    return enderecoRepository.save(enderecoExistente);
                });
    }

    public List<Endereco> listarEnderecos(){
        return enderecoRepository.findAll();
    }

    public Optional<Endereco> listarEnderecoId(Long id){
        return enderecoRepository.findById(id);
    }

    public boolean removerEndereco(Long id){
        if(enderecoRepository.existsById(id)){
            enderecoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}