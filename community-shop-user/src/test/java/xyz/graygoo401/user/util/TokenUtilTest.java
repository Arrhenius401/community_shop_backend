package xyz.graygoo401.user.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.graygoo401.common.util.TokenUtil;

@SpringBootTest
public class TokenUtilTest {

    @Autowired
    private TokenUtil tokenUtil;

    @Test
    public void testGenerateToken() {
        System.out.println(tokenUtil.generateToken(1l));
    }

    @Test
    public void testValidateToken() {
        tokenUtil.validateToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiIxNSIsInN1YiI6IlVTRVIiLCJpYXQiOjE3NjE4OTA3OTEsImV4cCI6MTc2MTg5NDM5MSwianRpIjoiN2FlOWM2OTItYWJmOS00MjM0LTk4MTctZTkyMTc1OWUwN2U4In0.UJvJMwAygLg_4rZw88KrG7_u1H-8yL0bJEr91TBMk5U");
    }
}
