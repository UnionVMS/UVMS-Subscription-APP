/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap;

import eu.europa.ec.fisheries.uvms.commons.rest.dto.ResponseDto;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EmailException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Map the {@link EmailException} to an HTTP 500 response.
 */
public class EmailExceptionMapper implements ExceptionMapper<EmailException> {

	@Override
	public Response toResponse(EmailException exception) {
		ResponseDto<String> dto = new ResponseDto<>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,exception.getMessage());
		return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(dto).build();
	}
}
