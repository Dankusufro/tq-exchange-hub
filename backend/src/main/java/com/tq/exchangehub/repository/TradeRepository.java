package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findByStatus(TradeStatus status);
    @Query(
            "SELECT COUNT(t) FROM Trade t"
                    + " WHERE (t.owner = :profile OR t.requester = :profile)"
                    + " AND t.status = :status")
    long countByParticipantAndStatus(
            @Param("profile") Profile profile, @Param("status") TradeStatus status);
    long countByStatus(TradeStatus status);

    List<Trade> findByCreatedAtAfter(OffsetDateTime createdAt);

    @Query(
            "SELECT t FROM Trade t WHERE t.owner.id = :ownerId OR t.requester.id = :requesterId")
    List<Trade> findByOwnerIdOrRequesterId(
            @Param("ownerId") UUID ownerId, @Param("requesterId") UUID requesterId);

    @Query(
            "SELECT t FROM Trade t"
                    + " WHERE (t.owner.id = :ownerId OR t.requester.id = :requesterId)"
                    + " AND t.status = :status")
    List<Trade> findByOwnerIdOrRequesterIdAndStatus(
            @Param("ownerId") UUID ownerId,
            @Param("requesterId") UUID requesterId,
            @Param("status") TradeStatus status);

    @Query(
            "SELECT t FROM Trade t"
                    + " WHERE (t.owner.id = :ownerId OR t.requester.id = :requesterId)"
                    + " AND t.status IN :statuses")
    List<Trade> findByOwnerIdOrRequesterIdAndStatusIn(
            @Param("ownerId") UUID ownerId,
            @Param("requesterId") UUID requesterId,
            @Param("statuses") Collection<TradeStatus> statuses);

    @EntityGraph(
            attributePaths = {
                "owner",
                "owner.account",
                "requester",
                "requester.account",
                "ownerItem",
                "requesterItem"
            })
    @Query(
            "SELECT t FROM Trade t"
                    + " WHERE t.id = :tradeId"
                    + " AND (t.owner.id = :profileId OR t.requester.id = :profileId)")
    Optional<Trade> findByIdForParticipant(
            @Param("tradeId") UUID tradeId, @Param("profileId") UUID profileId);

    @Query(
            "SELECT t.status AS status, t.owner.location AS ownerLocation, t.requester.location AS requesterLocation"
                    + " FROM Trade t")
    List<TradeStatusLocationView> findStatusAndLocations();

    interface TradeStatusLocationView {
        TradeStatus getStatus();

        String getOwnerLocation();

        String getRequesterLocation();
    }
}
