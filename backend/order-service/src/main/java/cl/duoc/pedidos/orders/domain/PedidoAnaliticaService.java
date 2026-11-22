package cl.duoc.pedidos.orders.domain;

import cl.duoc.pedidos.orders.streaming.OrderEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class PedidoAnaliticaService {

    private static final Logger log = LoggerFactory.getLogger(PedidoAnaliticaService.class);
    private final AtomicLong totalPedidosProcesados = new AtomicLong(0);

    public void procesarEventoAnalitica(OrderEventDTO evento) {
        // Validación de negocio (error irrecuperable si viene mal formado)
        if (evento.getOrderId() == null || evento.getOrderId().isBlank()) {
            throw new IllegalArgumentException("El evento no tiene orderId válido");
        }
        if (evento.getTotal() == null || evento.getTotal() < 0) {
            throw new IllegalArgumentException("El monto total del pedido es inválido: " + evento.getTotal());
        }

        long acumulado = totalPedidosProcesados.incrementAndGet();
        log.info("[ANALITICA] Pedido={} | Tipo={} | Monto={} | Total eventos procesados={}",
                evento.getOrderId(), evento.getTipoEvento(), evento.getTotal(), acumulado);
    }
}