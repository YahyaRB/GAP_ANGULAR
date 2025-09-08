package ma.gap.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter@Setter
public class BiotimeInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String emp_code;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime minPunch;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime maxPunch;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime difference;



}
