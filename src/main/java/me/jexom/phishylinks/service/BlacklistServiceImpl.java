package me.jexom.phishylinks.service;

import lombok.RequiredArgsConstructor;
import me.jexom.phishylinks.domain.BlacklistedLink;
import me.jexom.phishylinks.repository.BlacklistedLinkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final BlacklistedLinkRepository blacklistedLinkRepository;

    @Override
    public List<String> addLinks(List<String> links) {
        List<BlacklistedLink> blacklistLinks = new ArrayList<>(links.size());
        for (String link : links) {
            Matcher matcher = Pattern.compile("https?://(?:www\\.)?(?<domain>[\\w.-]*)/?.*").matcher(link);
            if (matcher.matches()) {
                String domain = matcher.group("domain");
                blacklistLinks.add(BlacklistedLink.builder()
                        .id((long) domain.hashCode())
                        .link(domain)
                        .build());
            }
        }
        return blacklistedLinkRepository.saveAll(blacklistLinks).stream()
                .map(BlacklistedLink::getLink)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLinkById(Long id) {
        blacklistedLinkRepository.deleteById(id);
    }

    @Override
    public Page<BlacklistedLink> listLinks(Pageable pageable) {
        return blacklistedLinkRepository.findAll(pageable);
    }
}
