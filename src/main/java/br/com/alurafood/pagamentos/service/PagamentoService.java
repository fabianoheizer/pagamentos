package br.com.alurafood.pagamentos.service;

import br.com.alurafood.pagamentos.dto.PagamentoDTO;
import br.com.alurafood.pagamentos.http.PedidoClient;
import br.com.alurafood.pagamentos.model.Pagamento;
import br.com.alurafood.pagamentos.model.Status;
import br.com.alurafood.pagamentos.repository.PagamentoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository pagamentoRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    PedidoClient pedidoClient;

    public Page<PagamentoDTO> obterTodos(Pageable paginacao) {
        return pagamentoRepository.findAll(paginacao).map(pgto -> modelMapper.map(pgto, PagamentoDTO.class));
    }

    public PagamentoDTO obterPorID(Long id){
        Pagamento pagamento = pagamentoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException());
        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    public PagamentoDTO criarPagamento(PagamentoDTO pagamentoDTO){
        Pagamento pagamento = modelMapper.map(pagamentoDTO, Pagamento.class);
        pagamento.setStatus(Status.CRIADO);
        pagamentoRepository.save(pagamento);
        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    public PagamentoDTO atualizarPagamento(Long id, PagamentoDTO dto) {
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
        pagamento.setId(id);
        pagamento = pagamentoRepository.save(pagamento);
        return modelMapper.map(pagamento, PagamentoDTO.class);
    }

    public void excluirPagamento(Long id) {
        pagamentoRepository.deleteById(id);
    }

    public void confirmarPagamento(Long id){
        Optional<Pagamento> pagamento = pagamentoRepository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO);
        pagamentoRepository.save(pagamento.get());
        pedidoClient.atualizaPagamento(pagamento.get().getPedidoId());
    }

    public void alterarStatus(Long id) {
        Optional<Pagamento> pagamento = pagamentoRepository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        pagamentoRepository.save(pagamento.get());

    }
}
