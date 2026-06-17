package org.example.kb7spring.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
// JUnit 테스트에서 스프링 프로젝트의 컨텍스트를 사용 가능하게 설정 > 스프링 프로젝트의 BEAN 주입 가능
@WebAppConfiguration
// 테스트 환경에 가짜 ServletContext를 주입, 현재 우리 프로젝트는 RootConfig 가 ServletConfig 를 임포트 하기 때문에 필요
// 단순, 서비스 레이어 테스트만 하는 경우 ServletContext 를 불러올 필요는 없으므로 경량화 테스트를 위해서는 빼는게 맞음
@ContextConfiguration(classes = RootConfig.class)
// 테스트하는 Context 를 어떤 환경으로 띄울지 결정, 우리는 RootConfig 가 적용된 환경에서 테스트를 해야하므로 필요
@Slf4j
class RootConfigTest {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void sqlSessionFactory() {
        try (SqlSession session = sqlSessionFactory.openSession();
             Connection con = session.getConnection()) {
            log.info("SqlSession: {}", session);
            log.info("Connection: {}", con);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}