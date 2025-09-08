package ma.gap.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.AllArgsConstructor;
import ma.gap.controller.RestTokenController;
import ma.gap.entity.*;
import ma.gap.repository.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class CalculImpService implements CalculService{


    private AffectationUpdateRepository affectationUpdateRepository;

    private AtelierImpService atelierImpService;

    private ProjetRepository projetRepository;

    private EmployeeRepository employeeRepository;

    private RestTokenController restTokenController;


    @Override
    public List<CalculPerProjet> getAffectationPerProject(Long id,Integer month,Integer year) {

        Ateliers atelier = atelierImpService.getAtelierById(id);
        List<CalculPerProjet> list = new ArrayList<>();

       List<Object[]> calculPerProject = affectationUpdateRepository.listCalculPerProject(id,month,year);
       calculPerProject.forEach(projet ->{

            //Integer total=0;
            CalculPerProjet calcul = new CalculPerProjet();
            List<Long> empIdList=new ArrayList<>();
            BigInteger prId =(BigInteger) projet[0];
           Projet projet1 = projetRepository.findById(prId.longValue()).get();
           Integer heureTravMatin = ((Integer) projet[1])*5;
           Integer heureTravAM = ((Integer) projet[1])*4;




            calcul.setProjet(projet1);
            calcul.setAtelier(atelier);
            calcul.setCount((Integer) projet[1]);
            calcul.setPeriode(projet[2].toString());

            if (projet[2].toString().equals("Matin")){
                calcul.setHeureTrav(heureTravMatin);

            }else if (projet[2].toString().equals("Après-midi")){
                calcul.setHeureTrav(heureTravAM);
            }

           List<String> employeeId =Stream.of(projet[4].toString().split(",")).collect(Collectors.toList());
            System.out.println(employeeId+"--------------------------------------------------");
           for (String emp:employeeId){
               Optional<Employee> e = employeeRepository.findById(Long.valueOf(emp));
               //Fonction fonction = e.get().getFonction();
               empIdList.add(e.get().getId());
               calcul.setEmpHeurTrav(calcul.getHeureTrav()/employeeId.size());

           }
           List<Employee> employeeList=employeeRepository.findAllByIdIn(empIdList);


           calcul.setEmployes(employeeList);

            list.add(calcul);

       });
        Integer sommeHeures = 0;
        for (int i = 0; i < list.size(); i++){
            sommeHeures +=list.get(i).getHeureTrav();
        }

        for (CalculPerProjet calcul : list){
            calcul.setTotalHeur(sommeHeures);
            calcul.setPourcHeur((float) calcul.getHeureTrav()*100/sommeHeures);
            calcul.setPourcHeurEmp(calcul.getEmpHeurTrav()*100/ calcul.getHeureTrav());
        }


        return list;
    }

    @Override
    public List<BiotimeInfo> bioTimeListEmp() throws JsonProcessingException, ParseException {
        List<Object> listData=new ArrayList<>();
        List<BiotimeInfo> listemp=new ArrayList<>();
        int page=1;
        JSONObject jsonObject = new JSONObject(restTokenController.getProrataEmploye(page));
        Integer count = jsonObject.getInt("count");
        DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter sourceFormatTime =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");;



        int totalPage=(count/10)+1;

        for (int j=1;j<=totalPage;j++){
            page=j;
            JSONObject jsonObject1 = new JSONObject(restTokenController.getProrataEmploye(page));
            JSONArray jsonArray = jsonObject1.getJSONArray("data");
            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject2= jsonArray.getJSONObject(i);
                String jsonArray1 = jsonObject2.getString("emp_code");
                String jsonDate = jsonObject2.getString("punch_time");

                BiotimeInfo emp = new BiotimeInfo();
                emp.setEmp_code(jsonArray1);
                emp.setDate(sourceFormat.parse(jsonDate));
                emp.setMaxPunch(LocalDateTime.parse(jsonDate,sourceFormatTime).toLocalTime());
                emp.setMinPunch(LocalDateTime.parse(jsonDate,sourceFormatTime).toLocalTime());

                listemp.add(emp);
                listData.add(jsonArray.get(i));



            }
        }


        return listemp;
    }
}
