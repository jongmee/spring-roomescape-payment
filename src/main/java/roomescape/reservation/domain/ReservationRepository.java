package roomescape.reservation.domain;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            JOIN FETCH r.member
            WHERE r.member = :member AND r.theme = :theme AND r.status = :status AND r.date >= :from AND r.date <= :to
            """)
    List<Reservation> findAllByMemberAndThemeAndStatusAndDateBetween(@Param(value = "member") Member member,
                                                                     @Param(value = "theme") Theme theme,
                                                                     @Param(value = "status") ReservationStatus status,
                                                                     @Param(value = "from") LocalDate fromDate,
                                                                     @Param(value = "to") LocalDate toDate);

    @Query("SELECT r.time.id FROM Reservation r WHERE r.date = :date AND r.theme = :theme")
    List<Long> findAllTimeIdsByDateAndTheme(@Param(value = "date") LocalDate date, @Param(value = "theme") Theme theme);

    @Query("""
            SELECT new roomescape.reservation.domain.ReservationPayment(r, p)
            FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            LEFT OUTER JOIN Payment p
            ON p.reservation = r
            WHERE r.member = :member AND r.status = :status
            """)
    List<ReservationPayment> findAllByMemberAndStatusWithPayment(@Param(value = "member") Member member,
                                                                 @Param(value = "status") ReservationStatus status);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE r.member = :member AND r.status = :status
            """)
    List<Reservation> findAllByMemberAndStatus(@Param(value = "member") Member member,
                                               @Param(value = "status") ReservationStatus status);

    int countByTime(ReservationTime time);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingReservation(
                r,
                (SELECT COUNT(w) FROM Reservation w
                    WHERE w.theme = r.theme
                        AND w.date = r.date
                        AND w.time = r.time
                        AND w.status = r.status
                        AND w.id < r.id))
            FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE r.member = :member AND r.status = roomescape.reservation.domain.ReservationStatus.WAITING
            """)
    List<WaitingReservation> findWaitingReservationsByMember(@Param(value = "member") Member member);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.theme
            JOIN FETCH r.time
            WHERE r.status IN :status
            """)
    List<Reservation> findAllByStatusWithDetails(@Param(value = "status") List<ReservationStatus> statusConditions);

    Optional<Reservation> findFirstByDateAndTimeAndThemeAndStatusOrderById(LocalDate date, ReservationTime time,
                                                                           Theme theme, ReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
    boolean existsByDateAndTimeAndThemeAndStatus(LocalDate date, ReservationTime time, Theme theme, ReservationStatus status);

    boolean existsByDateAndTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme, Member member);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Reservation r set r.status = :status where r.id = :id")
    void updateStatusById(@Param(value = "status") ReservationStatus status, @Param(value = "id") Long id);
}
