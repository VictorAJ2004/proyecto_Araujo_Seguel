package cl.duoc.pedidos.orders;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private List<Product> productos = new ArrayList<>();
    private long idCounter = 1;

    public ProductController() {
        // Se inician 2 productos automáticamente al levantar el backend
        productos.add(new Product(idCounter++, "Teclado Mecánico Redragon", 45000));
        productos.add(new Product(idCounter++, "Audífonos Sony ULT", 120000));
    }

    @GetMapping
    public List<Product> getAll() {
        return productos;
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        product.setId(idCounter++);
        productos.add(product);
        return product;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productos.removeIf(p -> p.getId().equals(id));
    }
}