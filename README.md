# Pedidos360

Proyecto de la Evaluacion Parcial 1 de DSY1107.

## Stack exigido por la rubrica

- Frontend React + AWS Amplify Authenticator.
- Cognito para login y tokens.
- Backend con microservicios Java Spring Boot.
- OAuth2 Resource Server para validar JWT.
- API Gateway delante de servicios desplegados en EC2.

## Desarrollo

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

```powershell
cd backend
mvn clean verify
```

Antes de conectar AWS, copia `frontend/.env.example` a `frontend/.env` y completa los valores del User Pool de Cognito.