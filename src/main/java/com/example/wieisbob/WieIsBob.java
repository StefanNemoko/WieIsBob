package com.example.wieisbob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WieIsBob {

  static void main(String[] args) {
    SpringApplication.run(WieIsBob.class, args);
  }

}
