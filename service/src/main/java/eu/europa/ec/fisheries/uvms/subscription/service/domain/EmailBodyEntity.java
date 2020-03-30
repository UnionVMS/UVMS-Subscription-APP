package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "email_body")
public class EmailBodyEntity implements Serializable {

    @Id
    @OneToOne()
    @JoinColumn(name = "subscription_id")
    private SubscriptionEntity subscription;

    @Column(name =  "body")
    @Lob
    private String body;
}
