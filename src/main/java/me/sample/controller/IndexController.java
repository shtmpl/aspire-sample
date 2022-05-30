package me.sample.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * This class representation a root application controller for correct working in clouds
 */
@ApiIgnore
@RestController
@RequestMapping({"/api", "/"})
public class IndexController {

    @GetMapping
    public ResponseEntity get() {
        return ResponseEntity.ok().build();
    }
}
