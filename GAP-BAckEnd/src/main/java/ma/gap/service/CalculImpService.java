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
public class CalculImpService implements CalculService {

    private AffectationUpdateRepository affectationUpdateRepository;

    private AtelierImpService atelierImpService;

    private ProjetRepository projetRepository;

    private EmployeeRepository employeeRepository;

    private RestTokenController restTokenController;

    @Override
    public List<CalculPerProjet> getAffectationPerProject(Long id, Integer month, Integer year) {

        Ateliers atelier = atelierImpService.getAtelierById(id);
        Map<Long, CalculPerProjet> map = new HashMap<>();
        // Map<ProjectId, Map<EmployeeId, Hours>>
        Map<Long, Map<Long, Integer>> projectEmpHours = new HashMap<>();

        List<Object[]> calculPerProject = affectationUpdateRepository.listCalculPerProject(id, month, year);
        calculPerProject.forEach(projet -> {
            BigInteger prId = (BigInteger) projet[0];
            Long projectId = prId.longValue();

            Integer count = (Integer) projet[1];
            String periode = projet[2].toString();
            Integer unitHours = 0;

            if ("Matin".equals(periode)) {
                unitHours = 5;
            } else if ("Après-midi".equals(periode)) {
                unitHours = 4;
            }

            Integer totalGroupHours = count * unitHours;

            CalculPerProjet calcul = map.getOrDefault(projectId, new CalculPerProjet());
            if (calcul.getProjet() == null) {
                Projet p = projetRepository.findById(projectId).orElse(null);
                calcul.setProjet(p);
                calcul.setAtelier(atelier);
                calcul.setHeureTrav(0);
                calcul.setEmployesCalculs(new ArrayList<>());
            }

            calcul.setHeureTrav(calcul.getHeureTrav() + totalGroupHours);

            // Employees
            if (projet[4] != null) {
                List<String> employeeIdStrs = Stream.of(projet[4].toString().split(",")).collect(Collectors.toList());
                Map<Long, Integer> empMap = projectEmpHours.computeIfAbsent(projectId, k -> new HashMap<>());

                for (String empIdStr : employeeIdStrs) {
                    if (!empIdStr.trim().isEmpty()) {
                        try {
                            Long empId = Long.valueOf(empIdStr.trim());
                            empMap.merge(empId, unitHours, Integer::sum);
                        } catch (NumberFormatException e) {
                            // Ignore invalid IDs
                        }
                    }
                }
            }

            map.put(projectId, calcul);
        });

        List<CalculPerProjet> list = new ArrayList<>(map.values());

        Integer sommeHeures = 0;
        for (CalculPerProjet c : list) {
            sommeHeures += c.getHeureTrav();
        }

        for (CalculPerProjet calcul : list) {
            calcul.setTotalHeur(sommeHeures);
            if (sommeHeures > 0) {
                calcul.setPourcHeur((float) calcul.getHeureTrav() * 100 / sommeHeures);
            } else {
                calcul.setPourcHeur(0);
            }

            // Populate EmployeeCalculs
            Map<Long, Integer> empMap = projectEmpHours.get(calcul.getProjet().getId());
            if (empMap != null && !empMap.isEmpty()) {
                List<Employee> employees = employeeRepository.findAllById(empMap.keySet());
                List<EmployeeCalcul> empCalculs = new ArrayList<>();
                for (Employee e : employees) {
                    EmployeeCalcul ec = new EmployeeCalcul();
                    ec.setId(e.getId());
                    ec.setMatricule(e.getMatricule());
                    ec.setNom(e.getNom());
                    ec.setPrenom(e.getPrenom());

                    Integer h = empMap.get(e.getId());
                    ec.setHeures(h);

                    if (calcul.getHeureTrav() > 0) {
                        ec.setPourcentage((float) h * 100 / calcul.getHeureTrav());
                    } else {
                        ec.setPourcentage(0);
                    }
                    empCalculs.add(ec);
                }
                calcul.setEmployesCalculs(empCalculs);
            }
        }

        return list;
    }

    @Override
    public List<BiotimeInfo> bioTimeListEmp() throws JsonProcessingException, ParseException {
        List<Object> listData = new ArrayList<>();
        List<BiotimeInfo> listemp = new ArrayList<>();
        int page = 1;
        JSONObject jsonObject = new JSONObject(restTokenController.getProrataEmploye(page));
        Integer count = jsonObject.getInt("count");
        DateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter sourceFormatTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ;

        int totalPage = (count / 10) + 1;

        for (int j = 1; j <= totalPage; j++) {
            page = j;
            JSONObject jsonObject1 = new JSONObject(restTokenController.getProrataEmploye(page));
            JSONArray jsonArray = jsonObject1.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                String jsonArray1 = jsonObject2.getString("emp_code");
                String jsonDate = jsonObject2.getString("punch_time");

                BiotimeInfo emp = new BiotimeInfo();
                emp.setEmp_code(jsonArray1);
                emp.setDate(sourceFormat.parse(jsonDate));
                emp.setMaxPunch(LocalDateTime.parse(jsonDate, sourceFormatTime).toLocalTime());
                emp.setMinPunch(LocalDateTime.parse(jsonDate, sourceFormatTime).toLocalTime());

                listemp.add(emp);
                listData.add(jsonArray.get(i));

            }
        }

        return listemp;
    }
}
