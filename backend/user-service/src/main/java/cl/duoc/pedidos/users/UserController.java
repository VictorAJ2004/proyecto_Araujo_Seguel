package cl.duoc.pedidos.users;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController public class UserController { @GetMapping("/api/users/me") public Map<String,String> me(@AuthenticationPrincipal Jwt jwt){return Map.of("subject",jwt.getSubject(),"email",jwt.getClaimAsString("email"));} }
