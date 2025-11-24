# Implémentation de la Pagination pour les Affectations

## Problème
Avec 70 615 affectations enregistrées, le chargement de toutes les données en mémoire cause des problèmes de performance.

## Solution
Implémenter la pagination côté backend et frontend, et supprimer les filtres de date par défaut.

## Modifications effectuées

### 1. Backend - Controller (AffectationController.java)
✅ **FAIT** - L'endpoint `/Search` a été modifié pour accepter les paramètres de pagination :
- `page` (défaut: 0)
- `size` (défaut: 10)

L'endpoint retourne maintenant un objet avec :
- `content`: Liste des affectations de la page
- `currentPage`: Numéro de la page actuelle
- `totalItems`: Nombre total d'éléments
- `totalPages`: Nombre total de pages

### 2. Backend - Repository (CustomAffectationRepository.java)
✅ **FAIT** - Ajout de la méthode :
```java
Page<AffectationUpdate> affectationFiltredPaginated(
    long idUser, long idprojet, long idemploye, long idarticle, 
    long idatelier, String dateDebut, String dateFin, Pageable pageable
) throws ParseException;
```

### 3. Backend - Repository Implementation (CustomAffectationImpRepository.java)
✅ **FAIT** - Implémentation de la méthode paginée avec :
- Mêmes critères de filtrage que la méthode non-paginée
- Support du tri
- Comptage du total d'éléments
- Retour d'un objet `Page<AffectationUpdate>`

### 4. Frontend - Service (affectation.service.ts)
✅ **FAIT** - Modification de `searchAffectation()` pour accepter :
- `page` (défaut: 0)
- `size` (défaut: 10)

Le type de retour est maintenant `Observable<any>` au lieu de `Observable<Iaffectation[]>`

### 5. Frontend - Component (liste-affectations.component.ts)
⚠️ **À CORRIGER** - Le fichier a été corrompu lors de la modification.

## Modifications à faire manuellement

### Dans liste-affectations.component.ts

#### 1. Supprimer les dates par défaut (ligne ~70)
```typescript
private initmyForm() {
  this.myFormSearch = this.formBuilder.group({
    idprojet: [],
    idemploye: [],
    idarticle: [],
    idatelier: [],
    dateDebut: [''],    // Pas de date par défaut
    dateFin: [''],      // Pas de date par défaut
  });
}
```

#### 2. Modifier searchAffectation() (ligne ~121)
```typescript
searchAffectation(): void {
  this.affectationService.searchAffectation(
    this.idUser,
    this.myFormSearch.value.idprojet ?? 0,
    this.myFormSearch.value.idemploye ?? 0,
    this.myFormSearch.value.idarticle ?? 0,
    this.myFormSearch.value.idatelier ?? 0,
    this.myFormSearch.value.dateDebut || '',
    this.myFormSearch.value.dateFin || '',
    this.page - 1,  // Spring Data Page commence à 0
    this.tableSize
  ).subscribe(
    (response) => {
      this.POSTS = response.content;  // Les données paginées
      this.count = response.totalItems;  // Total d'éléments
    },
    (error) => {
      console.error('Erreur lors de la recherche des affectations:', error);
    }
  );
}
```

#### 3. Modifier ClearSearch() (ligne ~115)
```typescript
ClearSearch() {
  this.initmyForm();
  this.page = 1;  // Réinitialiser à la première page
  this.searchAffectation();
}
```

#### 4. Supprimer la méthode getCurrentYearDates() (ligne ~470)
Cette méthode n'est plus nécessaire car nous n'utilisons plus de dates par défaut.

## Avantages de cette solution

1. **Performance améliorée** : Seules 10-20 lignes sont chargées à la fois au lieu de 70 615
2. **Chargement initial rapide** : Aucune donnée n'est chargée au démarrage (pas de dates par défaut)
3. **Expérience utilisateur** : L'utilisateur doit spécifier ses critères de recherche
4. **Scalabilité** : Le système peut gérer des millions d'affectations sans problème

## Test

1. Démarrer le backend
2. Démarrer le frontend
3. Aller sur la page des affectations
4. Vérifier qu'aucune donnée n'est chargée au démarrage
5. Sélectionner des critères de recherche (dates, atelier, etc.)
6. Cliquer sur "Rechercher"
7. Vérifier que seulement 10 lignes sont affichées
8. Tester la pagination en changeant de page
9. Tester le changement de taille de page (10, 15, 20 lignes par page)

## Notes importantes

- Le backend utilise la pagination Spring Data (page commence à 0)
- Le frontend utilise ngx-pagination (page commence à 1)
- La conversion est faite dans le composant : `this.page - 1`
- Les filtres de date sont maintenant optionnels (chaînes vides par défaut)
