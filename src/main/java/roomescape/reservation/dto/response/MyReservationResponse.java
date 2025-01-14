package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationPayment;
import roomescape.reservation.domain.WaitingReservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record MyReservationResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime time,
        String status,
        String paymentKey,
        Long amount
) {

    public static MyReservationResponse from(ReservationPayment reservationPayment) {
        Reservation reservation = reservationPayment.reservation();
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약",
                reservationPayment.paymentKey(),
                reservationPayment.totalAmount()
        );
    }

    public static MyReservationResponse from(Reservation unpaidReservation) {
        return new MyReservationResponse(
                unpaidReservation.getId(),
                unpaidReservation.getTheme().getName(),
                unpaidReservation.getDate(),
                unpaidReservation.getTime().getStartAt(),
                "결제 대기",
                null,
                null
        );
    }

    public static MyReservationResponse from(WaitingReservation waitingReservation) {
        return new MyReservationResponse(
                waitingReservation.getReservation().getId(),
                waitingReservation.getReservation().getTheme().getName(),
                waitingReservation.getReservation().getDate(),
                waitingReservation.getReservation().getTime().getStartAt(),
                (waitingReservation.calculateOrder()) + "번째 예약대기",
                null,
                null
        );
    }
}
