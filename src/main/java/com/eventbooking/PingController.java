package com.eventbooking;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class PingController {
    record PingResponse(String status, Instant time){}

    @GetMapping("api/ping")
    public PingResponse ping(){
        return new PingResponse("ok", Instant.now());
    }

}
