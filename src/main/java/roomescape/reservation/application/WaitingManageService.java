package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import roomescape.global.exception.ViolationException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;

@Service
public class WaitingManageService extends ReservationManageService {
    public WaitingManageService(ReservationRepository reservationRepository) {
        super(reservationRepository);
    }

    public void approve(Long reservationId, Member agent) {
        if (!agent.isAdmin()) {
            throw new ViolationException("예약 승인 권한이 없습니다.");
        }
        reservationRepository.findById(reservationId).ifPresent(reservation -> {
            reservation.changeToBooking();
            reservationRepository.save(reservation);
        });
    }

    @Override
    protected void correctReservationStatus(boolean existsReservation, Reservation reservation) {
        if (!existsReservation && reservation.isWaiting()) {
            reservation.changeToBooking();
        }
    }

    @Override
    protected void scheduleAfterDeleting(Reservation deletedReservation) {
    }

    @Override
    protected void validateReservationStatus(Reservation reservation) {
        if (reservation.isBooking()) {
            throw new ViolationException("대기 중인 예약이 아닙니다.");
        }
    }

    @Override
    protected void validatePermissionForDeleting(Reservation reservation, Member agent) {
        if (!reservation.isModifiableBy(agent)) {
            throw new ViolationException("대기 예약을 삭제할 권한이 없습니다. 예약자 혹은 관리자만 삭제할 수 있습니다.");
        }
    }
}
