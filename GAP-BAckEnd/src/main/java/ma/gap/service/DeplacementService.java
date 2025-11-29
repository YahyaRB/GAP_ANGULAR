package ma.gap.service;

import ma.gap.entity.Deplacement;
import ma.gap.exceptions.OrdreMissionNotFoundException;
import net.sf.jasperreports.engine.JRException;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface DeplacementService {
        public Deplacement getById(long id);

        public List<Deplacement> allDeplacement(long idUser);

        public Deplacement saveDeplacement(Deplacement deplacement);

        public Deplacement editDeplacement(Deplacement deplacement, long id);

        public boolean deleteDeplacement(Long id) throws IOException;

        ResponseEntity<byte[]> generateOm(Long id)
                        throws JRException, FileNotFoundException, IOException, EmptyResultDataAccessException,
                        OrdreMissionNotFoundException;

        ResponseEntity<byte[]> generateOmForAllEmployees(Long id)
                        throws JRException, FileNotFoundException, IOException, EmptyResultDataAccessException,
                        OrdreMissionNotFoundException;

        List<Deplacement> searchDeplacement(long idUser, long idemploye, long idprojet, long idatelier, String motif,
                        String dateDebut, String dateFin) throws ParseException;

        Page<Deplacement> allDeplacement(long idUser, Pageable pageable);

        Page<Deplacement> searchDeplacement(long idUser, long idemploye, long idprojet, long idatelier, String motif,
                        String dateDebut, String dateFin, Pageable pageable) throws ParseException;
}
