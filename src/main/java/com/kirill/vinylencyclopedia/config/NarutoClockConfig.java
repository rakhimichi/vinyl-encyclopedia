package com.kirill.vinylencyclopedia.config;

import com.kirill.vinylencyclopedia.naruto.NarutoStats;
import org.springframework.context.annotation.*;
import java.time.Clock;

@Configuration
public class NarutoClockConfig {
    @Bean
    public Clock narutoClock() { return Clock.system(NarutoStats.ZONE); }
}
