package com.transitassist.transitassist.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Runs the whole app against an in-memory H2 database (seeded by DataSeeder). Each test rolls back. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class AbstractApiTest {

    @Autowired
    protected MockMvc mvc;
}
