package org.pinterest.analyzer;

import com.github.isrsal.logging.RequestWrapper;
import com.github.isrsal.logging.ResponseWrapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {

	protected static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

	private AtomicLong ATOMIC_LONG = new AtomicLong(1);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
		long requestId = ATOMIC_LONG.getAndIncrement();
		RequestWrapper requestWrapper = new RequestWrapper(requestId, request);
		ResponseWrapper responseWrapper = new ResponseWrapper(requestId, response);

		try {
			filterChain.doFilter(requestWrapper, responseWrapper);
		} finally {
			log(requestWrapper, responseWrapper);
		}
	}

	private void log(RequestWrapper requestWrapper, ResponseWrapper responseWrapper) {
		HttpStatus responseStatus = HttpStatus.valueOf(responseWrapper.getStatus());

		if (responseStatus.is4xxClientError() || responseStatus.is5xxServerError()) {
			logger.error("Failed request processed:\n REQUEST : \n {} \n RESPONSE : {}", renderRequest(requestWrapper),
					renderResponse(responseWrapper));
		}

		if (logger.isDebugEnabled() && responseStatus.is2xxSuccessful()) {
			logger.debug("Successful request processed:\n REQUEST : {} \n RESPONSE : {}", renderRequest(requestWrapper),
					renderResponse(responseWrapper));
		}
	}

	private String renderRequest(RequestWrapper request) {
		StringBuilder builder = new StringBuilder();

		if (!isMultipart(request)) {
			try {
				String charEncoding = request.getCharacterEncoding() != null ? request.getCharacterEncoding() :
						"UTF-8";
				builder.append("\n body : ").append(IOUtils.toString(request.toByteArray(), charEncoding));
			} catch (IOException e) {
				logger.error("Failed to parse request body", e);
			}
		}

		return builder.toString();
	}

	private String renderResponse(ResponseWrapper response) {
		StringBuilder builder = new StringBuilder();

		try {
			builder.append("\n body : ").append(IOUtils.toString(response.toByteArray(), response.getCharacterEncoding()));
		} catch (IOException e) {
			logger.warn("Failed to parse response body", e);
		}

		return builder.toString();
	}

	private boolean isMultipart(final HttpServletRequest request) {
		return request.getContentType() != null && request.getContentType().startsWith("multipart/form-data");
	}

}
