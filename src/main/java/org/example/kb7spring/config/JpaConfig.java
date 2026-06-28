package org.example.kb7spring.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"org.example.kb7spring"})
@PropertySource("classpath:application.properties")
public class JpaConfig {
//    @Value("${jdbc.driver}")
//    private String driver;
//    @Value("${jdbc.url}")
//    private String url;
//    @Value("${jdbc.username}")
//    private String username;
//    @Value("${jdbc.password}")
//    private String password;
//    @Value("${hibernate.dialect}")
//    private String dialect;
//    @Value("${hibernate.hbm2ddl.auto}")
//    private String ddlAuto;
//    @Value("${hibernate.show_sql}")
//    private boolean showSql;
//    @Value("${hibernate.format_sql}")
//    private boolean formatSql;

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/kb7spring");
        config.setUsername("root");
        config.setPassword("1234");
        return new HikariDataSource(config);
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf =
                new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource()); // DB 연결 방법 전달
        emf.setPackagesToScan("org.example.kb7spring"); // Entity 를 읽을 패키지 설정
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
//        adapter.setShowSql(showSql); // JPA 실행 시, SQL 쿼리를 보여줄지 옵션
        emf.setJpaVendorAdapter(adapter);

        Properties props = new Properties();
        props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        props.setProperty("hibernate.hbm2ddl.auto", "update");
        props.setProperty("hibernate.show_sql", String.valueOf("true"));
        props.setProperty("hibernate.format_sql", String.valueOf("true"));
        emf.setJpaProperties(props);

        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}