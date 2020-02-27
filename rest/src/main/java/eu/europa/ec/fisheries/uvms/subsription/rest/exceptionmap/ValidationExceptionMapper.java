package eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import eu.europa.ec.fisheries.uvms.commons.rest.constants.ErrorCodes;
import eu.europa.ec.fisheries.uvms.commons.rest.dto.ResponseDto;

/**
 * Map the {@code ValidationException}.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
	@Override
	public Response toResponse(ValidationException exception) {
		// copied from the ValidationInterceptor
		ResponseDto<?> dto = new ResponseDto<>(HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.INPUT_VALIDATION_FAILED);
		return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(dto).build();
	}
}
