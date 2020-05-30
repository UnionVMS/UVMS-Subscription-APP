package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.constraints.NotNull;

import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration of an area object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AreaDto {

    private Long id;

    @NotNull
    private Long gid;

    @NotNull
    private AreaType areaType;
}
