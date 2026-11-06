package br.edu.campusmarket.common;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class HealthController {
    @GetMapping("/api/health") Map<String, String> health() {
        return Map.of("status", "UP", "application", "CampusMarket");
    }
}
