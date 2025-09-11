package com.community_shop.backend.controller;

import com.community_shop.backend.dto.user.LoginResultDTO;
import com.community_shop.backend.service.CheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckTokenController {

    @Autowired
    private CheckService checkService;

    @RequestMapping("/api/checkToken")
    public boolean checkToken(@RequestBody LoginResultDTO loginResultDTO){
        boolean isValid = checkService.checkToken(loginResultDTO);

        return isValid;
    }

    @RequestMapping("/api/checkAdmin")
    public boolean checkAdmin(@RequestBody LoginResultDTO loginResultDTO){
        boolean isValid = checkService.checkAdmin(loginResultDTO);

        return isValid;
    }
}
