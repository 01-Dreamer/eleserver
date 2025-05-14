package com.zxylearn.eleserver;

import com.zxylearn.eleserver.utils.EmailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class EleserverApplicationTests {

    @Autowired
    EmailUtil emailUtil;

    @Test
    void contextLoads() {


    }

}
