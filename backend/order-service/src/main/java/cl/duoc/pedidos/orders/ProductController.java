package cl.duoc.pedidos.orders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin("*")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AwsNotificationService awsNotificationService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        Product savedProduct = productRepository.save(product);
        
        // Ejecuta SQS y SNS
        String mensaje = "Se ha creado el producto: " + product.getName() + " con un precio de $" + product.getPrice();
        awsNotificationService.notificarCreacion(mensaje);
        
        return savedProduct;
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}