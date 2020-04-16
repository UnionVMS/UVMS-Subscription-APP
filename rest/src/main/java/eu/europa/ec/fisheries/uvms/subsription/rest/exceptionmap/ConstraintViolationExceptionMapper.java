/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import eu.europa.ec.fisheries.uvms.commons.rest.constants.ErrorCodes;
import eu.europa.ec.fisheries.uvms.commons.rest.dto.ResponseDto;

/**
 * Map the {@code ValidationException}.
 */
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
	@Override
	public Response toResponse(ConstraintViolationException exception) {
		Set<ConstraintViolationDto> data = exception.getConstraintViolations().stream()
				.map(this::toConstraintViolationDto)
				.collect(Collectors.toSet());
		ResponseDto<Set<ConstraintViolationDto>> dto = new ResponseDto<>(data, HttpServletResponse.SC_BAD_REQUEST, ErrorCodes.INPUT_VALIDATION_FAILED);
		return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(dto).build();
	}

	private ConstraintViolationDto toConstraintViolationDto(ConstraintViolation<?> violation) {
		List<Object> path = StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
				.map(n -> {
					switch (n.getKind()) {
						case BEAN:
							return null;
						case METHOD:
							return null;
						case PROPERTY:
							if (n.isInIterable()) {
								return n.getIndex() != null ? n.getIndex() : n.getKey();
							}
							return n.getName();
						case PARAMETER:
							return null;
						case CONSTRUCTOR:
							return null;
						case RETURN_VALUE:
							return null;
						case CROSS_PARAMETER:
							return null;
						case CONTAINER_ELEMENT:
							return n.getName();
						default:
							throw new IllegalStateException("unknown path kind: " + n.getKind());
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		return new ConstraintViolationDto(violation.getMessage(), path);
	}
}
