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

        @Bean
        public SqlSessionFactory sqlSessionFactory() throws Exception {
                SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
                sqlSessionFactory.setConfigLocation(
                        applicationContext.getResource("classpath:/mybatis-config.xml"));
                sqlSessionFactory.setDataSource(dataSource());
                return (SqlSessionFactory) sqlSessionFactory.getObject();
        }

        @Bean
        public DataSourceTransactionManager transactionManager(){
                DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());
                return manager;
        }

}
