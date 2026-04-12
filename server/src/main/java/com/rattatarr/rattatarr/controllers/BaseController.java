package com.rattatarr.rattatarr.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;

@Validated
public class BaseController {
    protected final Logger logger;

    protected BaseController() {
        this.logger = LoggerFactory.getLogger(getClass());
    }
}
