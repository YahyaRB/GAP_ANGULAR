package ma.gap.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import ma.gap.config.GlobalVariableConfig;
import ma.gap.entity.Deplacement;
import ma.gap.entity.OrdreFabrication;
import ma.gap.enums.StatutEntity;
import ma.gap.repository.DeplacementRepository;
import ma.gap.repository.OrdreFabricationRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;


@Service
@AllArgsConstructor
public class FilesStorageServiceImpl implements FilesStorageService {
    private GlobalVariableConfig globalVariableConfig;
    private OrdreFabricationRepository ordreFabricationRepository;
    private DeplacementRepository deplacementRepository;

    @Override
    public void init() {
        try {
            Path root = Paths.get(globalVariableConfig.getGlobalVariable());
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'initialiser le dossier pour le téléchargement !");
        }
    }

    @Override
    public void save(MultipartFile file) {
        try {

                Path dossierPiecesJointes = Paths.get(globalVariableConfig.getGlobalVariable());
                if (Files.notExists(dossierPiecesJointes)) {

                    Files.createDirectory(dossierPiecesJointes);
                }
                String pieceJointe = null;

                if(file.getSize()!=0)
                {
                    pieceJointe = UUID.randomUUID().toString();
                    String extension = "."+FilenameUtils.getExtension(file.getOriginalFilename());
System.out.println(pieceJointe+ extension);

                    Files.copy(file.getInputStream(), dossierPiecesJointes.resolve(pieceJointe+ extension));
                }

            } catch (Exception e) {
                if (e instanceof FileAlreadyExistsException) {
                    throw new RuntimeException("Un fichier de ce nom existe déjà.");
                }
                throw new RuntimeException(e.getMessage());
            }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path root = Paths.get(globalVariableConfig.getGlobalVariable());
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Impossible de lire le fichier !");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur: " + e.getMessage());
        }
    }


    @Override
    public boolean delete(String filename) {
        try {
            Path root = Paths.get(globalVariableConfig.getGlobalVariable());
            Path file = root.resolve(filename);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Erreur: " + e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        Path root = Paths.get(globalVariableConfig.getGlobalVariable());
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            Path root = Paths.get(globalVariableConfig.getGlobalVariable());
            return Files.walk(root, 1).filter(path -> !path.equals(root)).map(root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lire le fichier !");
        }
    }
    @Override
    public void updatePjOFById(long id,MultipartFile file) throws IOException {
        try{
            OrdreFabrication of=ordreFabricationRepository.findById(id).get();

                if (of.getPieceJointe() != null) {
                   delete(of.getPieceJointe());
                }
            Path dossierPiecesJointes = Paths.get(globalVariableConfig.getGlobalVariable());
            if (Files.notExists(dossierPiecesJointes)) {
                Files.createDirectory(dossierPiecesJointes);
            }
            String pieceJointe = null;

            if(file.getSize()!=0)
            {
                pieceJointe = UUID.randomUUID().toString();
                String extension = "."+FilenameUtils.getExtension(file.getOriginalFilename());
                of.setPieceJointe(pieceJointe+ extension);
                of.setId(id);
                ordreFabricationRepository.save(of);

                Files.copy(file.getInputStream(), dossierPiecesJointes.resolve(pieceJointe+ extension));
            }
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("Un fichier de ce nom existe déjà.");
            }
            throw new RuntimeException(e.getMessage());
        }
    }
    @Override
    public void savePjDeplacementById(long id,MultipartFile file) throws IOException {
        try{
            Deplacement dep=deplacementRepository.findById(id).get();
            System.out.println(dep.getPieceJointe());
            if (dep.getPieceJointe() != null) {
                delete(dep.getPieceJointe());
            }
            Path dossierPiecesJointes = Paths.get(globalVariableConfig.getGlobalVariable());
            if (Files.notExists(dossierPiecesJointes)) {

                Files.createDirectory(dossierPiecesJointes);
            }
            String pieceJointe = null;

            if(file.getSize()!=0)
            {
                pieceJointe = UUID.randomUUID().toString();
                String extension = "."+FilenameUtils.getExtension(file.getOriginalFilename());
                dep.setPieceJointe(pieceJointe+ extension);
                dep.setId(id);
                deplacementRepository.save(dep);

                Files.copy(file.getInputStream(), dossierPiecesJointes.resolve(pieceJointe+ extension));
            }
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("Un fichier de ce nom existe déjà.");
            }
            throw new RuntimeException(e.getMessage());
        }
    }
}
