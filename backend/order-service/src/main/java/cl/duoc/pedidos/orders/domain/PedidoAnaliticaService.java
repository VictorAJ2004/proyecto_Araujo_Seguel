package cl.duoc.pedidos.orders.domain;

import cl.duoc.pedidos.orders.OrderRepository;
import cl.duoc.pedidos.orders.streaming.OrderEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class PedidoAnaliticaService {

    private static final Logger log = LoggerFactory.getLogger(PedidoAnaliticaService.class);
    private final AtomicLong totalPedidosProcesados = new AtomicLong(0);
    private final OrderRepository orderRepository;

    public PedidoAnaliticaService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

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

        // Actualización automática de estado en RDS gatillada por el evento de Kinesis
        try {
            Long id = Long.valueOf(evento.getOrderId());
            orderRepository.findById(id).ifPresent(order -> {
                order.setStatus("PROCESSED");
                orderRepository.save(order);
                log.info("[ANALITICA] Estado actualizado automáticamente en BD: pedidoId={} -> PROCESSED", order.getId());
            });
        } catch (NumberFormatException e) {
            log.warn("[ANALITICA] orderId no numérico, se omite actualización en BD: {}", evento.getOrderId());
        }
    }
}