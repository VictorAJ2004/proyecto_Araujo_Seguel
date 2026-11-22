package cl.duoc.pedidos.orders.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/admin/streams")
public class StreamAdminController {

    private final StreamAdminService adminService;

    public StreamAdminController(StreamAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<String> listar() {
        return adminService.listarStreams();
    }

    @GetMapping("/{name}")
    public Map<String, Object> describir(@PathVariable String name) {
        return adminService.describirStream(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> crear(@RequestBody CreateStreamRequest request) {
        return Map.of("mensaje", adminService.crearStream(request.streamName(), request.shards()));
    }

    @PatchMapping("/{name}/shards")
    public Map<String, Object> modificarShards(@PathVariable String name, @RequestBody UpdateShardsRequest request) {
        return adminService.modificarShards(name, request.shards());
    }

    @PatchMapping("/{name}/retention")
    public Map<String, String> modificarRetencion(@PathVariable String name, @RequestBody UpdateRetentionRequest request) {
        return Map.of("mensaje", adminService.modificarRetencion(name, request.retentionHours()));
    }

    @DeleteMapping("/{name}")
    public Map<String, String> eliminar(@PathVariable String name) {
        return Map.of("mensaje", adminService.eliminarStream(name));
    }

    @GetMapping("/{name}/consumers")
    public Map<String, Object> inspeccionarConsumidores(@PathVariable String name) {
        return adminService.inspeccionarConsumidores(name);
    }

    public record CreateStreamRequest(String streamName, int shards) {}
    public record UpdateShardsRequest(int shards) {}
    public record UpdateRetentionRequest(int retentionHours) {}
}