package com.awspractice.sb_01;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetController {
    @GetMapping("/greet")
    public String greet(){
        return "Welcome to AWS";
    }

}
