package cl.duoc.pedidos.orders.streaming;

import java.time.Instant;
import java.util.UUID;

public class OrderEventDTO {
    private String eventId;
    private String orderId;
    private String clienteId;
    private String tipoEvento; // ej: PEDIDO_CREADO, PEDIDO_ACTUALIZADO
    private Double total;
    private String timestamp;

    public OrderEventDTO() {}

    public OrderEventDTO(String orderId, String clienteId, String tipoEvento, Double total) {
        this.eventId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.clienteId = clienteId;
        this.tipoEvento = tipoEvento;
        this.total = total;
        this.timestamp = Instant.now().toString();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}