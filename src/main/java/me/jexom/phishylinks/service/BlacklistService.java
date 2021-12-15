package me.jexom.phishylinks.service;

import me.jexom.phishylinks.domain.BlacklistedLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BlacklistService {
    List<String> addLinks(List<String> links);

    void deleteLinkById(Long id);

    Page<BlacklistedLink> listLinks(Pageable pageable);
}
