/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.fisheries.uvms.subscription.model.exceptions;

/**
 * Exception thrown when the current user is not allowed to access a resource or perform an operation.
 */
public class NotAuthorisedException extends ApplicationSecurityException {
	public NotAuthorisedException() {
	}

	public NotAuthorisedException(String message) {
		super(message);
	}

	public NotAuthorisedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotAuthorisedException(Throwable cause) {
		super(cause);
	}
}
