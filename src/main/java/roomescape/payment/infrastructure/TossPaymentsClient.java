package roomescape.payment.infrastructure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.domain.ConfirmedPayment;
import roomescape.payment.domain.NewPayment;
import roomescape.payment.domain.PGCompany;
import roomescape.payment.domain.PaymentCancelInfo;
import roomescape.payment.domain.PaymentCancelResult;
import roomescape.payment.domain.PaymentClient;
import roomescape.payment.exception.PaymentServerException;

@Component
public class TossPaymentsClient implements PaymentClient {
    private final RestClient restClient;
    private final PaymentClientErrorHandler errorHandler;

    public TossPaymentsClient(@Qualifier("tossPaymentsClientBuilder") RestClient.Builder builder,
                              PaymentClientErrorHandler errorHandler) {
        this.restClient = builder.build();
        this.errorHandler = errorHandler;
    }

    @Override
    public ConfirmedPayment confirm(NewPayment newPayment) {
        ConfirmedPayment confirmedPayment = restClient.post()
                .uri("/confirm")
                .body(newPayment)
                .retrieve()
                .onStatus(errorHandler)
                .body(ConfirmedPayment.class);
        if (confirmedPayment == null) {
            throw new PaymentServerException("결제가 실패했습니다.");
        }
        confirmedPayment.setCompany(PGCompany.TOSS);
        return confirmedPayment;
    }

    @Override
    public void cancel(PaymentCancelInfo paymentCancelInfo) {
        String uri = String.format("/%s/cancel", paymentCancelInfo.paymentKey());
        PaymentCancelResult paymentCancelResult = restClient.post()
                .uri(uri)
                .body(paymentCancelInfo)
                .retrieve()
                .onStatus(errorHandler)
                .body(PaymentCancelResult.class);
        validateCanceledPayment(paymentCancelResult);
    }

    private void validateCanceledPayment(PaymentCancelResult paymentCancelResult) {
        if (paymentCancelResult == null) {
            throw new PaymentServerException("결제 취소가 실패했습니다.");
        }
        if (!paymentCancelResult.isCorrectStatus()) {
            throw new PaymentServerException("결제 취소가 실패했습니다.");
        }
    }
}
