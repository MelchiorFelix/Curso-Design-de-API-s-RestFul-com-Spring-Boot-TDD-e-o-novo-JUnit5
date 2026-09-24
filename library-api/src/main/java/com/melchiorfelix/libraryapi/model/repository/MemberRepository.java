package com.melchiorfelix.libraryapi.model.repository;

import com.melchiorfelix.libraryapi.model.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByCardNumberIgnoreCase(String cardNumber);
    boolean existsByCardNumberIgnoreCaseAndIdNot(String cardNumber, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findLockedById(@Param("id") Long id);

    @Query("""
            select m from Member m where
            (:active is null or m.active = :active) and
            (lower(m.name) like lower(concat('%', :query, '%')) or
             lower(m.cardNumber) like lower(concat('%', :query, '%')) or
             lower(m.email) like lower(concat('%', :query, '%')))
            """)
    Page<Member> search(@Param("query") String query, @Param("active") Boolean active, Pageable pageable);
}
