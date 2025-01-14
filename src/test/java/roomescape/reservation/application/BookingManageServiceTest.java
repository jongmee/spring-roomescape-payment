package roomescape.reservation.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.global.exception.ViolationException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.response.MyReservationResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static roomescape.TestFixture.MIA_RESERVATION;
import static roomescape.TestFixture.MIA_RESERVATION_DATE;
import static roomescape.reservation.domain.ReservationStatus.BOOKING;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

class BookingManageServiceTest extends ReservationServiceTest {
    @Autowired
    private BookingManageService bookingManageService;

    @Autowired
    private WaitingManageService waitingManageService;

    @Autowired
    private ReservationQueryService reservationQueryService;

    @Test
    @DisplayName("예약을 생성한다.")
    void create() {
        // given
        Reservation reservation = MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING);

        // when
        Reservation createdReservation = bookingManageService.create(reservation);

        // then
        assertSoftly(softly -> {
            softly.assertThat(createdReservation.getId()).isNotNull();
            softly.assertThat(createdReservation.getStatus()).isEqualTo(BOOKING);
        });
    }

    @Test
    @DisplayName("동일한 테마, 날짜, 시간에 한 팀만 예약할 수 있다.")
    void createWithOverflowCapacity() {
        // given
        bookingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        Reservation duplicatedReservation = new Reservation(
                admin, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, BOOKING);

        // when
        Reservation createReservation = bookingManageService.create(duplicatedReservation);

        // then
        assertThat(createReservation.getStatus()).isEqualTo(WAITING);
    }

    @Test
    @DisplayName("예약을 삭제한다.")
    void delete() {
        // given
        Reservation reservation = bookingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        // when & then
        assertThatCode(() -> bookingManageService.delete(reservation.getId(), admin))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("사용자는 확정된 예약을 취소할 수 없다.")
    void deleteMyReservationInBooking() {
        // given
        Reservation reservation = bookingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));

        // when & then
        assertThatThrownBy(() -> bookingManageService.delete(reservation.getId(), mia))
                .isInstanceOf(ViolationException.class);
    }

    @Test
    @DisplayName("예약을 삭제하면 첫 번째 대기 예약 상태가 결제 대기로 바뀐다.")
    void deleteAndChangeToBooking() {
        // given
        Reservation reservationInBooking = bookingManageService.create(MIA_RESERVATION(miaReservationTime, wootecoTheme, mia, BOOKING));
        waitingManageService.create(new Reservation(admin, MIA_RESERVATION_DATE, miaReservationTime, wootecoTheme, WAITING));

        // when
        bookingManageService.delete(reservationInBooking.getId(), admin);

        // then
        List<MyReservationResponse> reservations = reservationQueryService.findAllMyReservations(admin);
        assertThat(reservations.get(0).status()).isEqualTo("결제 대기");
    }
}
