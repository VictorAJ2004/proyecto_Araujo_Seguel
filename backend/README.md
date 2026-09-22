# Pedidos360 Backend

Microservicios Java Spring Boot para la Evaluacion Parcial DSY1107.

- `order-service`: entidades, repositorio JPA y endpoints de pedidos.
- `user-service`: endpoint protegido de identidad.
- Ambos usan OAuth2 Resource Server y validan issuer, firma, expiracion y audience del JWT Cognito.

Compilar: `mvn clean verify` desde esta carpeta.

Configurar `COGNITO_ISSUER_URI`, `COGNITO_AUDIENCE` y `DATABASE_URL` antes de ejecutar. En AWS, API Gateway valida el mismo JWT y reenvia a las instancias EC2.
