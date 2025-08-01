package com.community_shop.backend.controller;

import com.community_shop.backend.entity.LocalToken;
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
    public boolean checkToken(@RequestBody LocalToken localToken){
        boolean isValid = checkService.checkToken(localToken);

        return isValid;
    }

    @RequestMapping("/api/checkAdmin")
    public boolean checkAdmin(@RequestBody LocalToken localToken){
        boolean isValid = checkService.checkAdmin(localToken);

        return isValid;
    }
}
