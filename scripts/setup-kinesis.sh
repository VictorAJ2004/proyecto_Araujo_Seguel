#!/bin/bash
set -e

REGION="us-east-1"

echo "=== 1. Creando Streams de Kinesis en $REGION ==="

#Eventos de pedidos
aws kinesis create-stream \
  --stream-name pedidos-eventos-stream \
  --shard-count 3 \
  --stream-mode-details StreamMode=PROVISIONED \
  --region $REGION

#  Actividad de usuarios
aws kinesis create-stream \
  --stream-name usuarios-actividad-stream \
  --shard-count 3 \
  --stream-mode-details StreamMode=PROVISIONED \
  --region $REGION

# Dead Letter Stream para eventos irrecuperables 
aws kinesis create-stream \
  --stream-name pedidos-dlq-stream \
  --shard-count 1 \
  --stream-mode-details StreamMode=PROVISIONED \
  --region $REGION

echo "=== 2. Esperando a que los streams estén activos ==="
aws kinesis wait stream-exists --stream-name pedidos-eventos-stream --region $REGION
aws kinesis wait stream-exists --stream-name usuarios-actividad-stream --region $REGION
aws kinesis wait stream-exists --stream-name pedidos-dlq-stream --region $REGION

echo "=== 3. Configurando políticas de retención ==="
aws kinesis increase-stream-retention-period \
  --stream-name pedidos-eventos-stream \
  --retention-period-hours 48 \
  --region $REGION

aws kinesis increase-stream-retention-period \
  --stream-name pedidos-dlq-stream \
  --retention-period-hours 168 \
  --region $REGION

echo "=== 4. Creando Alarma de CloudWatch (Retraso de consumo / Iterator Age) ==="
aws cloudwatch put-metric-alarm \
  --alarm-name "Kinesis-Pedidos-IteratorAge-Alto" \
  --alarm-description "Alerta cuando el retraso de consumo en pedidos-eventos-stream supera 1 minuto" \
  --namespace AWS/Kinesis \
  --metric-name GetRecords.IteratorAgeMilliseconds \
  --dimensions Name=StreamName,Value=pedidos-eventos-stream \
  --statistic Maximum \
  --period 60 \
  --evaluation-periods 2 \
  --threshold 60000 \
  --comparison-operator GreaterThanThreshold \
  --treat-missing-data notBreaching \
  --region $REGION

echo "=== Infraestructura lista ==="
aws kinesis list-streams --region $REGION