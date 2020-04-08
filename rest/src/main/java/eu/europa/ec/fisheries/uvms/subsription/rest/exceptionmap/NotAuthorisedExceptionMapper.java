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
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import eu.europa.ec.fisheries.uvms.commons.rest.constants.ErrorCodes;
import eu.europa.ec.fisheries.uvms.commons.rest.dto.ResponseDto;
import eu.europa.fisheries.uvms.subscription.model.exceptions.NotAuthorisedException;

/**
 * Map the {@link NotAuthorisedException} to an HTTP 403 response.
 */
public class NotAuthorisedExceptionMapper implements ExceptionMapper<NotAuthorisedException> {
	@Override
	public Response toResponse(NotAuthorisedException exception) {
		ResponseDto<String> dto = new ResponseDto<>(exception.getMessage(), HttpServletResponse.SC_FORBIDDEN, ErrorCodes.NOT_AUTHORIZED);
		return Response.status(HttpServletResponse.SC_FORBIDDEN).entity(dto).build();
	}
}
