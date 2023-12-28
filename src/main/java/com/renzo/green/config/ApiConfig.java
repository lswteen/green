package com.renzo.green.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

import java.time.YearMonth;

@OpenAPIDefinition(info = @Info(
        title = "OpenAPI Doc",
        description = "API 기능을 제공합니다.",
        version = "1.0"),
        servers = @Server(url = "/", description = "Default server url"))
@Configuration
public class ApiConfig {
    static {
        // ParameterObject Class 내부에 YearMonth Type 을 선언할 경우,
        // /v3/api-docs 호출시 발생하는 오류를 방지하기 위해 YearMonth Type을 SimpleType으로 표시하도록 설정합니다.
        SpringDocUtils.getConfig().addSimpleTypesForParameterObject(YearMonth.class);
    }
}
