package me.jexom.phishylinks.repository;

import me.jexom.phishylinks.domain.BlacklistedLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedLinkRepository extends JpaRepository<BlacklistedLink, Long> {

}
