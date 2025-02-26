package com.example.backend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.example.backend.typeHandler.IntegerArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// mybatis plus 分页拦截器
@Configuration
public class MybatisPlusConfiguration {
    @Bean
    public MybatisConfiguration mybatisConfiguration() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(List.class, JdbcType.ARRAY, new IntegerArrayTypeHandler());
        return configuration;
    }
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
//        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
//        paginationInnerInterceptor.setDbType(DbType.POSTGRE_SQL);
//        paginationInnerInterceptor.setOverflow(true);
//        interceptor.addInnerInterceptor(paginationInnerInterceptor);
//        return interceptor;

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }


//    @Bean
//    public MapperScannerConfigurer mapperScannerConfigurer(){
//        MapperScannerConfigurer scannerConfigurer = new MapperScannerConfigurer();
//        //可以通过环境变量获取你的mapper路径,这样mapper扫描可以通过配置文件配置了
//        scannerConfigurer.setBasePackage("com.example.backend.mapper");
//
//        return scannerConfigurer;
//    }
}
