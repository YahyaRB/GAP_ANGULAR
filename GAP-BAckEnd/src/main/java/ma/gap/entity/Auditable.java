package ma.gap.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import javax.persistence.*;
import java.util.Date;


@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Auditable<U> {

    @CreatedBy
    @Column(name = "Creer_par")
    private U createdBy;

    @CreatedDate
    @Column(name = "Creer_le")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @LastModifiedBy
    @Column(name = "Modifier_par")
    private U lastModifiedBy;

    @LastModifiedDate
    @Column(name = "Modifier_le")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

}
