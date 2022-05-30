package me.sample.gateway.dadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.sample.config.gateway.DadataGatewayProperties;
import me.sample.gateway.GatewayException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import me.sample.gateway.dadata.response.ResponseError;

import java.io.IOException;
import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Component
public class DadataGatewayErrorHandler implements ResponseErrorHandler {

    DadataGatewayProperties dadataGatewayProperties;

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String message = Optional.ofNullable(objectMapper.readValue(response.getBody(), ResponseError.class).getMessage())
                .map((String it) -> it.replaceAll(dadataGatewayProperties.getApi().getAuth().getToken(), "<hidden>"))
                .orElse(response.getStatusText());

        throw new GatewayException(message);
    }
}
