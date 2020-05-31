/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

/**
 * Service that handles incoming messages.
 */
public interface IncomingDataMessageService {
	/**
	 * Handle an incoming message.
	 *
	 * @param subscriptionSource The source of this message (movement, activity etc)
	 * @param representation The representation of this message, to be passed to an appropriate {@link SubscriptionCommandFromMessageExtractor}
	 */
	void handle(String subscriptionSource, String representation);
}
