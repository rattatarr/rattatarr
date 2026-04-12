package com.rattatarr.rattatarr.configs;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZoneId;

@ControllerAdvice
public class GlobalModelAttributeAdvice {

    @ModelAttribute("zoneId")
    public ZoneId resolveZone(
            @RequestParam(name = "timezone", required = false, defaultValue = "UTC") String timezone
    ) {
        return ZoneId.of(timezone);
    }
}
