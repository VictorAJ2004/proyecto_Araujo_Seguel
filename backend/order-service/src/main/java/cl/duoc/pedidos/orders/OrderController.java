package cl.duoc.pedidos.orders;

import cl.duoc.pedidos.orders.streaming.OrderEventDTO;
import cl.duoc.pedidos.orders.streaming.OrderEventProducer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository repository;
    private final OrderEventProducer orderEventProducer;

    public OrderController(OrderRepository repository, OrderEventProducer orderEventProducer) {
        this.repository = repository;
        this.orderEventProducer = orderEventProducer;
    }

    @GetMapping
    public List<Order> list() {
        return repository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody CreateOrder request) {
        Order saved = repository.save(new Order(request.customer(), request.email(), request.totalCents()));

        OrderEventDTO evento = new OrderEventDTO(
                String.valueOf(saved.getId()),
                request.customer(),
                "PEDIDO_CREADO",
                (double) request.totalCents()
        );
        orderEventProducer.publicarEventoPedido(evento);

        return saved;
    }

    @PatchMapping("/{id}/status")
    public Order status(@PathVariable Long id, @Valid @RequestBody StatusChange request) {
        Order order = repository.findById(id).orElseThrow();
        order.setStatus(request.status());
        Order saved = repository.save(order);

        OrderEventDTO evento = new OrderEventDTO(
                String.valueOf(saved.getId()),
                saved.getCustomer(),
                "PEDIDO_ACTUALIZADO_" + request.status().toUpperCase(),
                (double) saved.getTotalCents()
        );
        orderEventProducer.publicarEventoPedido(evento);

        return saved;
    }

    public record CreateOrder(@NotBlank String customer, @Email @NotBlank String email, @Min(0) long totalCents) { }
    public record StatusChange(@NotBlank String status) { }
}