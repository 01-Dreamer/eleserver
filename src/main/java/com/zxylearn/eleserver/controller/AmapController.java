package com.zxylearn.eleserver.controller;

import com.zxylearn.eleserver.service.AmapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AmapController {

    @Autowired
    private AmapService amapService;

    @GetMapping("/getFormattedAddress")
    public String getFormattedAddress(@RequestParam double longitude, @RequestParam double latitude) {
        return amapService.getFormattedAddress(longitude, latitude);
    }

}
