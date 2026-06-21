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
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@PropertySource({"classpath:/application.properties"})
@MapperScan(basePackages = {"org.example.kb7spring.member.mapper", "org.example.kb7spring.student.mapper"})
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
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/kb7spring");
                config.setUsername("root");
                config.setPassword("1234");
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

        // JPA EntityManagerFactory
        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
                LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
                emf.setDataSource(dataSource());
                emf.setPackagesToScan("org.example.kb7spring");

                HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
                adapter.setShowSql(true);
                emf.setJpaVendorAdapter(adapter);

                Properties props = new Properties();
                props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
                props.setProperty("hibernate.hbm2ddl.auto", "update");
                props.setProperty("hibernate.format_sql", "true");
                emf.setJpaProperties(props);

                return emf;
        }

        // JPA 트랜잭션 매니저 (MyBatis 의 DataSourceTransactionManager 를 대체)
        @Bean
        public JpaTransactionManager transactionManager() {
                return new JpaTransactionManager(entityManagerFactory().getObject());
        }

}
