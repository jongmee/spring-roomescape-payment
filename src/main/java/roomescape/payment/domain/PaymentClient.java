package roomescape.payment.domain;

import org.springframework.scheduling.annotation.Async;

public interface PaymentClient {
    ConfirmedPayment confirm(NewPayment newPayment);

    @Async
    void cancel(PaymentCancelInfo paymentCancelInfo);
}
