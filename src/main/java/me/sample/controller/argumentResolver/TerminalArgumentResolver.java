package me.sample.controller.argumentResolver;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import me.sample.dto.TerminalDTO;
import me.sample.domain.TerminalPlatform;

import javax.servlet.http.HttpServletRequest;

@Component
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TerminalArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String APP_BUNDLE_HEADER = "appBundle";
    private static final String PLATFORM_HEADER = "platform";
    private static final String VENDOR_HEADER = "vendor";
    private static final String MODEL_HEADER = "model";
    private static final String OS_VERSION_HEADER = "osver";
    private static final String APP_VERSION_HEADER = "version";
    private static final String HARDWARE_ID_HEADER = "hwid";
    private static final String PHONE_HEADER = "phone";
    String ipHeader;

    public TerminalArgumentResolver(@Value("${ip-header-name}") String ipHeader) {
        this.ipHeader = ipHeader;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(TerminalBind.class) != null;
    }

    @Override
    public TerminalDTO resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String appBundle = request.getHeader(APP_BUNDLE_HEADER);
        String platform = request.getHeader(PLATFORM_HEADER);
        String vendor = request.getHeader(VENDOR_HEADER);
        String model = request.getHeader(MODEL_HEADER);
        String osVersion = request.getHeader(OS_VERSION_HEADER);
        String appVersion = request.getHeader(APP_VERSION_HEADER);
        String hardwareId = request.getHeader(HARDWARE_ID_HEADER);
        String msisdn = request.getHeader(PHONE_HEADER);
        String ip = request.getHeader(ipHeader);

        return TerminalDTO.builder()
                .appBundle(appBundle)
                .platform(TerminalPlatform.of(platform))
                .vendor(vendor)
                .model(model)
                .osVersion(osVersion)
                .appVersion(appVersion)
                .hardwareId(hardwareId)
                .msisdn(msisdn)
                .ip(ip)
                .build();
    }
}
