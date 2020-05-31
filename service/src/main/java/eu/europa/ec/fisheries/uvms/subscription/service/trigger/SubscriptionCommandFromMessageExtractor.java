/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;

/**
 * Translates a textual message representation from a specific source to a
 * stream of commands.
 * <p>
 * The purpose of this class is to allow a message to be handled as a stream and to
 * abstract the handling of messages from a specific source.
 */
public interface SubscriptionCommandFromMessageExtractor {
	/**
	 * Get the name of the subscription source that this facility can handle.
	 */
	String getEligibleSubscriptionSource();

	/**
	 * Extract the commands from this kind of message.
	 *
	 * @param representation The representation of the message as string, as it arrived in the messaging facilities
	 * @return A possibly empty but never null stream of commands to execute in order to process this message
	 */
	Stream<Command> extractCommands(String representation);
}
