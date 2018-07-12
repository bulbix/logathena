package com.bancoazteca.logathena;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class AthenaConfig {

    @Value("${athena.driverClassName}")
    String driverClassName;
    @Value("${athena.url}")
    String url;
    @Value("${athena.userName}")
    String userName;
    @Value("${athena.password}")
    String password;
    @Value("${athena.s3OutputLocation}")
    String s3OutputLocation;

    @Bean
    @Primary
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        Properties props = new Properties();
        props.setProperty("s3OutputLocation",s3OutputLocation);
        props.setProperty("ProxyHost","10.50.8.20");
        props.setProperty("ProxyPort","8080");
        dataSource.setConnectionProperties(props);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setResultsMapCaseInsensitive(true);
        return jdbcTemplate;
    }


}