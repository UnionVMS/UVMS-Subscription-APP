package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/**
 * Capture the triggering data of a triggered subscription instance.
 */
@Entity
@IdClass(TriggeredSubscriptionDataId.class)
@Table(name = "triggered_subscription_data")
@Getter
@Setter
public class TriggeredSubscriptionDataEntity {
	@Id
	@ManyToOne
	@JoinColumn(name = "trig_subscription_id")
	private TriggeredSubscriptionEntity triggeredSubscription;

	@Id
	@Column(name = "key")
	private String key;

	@Column(name = "value")
	private String value;

	@Override
	public int hashCode() {
		return Objects.hash(key, value, triggeredSubscription != null ? triggeredSubscription.getId() : null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TriggeredSubscriptionDataEntity that = (TriggeredSubscriptionDataEntity) o;
		return (triggeredSubscription == that.getTriggeredSubscription() || (triggeredSubscription != null && that.triggeredSubscription != null && Objects.equals(triggeredSubscription.getId(), that.getTriggeredSubscription().getId()))) &&
				Objects.equals(key, that.key) &&
				Objects.equals(value, that.value);
	}
}
