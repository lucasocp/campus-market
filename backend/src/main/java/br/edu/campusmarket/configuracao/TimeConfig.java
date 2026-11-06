package br.edu.campusmarket.configuracao;
import java.time.Clock;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
@Configuration @EnableScheduling
public class TimeConfig {
    @Bean Clock clock() { return Clock.systemUTC(); }
}
