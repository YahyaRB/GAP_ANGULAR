package ma.gap.entity;


import ma.gap.repository.AffectationUpdateRepository;
import ma.gap.repository.AtelierRepository;
import ma.gap.service.AffectationUpImpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Component
public class MailAutoComponent {


    @Autowired
    AffectationUpImpService affectationUpImpService ;

    @Autowired
    AtelierRepository atelierRepository;

    @Autowired
    AffectationUpdateRepository affectationUpdateRepository;

    private static final Logger log = LoggerFactory.getLogger(MailAutoComponent.class);

    LocalDate now = LocalDate.now();

    @Scheduled(fixedDelay = 5000)
    public void reportCurrentTime() {

        {
            try {
                List<Ateliers> ateliersList = atelierRepository.findAll();

                for (Ateliers ateliers : ateliersList){
                    List<AffectationUpdate> affectationUpdateList = affectationUpdateRepository.findAllByAteliersOrderByIdDesc(ateliers);
                    AffectationUpdate affectationUpdate = affectationUpdateList.get(0);
                    SimpleDateFormat StringDate = new SimpleDateFormat("yyyy-MM-dd");

                    Date affDate = affectationUpdate.getCreatedDate();

                    Date nowDate = StringDate.parse(String.valueOf(now));
                    long diffDate= nowDate.getTime()-affDate.getTime();

                    if (diffDate/(1000*60*60)>=48 && diffDate/(1000*60*60)<96){
                        System.out.println("aucun affectation ajouté depuis deux jours ==> chef Projet ");
//                        affectationUpImpService.sendEmailAgentSaisi(affectationUpdate);
                        log.info("mail sent to Agent");
                    }else if (diffDate/(1000*60*60)>=96){
                        System.out.println("aucun affectation ajouté depuis 4 jours ==> chef Projet/DAF");
//                        affectationUpImpService.sendEmailConsulteur(affectationUpdate);
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }








}
