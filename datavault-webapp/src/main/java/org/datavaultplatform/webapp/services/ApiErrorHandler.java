package org.datavaultplatform.webapp.services;

import org.datavaultplatform.common.api.ApiError;
import org.datavaultplatform.webapp.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.rmi.ServerException;

public class ApiErrorHandler extends DefaultResponseErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiErrorHandler.class);

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = response.getStatusCode();

        if (statusCode == HttpStatus.FORBIDDEN) {
            logger.error("Attempted to call the broker but received a 403 - Forbidden");
            throw new ForbiddenException();
        }

        if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR){

            RestTemplate restTemplate = new RestTemplate();

            ResponseExtractor<ResponseEntity<ApiError>> responseExtractor =
                    new ResponseEntityResponseExtractor(ApiError.class, restTemplate);

            HttpEntity<ApiError> extractedData = responseExtractor.extractData(response);

            System.err.println("There has been an error while calling the broker Api, here is the stackTrace of the broker:");
            Exception ex = extractedData.getBody().getException();
            ex.printStackTrace();

            throw new ServerException("There has been an error while calling the broker Api:", ex);
        }
        super.handleError(response);
    }

    private Charset getCharset(ClientHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        MediaType contentType = headers.getContentType();
        return contentType != null ? contentType.getCharset() : null;
    }

    /**
     * Response extractor for {@link HttpEntity}.
     */
    private class ResponseEntityResponseExtractor<T> implements ResponseExtractor<ResponseEntity<T>> {

        private final HttpMessageConverterExtractor<T> delegate;

        public ResponseEntityResponseExtractor(Type responseType, RestTemplate restTemplate) {
            if (responseType != null && Void.class != responseType) {
                this.delegate = new HttpMessageConverterExtractor<T>(responseType, restTemplate.getMessageConverters());
            }
            else {
                this.delegate = null;
            }
        }

        @Override
        public ResponseEntity<T> extractData(ClientHttpResponse response) throws IOException {
            if (this.delegate != null) {
                T body = this.delegate.extractData(response);
                return new ResponseEntity<T>(body, response.getHeaders(), response.getStatusCode());
            }
            else {
                return new ResponseEntity<T>(response.getHeaders(), response.getStatusCode());
            }
        }
    }
}
