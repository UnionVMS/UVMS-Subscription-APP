/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static javax.persistence.EnumType.STRING;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A fishing activity configured as start or stop condition for a subscription.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionFishingActivity implements Serializable {
	@Enumerated(STRING)
	@Column(name = "type")
	@NotNull
	private SubscriptionFaReportDocumentType type;

	@Column(name = "value")
	@NotBlank
	private String value;
}
