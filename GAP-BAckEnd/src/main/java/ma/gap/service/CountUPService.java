package ma.gap.service;

import ma.gap.entity.Ateliers;
import ma.gap.entity.CountUP;
import ma.gap.repository.CountUPRepository;

import java.util.Calendar;
import java.util.Date;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CountUPService {

    private CountUPRepository countUPRepository;


    public Integer saveCountUP(Date dateOf,Ateliers atelier){
    	int count = 1;
    	Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateOf);
        int atelierNum = Long.valueOf(atelier.getId()).intValue();
        CountUP countUP = countUPRepository.findByAnneeAndAtelier(calendar.get(Calendar.YEAR),atelierNum);
        if (countUP==null){

            CountUP countUP1 = new CountUP();
            countUP1.setAnnee(calendar.get(Calendar.YEAR));
            countUP1.setCountNum(count);
            countUP1.setAtelier(atelierNum);
            countUPRepository.save(countUP1);
        }
        if (countUP!=null){

            count = countUP.getCountNum()+1;
            countUP.setCountNum(count);
            countUPRepository.save(countUP);
        }
        return count;
    }
}
