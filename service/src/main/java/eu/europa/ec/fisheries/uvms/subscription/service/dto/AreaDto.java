package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.constraints.NotNull;

import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import lombok.Data;

/**
 * Configuration of an area object.
 */
@Data
public class AreaDto {

    private Long id;

    @NotNull
    private Long gid;

    @NotNull
    private AreaType areaType;
}
