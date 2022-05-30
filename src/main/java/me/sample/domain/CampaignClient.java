package me.sample.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "campaign_client")
@Entity
public class CampaignClient extends AbstractIdentifiable<CampaignClientId> {

    @Builder.Default
    @EmbeddedId
    CampaignClientId id = new CampaignClientId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("campaignId")
    Campaign campaign;

    public CampaignClient setClientId(String clientId) {
        id.setClientId(clientId);

        return this;
    }
}
