package me.sample.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ApiImplicitParams({
        @ApiImplicitParam(name = "appBundle", required = true, dataType = "string", paramType = "header",
                value = "${terminalApi.appBundle}"),
        @ApiImplicitParam(name = "platform", required = true, dataType = "string", paramType = "header",
                value = "${terminalApi.platform}"),
        @ApiImplicitParam(name = "vendor", dataType = "string", paramType = "header",
                value = "${terminalApi.vendor}"),
        @ApiImplicitParam(name = "model", dataType = "string", paramType = "header",
                value = "${terminalApi.model}"),
        @ApiImplicitParam(name = "osver", dataType = "string", paramType = "header",
                value = "${terminalApi.osVersion}"),
        @ApiImplicitParam(name = "version", dataType = "string", paramType = "header",
                value = "${terminalApi.appVersion}"),
        @ApiImplicitParam(name = "hwid", required = true, dataType = "string", paramType = "header",
                value = "${terminalApi.hardwareId}"),
        @ApiImplicitParam(name = "phone", dataType = "string", paramType = "header",
                value = "${terminalApi.msisdn}")
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TerminalApi {
}
