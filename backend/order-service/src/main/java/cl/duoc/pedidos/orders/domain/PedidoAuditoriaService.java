package cl.duoc.pedidos.orders.domain;

import cl.duoc.pedidos.orders.streaming.OrderEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PedidoAuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(PedidoAuditoriaService.class);

    public void registrarAuditoria(OrderEventDTO evento) {
        if (evento.getOrderId() == null || evento.getTipoEvento() == null) {
            throw new IllegalArgumentException("Evento incompleto para auditoría");
        }

        log.info("[AUDITORIA] Registro inmutable -> eventId={} | orderId={} | cliente={} | accion={} | fecha={}",
                evento.getEventId(), evento.getOrderId(), evento.getClienteId(),
                evento.getTipoEvento(), evento.getTimestamp());
    }
}