//package ma.gap.entity;
//
//import java.io.Serializable;
//import java.util.Date;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.EnumType;
//import javax.persistence.Enumerated;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
//
//import org.springframework.format.annotation.DateTimeFormat;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import ma.gap.enums.TypeNomenclature;
//@Entity(name = "Nomenclature")
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class Nomenclature extends Auditable<String> implements Serializable{
//		
//		@Id
//	    @GeneratedValue(strategy = GenerationType.IDENTITY)
//	    private Long id;
//
//	    @Enumerated(EnumType.STRING)
//	    private TypeNomenclature type ;
//	
////	    @Column(name = "ref")
////	    private String ref;
////
////	    @Column(name = "designation")
////	    private String designation;
//	    
//	    @Column(name = "finition")
//	    private String finition;
//	    
//	    @Column(name="longueur")
//	    private Float longueur;
//	    
//	    @Column(name = "largeur")
//	    private Float largeur;
//	    
//	    @Column(name = "epaisseur")
//	    private Float epaisseur;
//	    
////	    @Column(name = "dateNomenclature")
////	    @DateTimeFormat(pattern = "yyyy-MM-dd")
////	    private Date date;
//
//	    @Column(name = "quantite")
//	    private Float quantite;
//	    
//	    @Column(name = "unite")
//	    private String unite;
//	    
//
//	    @ManyToOne
//	    @JoinColumn(name = "article_ach")
//	    private ArticleAch articleAch;
//
//	    @ManyToOne
//	    @JoinColumn(name = "numero_plan_id")
//	    private Plan numeroPlan;
//
//
//}





package ma.gap.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ma.gap.enums.TypeNomenclature;

@Entity(name = "Nomenclature")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Nomenclature extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unite;
    
    @Enumerated(EnumType.STRING)
    private TypeNomenclature type ;

    @ManyToOne
    @JoinColumn(name = "numero_plan_id")
    private Plan numeroPlan;
}
//@ManyToOne
//@JoinColumn(name = "article_ach")
//private ArticleAch articleAch;
