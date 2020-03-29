package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static javax.persistence.EnumType.STRING;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import lombok.Data;

/**
 * Encapsulates instructions for how and when to execute a subscription.
 */
@Embeddable
@Data
public class SubscriptionExecution implements Serializable {

    @Enumerated(STRING)
    @NotNull
    @Column(name = "trigger_type")
    private TriggerType triggerType;

	@Column(name = "frequency")
	@Min(0)
    private Integer frequency;

	@Column(name = "immediate")
	private Boolean immediate;

	@Column(name = "time_expr")
	private String timeExpression;
}
