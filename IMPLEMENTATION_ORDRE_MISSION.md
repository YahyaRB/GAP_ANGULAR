# Implémentation : Génération de PDF d'Ordre de Mission par Employé

## Objectif
Générer un PDF d'ordre de mission pour chaque employé d'un déplacement :
- **Si 1 seul employé** : télécharge un seul PDF
- **Si plusieurs employés** : télécharge un fichier ZIP contenant un PDF par employé

## Modifications apportées

### 1. Backend (Java/Spring Boot)

#### A. Service - `DeplacementImpService.java`
**Nouvelle méthode** : `generateOmForAllEmployees(Long id)`
- Vérifie le nombre d'employés dans le déplacement
- Si 1 employé : utilise la méthode existante `generateOm()`
- Si plusieurs : crée un ZIP avec un PDF par employé
- Chaque PDF est nommé : `Ordre_Mission_{id}_{Nom}_{Prenom}.pdf`

**Localisation** : 
```
GAP-BAckEnd/src/main/java/ma/gap/service/DeplacementImpService.java
Lignes : 146-219
```

#### B. Interface - `DeplacementService.java`
**Ajout** : Signature de la nouvelle méthode dans l'interface
```java
ResponseEntity<byte[]> generateOmForAllEmployees(Long id) 
    throws JRException, FileNotFoundException, IOException, 
           EmptyResultDataAccessException, OrdreMissionNotFoundException;
```

**Localisation** : 
```
GAP-BAckEnd/src/main/java/ma/gap/service/DeplacementService.java
Lignes : 25-26
```

#### C. Controller - `DeplacementController.java`
**Nouvel endpoint** : `GET /api/dpl/Deplacement/ImprimerTous/{id}`
- Autorisé pour les rôles : `admin`, `agentSaisie`
- Appelle : `deplacementImpService.generateOmForAllEmployees(id)`

**Localisation** : 
```
GAP-BAckEnd/src/main/java/ma/gap/controller/DeplacementController.java
Lignes : 135-153
```

### 2. Frontend (Angular)

#### A. Service - `deplacement.service.ts`
**Nouvelle méthode** : `downloadOrdreMission(deplacementId: number)`
- Effectue un appel GET vers `/api/dpl/Deplacement/ImprimerTous/{id}`
- Type de réponse : `blob`
- Détecte automatiquement le type (PDF ou ZIP) selon le Content-Type
- Télécharge le fichier avec le nom approprié

**Localisation** : 
```
GAP-FrontEnd/src/app/services/deplacement.service.ts
Lignes : 101-140
```

#### B. Component - `liste-deplacements.component.ts`
**Nouvelle méthode** : `downloadOrdreMission(deplacement: Ideplacement)`
- Valide le déplacement
- Appelle le service : `deplacementService.downloadOrdreMission()`

**Localisation** : 
```
GAP-FrontEnd/src/app/Components/Deplacements/liste-deplacements/liste-deplacements.component.ts
Lignes : 141-151
```

#### C. Template - `liste-deplacements.component.html`
**Nouveau bouton** dans la colonne Actions :
```html
<a *ngIf="hasRoleGroup(ROLES_ADMIN_AGENTSAISIE)"
   title="Télécharger l'ordre de mission (PDF pour chaque employé)"
   (click)="downloadOrdreMission(post)"
   class="action-btn">
  <i class="bx bxs-file-pdf icon-large text-danger"></i>
</a>
```

**Localisation** : 
```
GAP-FrontEnd/src/app/Components/Deplacements/liste-deplacements/liste-deplacements.component.html
Lignes : 206-212
```

## Comment tester

### 1. Démarrer le backend
```bash
cd GAP-BAckEnd
mvn spring-boot:run
```

### 2. Vérifier que le backend est accessible
L'API devrait être disponible à : `http://localhost:8083/GAP-UI/`

### 3. Depuis l'interface
1. Accéder à la page "Liste Déplacements"
2. Cliquer sur l'icône PDF rouge dans la colonne Actions
3. Le téléchargement commence automatiquement :
   - **1 employé** → Fichier : `Ordre_Mission_{id}.pdf`
   - **Plusieurs employés** → Fichier : `Ordres_Mission_{id}.zip`

## Résolution des problèmes

### Erreur HTTP lors du téléchargement

**Cause possible** : Le backend n'est pas démarré ou l'endpoint n'est pas accessible

**Solutions** :
1. Vérifier que le backend est en cours d'exécution
2. Vérifier l'URL de l'API dans la console réseau :
   - Doit être : `http://localhost:8083/GAP-UI/api/dpl/Deplacement/ImprimerTous/{id}`
3. Vérifier les logs du backend pour les erreurs

### Le fichier OrdreMission.jrxml n'est pas trouvé

**Cause** : Le fichier JRXML n'est pas présent dans le classpath

**Solution** :
1. Vérifier que le fichier existe : `GAP-BAckEnd/src/main/resources/files/OrdreMission.jrxml`
2. Recompiler le projet : `mvn clean compile`

### Erreur lors de la génération du PDF

**Cause possible** : Données manquantes ou incorrectes

**Solution** :
1. Vérifier que le déplacement contient au moins un employé
2. Vérifier que chaque employé a un nom, prénom et fonction
3. Consulter les logs du backend pour plus de détails

## Fichiers modifiés

### Backend
- ✅ `DeplacementImpService.java` - Nouvelle méthode de génération multiple
- ✅ `DeplacementService.java` - Signature de l'interface
- ✅ `DeplacementController.java` - Nouvel endpoint

### Frontend
- ✅ `deplacement.service.ts` - Méthode de téléchargement
- ✅ `liste-deplacements.component.ts` - Méthode d'appel
- ✅ `liste-deplacements.component.html` - Bouton PDF

## État actuel

✅ **Backend compilé avec succès** (mvn clean compile)
⚠️ **Backend à démarrer** pour tester la fonctionnalité
⏳ **Test en attente**
