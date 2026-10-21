package cl.duoc.pedidos.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class AwsNotificationService {

    @Value("${aws.sqs.url}")
    private String sqsUrl;

    @Value("${aws.sns.topic-arn}")
    private String snsTopicArn;

    @Value("${aws.region}")
    private String awsRegion;

    public void notificarCreacion(String mensaje) {
        Region region = Region.of(awsRegion);

        // Enviar a SQS
        try (SqsClient sqsClient = SqsClient.builder().region(region).build()) {
            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(sqsUrl)
                    .messageBody(mensaje)
                    .build();
            sqsClient.sendMessage(sendMsgRequest);
            System.out.println("✅ Evento registrado en SQS");
        } catch (Exception e) {
            System.err.println("❌ Error enviando a SQS: " + e.getMessage());
        }

        // Enviar correo por SNS
        try (SnsClient snsClient = SnsClient.builder().region(region).build()) {
            PublishRequest request = PublishRequest.builder()
                    .message(mensaje)
                    .topicArn(snsTopicArn)
                    .subject("Notificación Pedidos360")
                    .build();
            snsClient.publish(request);
            System.out.println("✅ Correo enviado vía SNS");
        } catch (Exception e) {
            System.err.println("❌ Error enviando a SNS: " + e.getMessage());
        }
    }
}