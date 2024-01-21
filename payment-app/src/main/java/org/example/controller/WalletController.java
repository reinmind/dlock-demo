package org.example.controller;

import org.example.dao.WalletDAO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Resource
    WalletDAO walletDAO;

    @PostMapping("/topup")
    public String topUp(@RequestBody Map<Long,Long> requestMap){
        for (Map.Entry<Long,Long> entry:requestMap.entrySet()){
            walletDAO.setBalace(entry.getKey(), entry.getValue().intValue());
        }
        return "success";
    }

    @GetMapping("/getBalance")
    public Integer getUserBalance(@RequestParam Long userId){
        return walletDAO.getBalance(userId);
    }
}
