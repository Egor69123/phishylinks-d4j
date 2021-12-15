package me.jexom.phishylinks.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.jexom.phishylinks.domain.BlacklistedLink;
import me.jexom.phishylinks.service.BlacklistService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/blacklist")
public class BlacklistController {

    private final BlacklistService blacklistService;

    @ApiOperation("Add new links to blacklist")
    @PostMapping("/links")
    public ResponseEntity<List<String>> addLinks(
            @RequestBody List<String> links
    ) {
        return ResponseEntity.ok(blacklistService.addLinks(links));
    }

    @ApiOperation("Delete a link by id")
    @DeleteMapping("/link/{linkId}")
    public ResponseEntity<String> deleteLink(@RequestParam("linkId") Long linkId) {
        blacklistService.deleteLinkById(linkId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Success");
    }

    @ApiOperation("View blacklisted links")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "Integer", paramType = "query", value = "Results page you want to retrieve (0..N). Default: 0", defaultValue = "0"),
            @ApiImplicitParam(name = "size", dataType = "Integer", paramType = "query", value = "Number of records per page. Default: 20", defaultValue = "20"),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc)")
    })
    @GetMapping("/links")
    public ResponseEntity<Page<BlacklistedLink>> getLinks(
            @ApiIgnore Pageable pageable
    ) {
        return ResponseEntity.ok(blacklistService.listLinks(pageable));
    }
}
