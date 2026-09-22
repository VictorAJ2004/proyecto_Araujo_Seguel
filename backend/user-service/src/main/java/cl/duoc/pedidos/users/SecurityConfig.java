package cl.duoc.pedidos.users;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
@Configuration public class SecurityConfig { @Bean SecurityFilterChain filter(HttpSecurity http)throws Exception{return http.csrf(c->c.disable()).authorizeHttpRequests(a->a.anyRequest().authenticated()).oauth2ResourceServer(o->o.jwt()).build();} @Bean JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")String issuer,@Value("${spring.security.oauth2.resourceserver.jwt.audiences}")String audience){NimbusJwtDecoder decoder=NimbusJwtDecoder.withIssuerLocation(issuer).build(); OAuth2TokenValidator<Jwt> issuerValidator=JwtValidators.createDefaultWithIssuer(issuer); OAuth2TokenValidator<Jwt> audienceValidator=token->token.getAudience().contains(audience)?OAuth2TokenValidatorResult.success():OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token","Invalid audience",null)); decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerValidator,audienceValidator)); return decoder;} }
