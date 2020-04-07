package eu.europa.ec.fisheries.uvms.subsription.rest.exceptionmap;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objects of this class communicate constraint violations (validity errors) to the client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintViolationDto {
	/**
	 * The violation message.
	 */
	private String message;
	/**
	 * Path to the invalid element. Elements may be strings (property names, map keys) or integer (indexes).
	 */
	private List<Object> path;
}
