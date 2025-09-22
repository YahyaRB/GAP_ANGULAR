import {Component, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";
import {INomenclature} from "../../../services/Interfaces/inomenclature";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {Iarticle} from "../../../services/Interfaces/iarticle";
import {OfService} from "../../../services/of.service";
import {NomenclatureService} from "../../../services/nomenclature.service";
import {AtelierService} from "../../../services/atelier.service";
import {ProjetService} from "../../../services/projet.service";
import {ArticleService} from "../../../services/article.service";
import {RoleService} from "../../../services/role.service";
import {NotificationService} from "../../../services/notification.service";
import {AddOFComponent} from "../add-of/add-of.component";
import {UpdateOFComponent} from "../update-of/update-of.component";
import {DeleteOFComponent} from "../delete-of/delete-of.component";

import {ROLES_ADMIN, ROLES_ADMIN_AGENTSAISIE} from 'src/app/Roles';
import * as XLSX from 'xlsx';
import {TokenStorageService} from "../../../Auth/services/token-storage.service";
import {AddNomenclatureComponent} from "../../Nomenclature/add-nomenclature/add-nomenclature.component";
import {UpdateNomenclatureComponent} from "../../Nomenclature/update-nomenclature/update-nomenclature.component";
import {DeleteNomenclatureComponent} from "../../Nomenclature/delete-nomenclature/delete-nomenclature.component";
import {HttpClient} from "@angular/common/http";

@Component({
  selector: 'app-liste-of',
  templateUrl: './liste-of.component.html',
  styleUrls: ['./liste-of.component.css']
})
export class ListeOFComponent implements OnInit {

  @ViewChild(AddOFComponent) addOFComponent: AddOFComponent;
  @ViewChild(UpdateOFComponent) updateOFComponent: UpdateOFComponent;
  @ViewChild(DeleteOFComponent) deleteOFComponent: DeleteOFComponent;
  @ViewChild(AddNomenclatureComponent) addNomenclatureComponent: AddNomenclatureComponent;
  @ViewChild(UpdateNomenclatureComponent) updateNomenclatureComponent: UpdateNomenclatureComponent;
  @ViewChild(DeleteNomenclatureComponent) deleteNomenclatureComponent: DeleteNomenclatureComponent;

  // Propriétés existantes
  POSTS: IordreFabrication[] = [];
  ofSelected: IordreFabrication;
  listeAteliers: Iateliers[] = [];
  listeAffairesByAtelier: Iprojet[] = [];
  listeArticles: Iarticle[] = [];
  myFormSearch: FormGroup;
  idUser: number = 1; // Assurez-vous que l'ID de l'utilisateur est récupéré correctement
  // Pagination et filtres
  page: number = 1;
  count: number = 0;
  tableSize: number = 10;
  pfiltre: string = '';
  dateDebut: string = '';
  dateFin: string = '';

  // Propriétés pour la gestion des nomenclatures
  selectedNomenclature: INomenclature;
  selectedOFForNomenclature: IordreFabrication;

  // Cache des nomenclatures par OF
  nomenclaturesCache: Map<number, INomenclature[]> = new Map();
  loadingStatesCache: Map<number, boolean> = new Map();
  errorStatesCache: Map<number, boolean> = new Map();
  expandedOFs: Set<number> = new Set();

  // Constantes pour les rôles
  protected readonly ROLES_ADMIN = ROLES_ADMIN;
  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;

  constructor(
    private ofService: OfService,
    private tokenStorage:TokenStorageService,
    private nomenclatureService: NomenclatureService,
    private atelierService: AtelierService,
    private projetService: ProjetService,
    private articleService: ArticleService,
    private roleService: RoleService,
    private http: HttpClient,
    private notifyService: NotificationService,
    private formBuilder: FormBuilder
  ) {
    this.initForm();
    // Récupération d'id d'utilisateur connecté
    this.idUser=this.tokenStorage.getUser().id
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  // Initialisation du formulaire de recherche
  private initForm(): void {
    this.myFormSearch = this.formBuilder.group({
      numOF: [''],
      idprojet: [''],
      idarticle: [''],
      idatelier: [''],
      dateDebut: [''],
      dateFin: ['']
    });
  }

  // Chargement des données initiales
  private loadInitialData(): void {
    this.searchOF();
    this.loadAteliers();
    this.loadAffaires();
    this.loadArticles();
  }

  searchOF(): void {
    console.log('🔍 Recherche avec searchOF réel');

    const formValues = this.myFormSearch.value;

    this.ofService.searchOF(
      this.idUser,
      formValues.numOF || '',
      formValues.idprojet || null,
      formValues.idatelier || null,
      formValues.idarticle || null,
      formValues.dateDebut || null,
      formValues.dateFin || null
    ).subscribe({
      next: (data) => {
        console.log('✅ searchOF réussi - Données reçues:', data);
        this.POSTS = data;
        this.count = data.length;
        setTimeout(() => {
          this.extractUniqueTables();
        }, 1000);
      },
      error: (error) => {
        console.error('❌ searchOF échoué:', error);
        this.notifyService.showError('Erreur lors du chargement des ordres de fabrication', 'Erreur');
      }
    });
  }
  extractUniqueTables() {
    const uniqueProjectsMap = new Map<number, Iprojet>();
    const uniqueAteliersMap = new Map<number, Iateliers>();
    const uniqueArticlesMap = new Map<number, Iarticle>();

    this.POSTS.forEach(element => {
      if (!uniqueProjectsMap.has(element.projet.id)) {
        uniqueProjectsMap.set(element.projet.id, element.projet);
      }
      if (!uniqueAteliersMap.has(element.atelier.id)) {
        uniqueAteliersMap.set(element.atelier.id, element.atelier);
      }
      if (!uniqueArticlesMap.has(element.article.id)) {
        uniqueArticlesMap.set(element.article.id, element.article);
      }
    });

    this.listeAffairesByAtelier = Array.from(uniqueProjectsMap.values());
    this.listeAteliers = Array.from(uniqueAteliersMap.values());
    this.listeArticles=Array.from(uniqueArticlesMap.values());
  }

  // Chargement des ateliers
  private loadAteliers(): void {
    this.atelierService.getAll(this.idUser).subscribe({
      next: (data: Iateliers[]) => {
        this.listeAteliers = data;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des ateliers:', error);
      }
    });
  }

  // Chargement des affaires
  private loadAffaires(): void {
    this.projetService.getAll().subscribe({
      next: (data: Iprojet[]) => {
        this.listeAffairesByAtelier = data;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des affaires:', error);
      }
    });
  }

  // Chargement des articles
  private loadArticles(): void {
    this.articleService.getAll(this.idUser).subscribe({
      next: (data: Iarticle[]) => {
        this.listeArticles = data;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des articles:', error);
      }
    });
  }

  // ================= GESTION DES NOMENCLATURES =================

  // Toggle l'affichage des nomenclatures pour un OF
  toggleNomenclatures(ordreFabrication: IordreFabrication): void {
    const ofId = ordreFabrication.id;

    if (this.expandedOFs.has(ofId)) {
      // Fermer l'expansion
      this.expandedOFs.delete(ofId);
    } else {
      // Ouvrir l'expansion et charger les nomenclatures
      this.expandedOFs.add(ofId);
      this.loadNomenclatures(ofId);
    }
  }

  // Vérifie si un OF est étendu
  isOFExpanded(ofId: number): boolean {
    return this.expandedOFs.has(ofId);
  }

  // Charge les nomenclatures pour un OF donné
  loadNomenclatures(ofId: number): void {
    // Éviter les chargements multiples
    if (this.loadingStatesCache.get(ofId) || this.nomenclaturesCache.has(ofId)) {
      return;
    }

    this.loadingStatesCache.set(ofId, true);
    this.errorStatesCache.set(ofId, false);

    this.nomenclatureService.getNomenclaturesByOF(ofId).subscribe({
      next: (nomenclatures: INomenclature[]) => {
        this.nomenclaturesCache.set(ofId, nomenclatures);
        this.loadingStatesCache.set(ofId, false);
      },
      error: (error) => {
        console.error(`Erreur lors du chargement des nomenclatures pour l'OF ${ofId}:`, error);
        this.loadingStatesCache.set(ofId, false);
        this.errorStatesCache.set(ofId, true);
      }
    });
  }

  // Retente le chargement des nomenclatures
  retryLoadNomenclatures(ofId: number): void {
    this.nomenclaturesCache.delete(ofId);
    this.errorStatesCache.set(ofId, false);
    this.loadNomenclatures(ofId);
  }

  // Rafraîchit les nomenclatures après modification
  refreshNomenclatures(): void {
    if (this.selectedOFForNomenclature) {
      const ofId = this.selectedOFForNomenclature.id;
      this.nomenclaturesCache.delete(ofId);
      this.loadNomenclatures(ofId);
    }
  }

  // ================= GETTERS POUR LES NOMENCLATURES =================

  // Récupère les nomenclatures d'un OF
  getNomenclatures(ofId: number): INomenclature[] {
    return this.nomenclaturesCache.get(ofId) || [];
  }

  // Vérifie si un OF a des nomenclatures
  hasNomenclatures(ofId: number): boolean {
    const nomenclatures = this.nomenclaturesCache.get(ofId);
    return nomenclatures && nomenclatures.length > 0;
  }

  // Vérifie si les nomenclatures d'un OF sont en cours de chargement
  isLoadingNomenclatures(ofId: number): boolean {
    return this.loadingStatesCache.get(ofId) || false;
  }

  // Vérifie s'il y a une erreur pour un OF
  hasErrorForOF(ofId: number): boolean {
    return this.errorStatesCache.get(ofId) || false;
  }

  // Compte les nomenclatures disponibles
  getDisponiblesCount(ofId: number): number {
    const nomenclatures = this.getNomenclatures(ofId);
    return nomenclatures.filter(n => (n.quantiteRest || 0) > 0).length;
  }

  // Compte les nomenclatures épuisées
  getEpuiseesCount(ofId: number): number {
    const nomenclatures = this.getNomenclatures(ofId);
    return nomenclatures.filter(n => (n.quantiteRest || 0) === 0).length;
  }

  // Calcule le pourcentage livré
  getPourcentageLivre(ofId: number): number {
    const nomenclatures = this.getNomenclatures(ofId);
    if (nomenclatures.length === 0) return 0;

    const totalQuantite = nomenclatures.reduce((sum, n) => sum + (n.quantite || 0), 0);
    const totalLivre = nomenclatures.reduce((sum, n) => sum + (n.quantiteLivre || 0), 0);

    if (totalQuantite === 0) return 0;
    return Math.round((totalLivre / totalQuantite) * 100);
  }

  // ================= STYLES ET AFFICHAGE =================

  // Retourne la classe CSS pour la quantité restante
  getQuantiteRestClass(quantiteRest: number): string {
    if (!quantiteRest || quantiteRest === 0) {
      return 'text-danger font-weight-bold';
    } else if (quantiteRest < 10) {
      return 'text-warning font-weight-bold';
    } else {
      return 'text-success font-weight-bold';
    }
  }

  // Retourne le titre du statut d'une nomenclature
  getStatutTitle(nomenclature: INomenclature): string {
    const quantiteRest = nomenclature.quantiteRest || 0;
    const quantiteLivre = nomenclature.quantiteLivre || 0;
    const quantiteTotal = nomenclature.quantite || 0;

    if (quantiteRest === 0) {
      return 'Épuisé - Aucun stock restant';
    } else if (quantiteLivre > 0) {
      return `Partiellement livré - ${quantiteLivre}/${quantiteTotal}`;
    } else {
      return 'Disponible - Aucune livraison effectuée';
    }
  }

  // Retourne l'icône du statut
  getStatutIcon(nomenclature: INomenclature): string {
    const quantiteRest = nomenclature.quantiteRest || 0;
    const quantiteLivre = nomenclature.quantiteLivre || 0;

    if (quantiteRest === 0) {
      return 'bx bx-x-circle';
    } else if (quantiteLivre > 0) {
      return 'bx bx-minus-circle';
    } else {
      return 'bx bx-check-circle';
    }
  }

  // Retourne la couleur du statut
  getStatutColor(nomenclature: INomenclature): string {
    const quantiteRest = nomenclature.quantiteRest || 0;
    const quantiteLivre = nomenclature.quantiteLivre || 0;

    if (quantiteRest === 0) {
      return '#dc3545'; // Rouge
    } else if (quantiteLivre > 0) {
      return '#ffc107'; // Orange
    } else {
      return '#28a745'; // Vert
    }
  }

  // ================= GESTION DES MODALS NOMENCLATURES =================

  // Ouvre le modal d'ajout de nomenclature
  openAddNomenclatureModal(ordreFabrication: IordreFabrication): void {
    this.selectedOFForNomenclature = ordreFabrication;
  }

  // Ouvre le modal de modification de nomenclature
  openUpdateNomenclatureModal(nomenclature: INomenclature, ordreFabrication: IordreFabrication): void {
    this.selectedNomenclature = nomenclature;
    this.selectedOFForNomenclature = ordreFabrication;
  }

  // Ouvre le modal de suppression de nomenclature
  openDeleteNomenclatureModal(nomenclature: INomenclature): void {
    this.selectedNomenclature = nomenclature;
  }

  // ================= GESTION DES OF (MÉTHODES EXISTANTES) =================

  // Récupère un item pour les modals d'OF
  recupItem(ordreFabrication: IordreFabrication): void {
    this.ofSelected = ordreFabrication;
  }

  // Récupère les items pour l'ajout
  recupItemAdd(): void {
    // Logique pour préparer l'ajout d'un nouvel OF
  }

  // ================= GESTION DES RÔLES =================

  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string): boolean {
    return this.roleService.hasRole(roleToCheck);
  }

  // ================= PAGINATION ET FILTRES =================

  onTableDataChange(event: any): void {
    this.page = event;
  }

  onTableSizeChange(): void {
    this.page = 1;
  }

  ClearSearch(): void {
    this.myFormSearch.reset();
    this.pfiltre = '';
    this.searchOF();
  }

  // ================= TRI =================

  sortColumn(columnName: string): void {
    // Implémentation du tri selon la colonne
    this.POSTS.sort((a, b) => {
      const valueA = this.getNestedProperty(a, columnName);
      const valueB = this.getNestedProperty(b, columnName);

      if (valueA < valueB) return -1;
      if (valueA > valueB) return 1;
      return 0;
    });
  }

  private getNestedProperty(obj: any, path: string): any {
    return path.split('.').reduce((o, p) => o && o[p], obj);
  }

  // ================= EXPORT EXCEL =================

  exportExel(): void {
    try {
      const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.POSTS);
      const wb: XLSX.WorkBook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, 'Ordres de Fabrication');
      XLSX.writeFile(wb, 'ordres_fabrication.xlsx');
      this.notifyService.showSuccess('Export Excel réussi', 'Export');
    } catch (error) {
      console.error('Erreur lors de l\'export Excel:', error);
      this.notifyService.showError('Erreur lors de l\'export Excel', 'Erreur');
    }
  }

  // ================= IMPRESSION =================

  ImprimeOF(ordreFabrication: IordreFabrication): void {
    // Implémentation de l'impression de l'OF
    console.log('Impression de l\'OF:', ordreFabrication.numOF);
  }

  // ================= TRACK BY FUNCTIONS =================

  trackByNomenclature(index: number, nomenclature: INomenclature): number {
    return nomenclature.id;
  }

  trackByOF(index: number, of: IordreFabrication): number {
    return of.id;
  }
}
