package roomescape.payment.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.dto.PaymentConfirmRequest;

import java.util.Base64;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class TossPaymentsClient {
    private final RestClient restClient;

    public TossPaymentsClient(@Value("${security.toss.secret-key}") String secretKey) {
        secretKey = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());
        this.restClient = RestClient.builder()
                .baseUrl("https://api.tosspayments.com/v1/payments")
                .defaultHeader(AUTHORIZATION, "Basic " + secretKey)
                .build();
    }

    public void confirm(PaymentConfirmRequest request) {
        restClient.post()
                .uri("/confirm")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}