package io.github.brunoeugeniodev.marketplace.util;

import io.github.brunoeugeniodev.marketplace.dto.*;
import io.github.brunoeugeniodev.marketplace.models.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MapperUtil {

    private final ModelMapper modelMapper;

    // Usuario
    public UsuarioDTO toUsuarioDTO(Usuario usuario) {
        UsuarioDTO dto = modelMapper.map(usuario, UsuarioDTO.class);
        return dto;
    }

    public Usuario toUsuarioEntity(UsuarioRegisterDTO dto) {
        Usuario usuario = modelMapper.map(dto, Usuario.class);
        usuario.setRoles(List.of("ROLE_USER"));
        return usuario;
    }

    public Usuario toUsuarioEntity(UsuarioUpdateDTO dto) {
        Usuario usuario = modelMapper.map(dto, Usuario.class);
        return usuario;
    }

    // Produto
    public ProdutoDTO toProdutoDTO(Produto produto) {
        ProdutoDTO dto = modelMapper.map(produto, ProdutoDTO.class);
        dto.setLojaId(produto.getLoja() != null ? produto.getLoja().getId() : null);
        dto.setLojaNome(produto.getLoja() != null ? produto.getLoja().getNome() : null);
        dto.setLojaCnpj(produto.getLoja() != null ? produto.getLoja().getCnpj() : null);
        return dto;
    }

    public Produto toProdutoEntity(ProdutoDTO dto) {
        return modelMapper.map(dto, Produto.class);
    }

    public Produto toProdutoEntity(ProdutoCreateDTO dto) {
        return modelMapper.map(dto, Produto.class);
    }

    public Produto toProdutoEntity(ProdutoUpdateDTO dto) {
        return modelMapper.map(dto, Produto.class);
    }

    // Loja
    public LojaDTO toLojaDTO(Loja loja) {
        LojaDTO dto = modelMapper.map(loja, LojaDTO.class);
        if (loja.getUsuario() != null) {
            dto.setProprietarioId(loja.getUsuario().getId());
            dto.setProprietarioNome(loja.getUsuario().getNome());
        }
        dto.setQuantidadeProdutos(loja.getProdutos() != null ? loja.getProdutos().size() : 0);

        // Converter EnderecoLoja para EnderecoLojaDTO
        if (loja.getEndereco() != null) {
            EnderecoLojaDTO enderecoDTO = toEnderecoLojaDTO(loja.getEndereco());
            dto.setEndereco(enderecoDTO);
        }

        return dto;
    }

    public Loja toLojaEntity(LojaDTO dto) {
        return modelMapper.map(dto, Loja.class);
    }

    public Loja toLojaEntity(LojaCreateDTO dto) {
        Loja loja = modelMapper.map(dto, Loja.class);
        // Converter EnderecoLojaCreateDTO para EnderecoLoja
        if (dto.getEndereco() != null) {
            Loja.EnderecoLoja endereco = toEnderecoLojaEntity(dto.getEndereco());
            loja.setEndereco(endereco);
        }
        return loja;
    }

    public Loja toLojaEntity(LojaUpdateDTO dto) {
        Loja loja = modelMapper.map(dto, Loja.class);
        if (dto.getEndereco() != null) {
            Loja.EnderecoLoja endereco = toEnderecoLojaEntity(dto.getEndereco());
            loja.setEndereco(endereco);
        }
        return loja;
    }

    // EnderecoLoja (classe interna da Loja)
    public EnderecoLojaDTO toEnderecoLojaDTO(Loja.EnderecoLoja enderecoLoja) {
        return modelMapper.map(enderecoLoja, EnderecoLojaDTO.class);
    }

    public Loja.EnderecoLoja toEnderecoLojaEntity(EnderecoLojaDTO dto) {
        return modelMapper.map(dto, Loja.EnderecoLoja.class);
    }

    public Loja.EnderecoLoja toEnderecoLojaEntity(EnderecoLojaCreateDTO dto) {
        return modelMapper.map(dto, Loja.EnderecoLoja.class);
    }

    public Loja.EnderecoLoja toEnderecoLojaEntity(EnderecoLojaUpdateDTO dto) {
        return modelMapper.map(dto, Loja.EnderecoLoja.class);
    }

    // Endereco (do Usuario)
    public EnderecoDTO toEnderecoDTO(Endereco endereco) {
        EnderecoDTO dto = modelMapper.map(endereco, EnderecoDTO.class);
        dto.setUsuarioId(endereco.getUsuario() != null ? endereco.getUsuario().getId() : null);
        return dto;
    }

    public Endereco toEnderecoEntity(EnderecoDTO dto) {
        return modelMapper.map(dto, Endereco.class);
    }

    public Endereco toEnderecoEntity(EnderecoCreateDTO dto) {
        return modelMapper.map(dto, Endereco.class);
    }

    public Endereco toEnderecoEntity(EnderecoUpdateDTO dto) {
        return modelMapper.map(dto, Endereco.class);
    }

    // Carrinho
    public CarrinhoDTO mapCarrinhoToDTO(Carrinho carrinho) {
        CarrinhoDTO dto = modelMapper.map(carrinho, CarrinhoDTO.class);
        if (carrinho.getUsuario() != null) {
            dto.setUsuarioId(carrinho.getUsuario().getId());
            dto.setUsuarioNome(carrinho.getUsuario().getNome());
            dto.setUsuarioEmail(carrinho.getUsuario().getEmail());
        }

        // Converter itens
        if (carrinho.getItens() != null && !carrinho.getItens().isEmpty()) {
            List<ItemCarrinhoDTO> itensDTO = carrinho.getItens().stream()
                    .map(this::mapItemCarrinhoToDTO)
                    .collect(Collectors.toList());
            dto.setItens(itensDTO);
        }

        return dto;
    }

    public ItemCarrinhoDTO mapItemCarrinhoToDTO(ItemCarrinho item) {
        ItemCarrinhoDTO dto = modelMapper.map(item, ItemCarrinhoDTO.class);
        if (item.getProduto() != null) {
            dto.setProdutoId(item.getProduto().getId());
            dto.setProdutoNome(item.getProduto().getNome());
            dto.setProdutoDescricao(item.getProduto().getDescricao());
            dto.setProdutoPreco(item.getProduto().getPreco());
            dto.setProdutoFotoUrl(item.getProduto().getFotoUrl());
            dto.setProdutoQuantidadeDisponivel(item.getProduto().getQuantidade());
            dto.setProdutoDisponivel(item.getProduto().isDisponivel());
        }
        return dto;
    }

    // ItemCarrinhoRequestDTO para ItemCarrinho (opcional, mas útil)
    public ItemCarrinho toItemCarrinhoEntity(ItemCarrinhoRequestDTO dto, Produto produto) {
        ItemCarrinho item = new ItemCarrinho();
        item.setProduto(produto);
        item.setQuantidade(dto.getQuantidade());
        item.setPrecoUnitario(produto.getPreco());
        return item;
    }

    // Métodos auxiliares para configuração do ModelMapper (opcional)
    public void configureModelMapper() {
        // Configurações específicas se necessário
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);

        // Mapeamentos personalizados podem ser adicionados aqui
    }

    // Listas
    public <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }
}