package org.example.kb7spring.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;

@Configuration
@PropertySource({"classpath:/application.properties"})
@MapperScan(basePackages = {"org.example.kb7spring"})
@ComponentScan(basePackages = {"org.example.kb7spring"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = Controller.class
        )
)
public class RootConfig {
    @Value("${jdbc.driver}") String driver;
    @Value("${jdbc.url}") String url;
    @Value("${jdbc.username}") String username;
    @Value("${jdbc.password}") String password;

        @Bean
        public DataSource dataSource() {
                HikariConfig config = new HikariConfig();
                config.setDriverClassName(driver);
                config.setJdbcUrl(url);
                config.setUsername(username);
                config.setPassword(password);
                HikariDataSource dataSource = new HikariDataSource(config);
                return dataSource;
        }

        @Autowired
        ApplicationContext applicationContext;

        // MyBatis 가 DB 와 통신하기 위한 SqlSessionFactory 인스턴스를 생성하기 위한 Bean
        @Bean
        public SqlSessionFactory sqlSessionFactory() throws Exception {
                SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
                sqlSessionFactory.setConfigLocation(
                        applicationContext.getResource("classpath:/mybatis-config.xml"));
                sqlSessionFactory.setDataSource(dataSource());
                return (SqlSessionFactory) sqlSessionFactory.getObject();
        }

        // 위에서 설정한 DataSource 를 받아서 트랜잭션을 관리하는 매니저를 등록하는 Bean
        // 이 매니저 등록이 없으면 스프링 내부에서 @Transactional 어노테이션 사용 불가
        @Bean
        public DataSourceTransactionManager transactionManager(){
                DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());
                return manager;
        }

}
