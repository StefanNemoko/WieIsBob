package com.example.wieisbob;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    protected ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
}
