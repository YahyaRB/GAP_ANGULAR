import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import {Ilivraison} from "../../../services/Interfaces/ilivraison";
import {LivraisonService} from "../../../services/livraison.service";
import {ListeLivraisonsComponent} from "../../Livraison/liste-livraisons/liste-livraisons.component";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iateliers} from "../../../services/Interfaces/iateliers";
import {Iprojet} from "../../../services/Interfaces/iprojet";
import {ProjetService} from "../../../services/projet.service";
import {IordreFabrication} from "../../../services/Interfaces/iordre-fabrication";
import {DetailLivraisonService} from "../../../services/detail-livraison.service";
import {IdetailLivraison} from "../../../services/Interfaces/idetail-livraison";
import {ActivatedRoute} from "@angular/router";
import {OfProjectQteRest} from "../../../services/Interfaces/of-project-qte-rest";
import {OfService} from "../../../services/of.service";
import {ROLES_ADMIN, ROLES_ADMIN_AGENTSAISIE} from "../../../Roles";

@Component({
  selector: 'app-add-detail-livraison',
  templateUrl: './add-detail-livraison.component.html',
  styleUrls: ['./add-detail-livraison.component.css']
})
export class AddDetailLivraisonComponent implements OnInit, OnChanges {
  @ViewChild('closebutton') closebutton;
  @Input() livraison: Ilivraison;
  @Output() ordreSelectionne = new EventEmitter<IordreFabrication>();
  myFormAdd: FormGroup;
  ordreFabrications: OfProjectQteRest[] = [];
  loading = false;
  error: string | null = null;
  detailsLivraison: IdetailLivraison[] = [];
  qteMax: number;

  // Variables pour la pagination
  currentPage = 1;
  pageSize = 5;
  totalItems = 0;
  paginatedDetails: IdetailLivraison[] = [];

  // Variables pour l'édition inline
  editingRows: Set<number> = new Set();
  originalValues: Map<number, any> = new Map();
  tempQuantities: Map<number, number> = new Map();

  // Expose Math pour le template
  Math = Math;

  constructor(
    private ofService: OfService,
    private detailLivraisonService: DetailLivraisonService,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private livraisonService:LivraisonService,
    private notificationService: NotificationService
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    if(this.livraison){
      this.loadDetailLivraison();
    }
  }

  ngOnInit(): void {
    this.initmyForm();
  }

  onMaterialGroupChange(event) {}

  private initmyForm() {
    this.myFormAdd = this.formBuilder.group({
      quantite:['', Validators.required],
      emplacement:['', Validators.required],
      observation:['', Validators.required],
      ordreFabrication:[[], Validators.required],
    });
  }

  loadDetailLivraison(): void {
    if (this.livraison.id) {
      this.loading = true;
      this.error = null;
      this.detailLivraisonService.getAllOFByLivraison(this.livraison.id).subscribe({
        next: (data) => {
          this.ordreFabrications = data;

        },
        error: (err) => {
          console.error('Erreur lors du chargement:', err);
          this.error = 'Erreur lors du chargement des données';
          this.loading = false;
        }
      });
      this.detailLivraisonService.getListeDetailByLivraison(this.livraison.id).subscribe({
        next: (data) => {
          this.detailsLivraison = data;
          this.totalItems = data.length;
          this.updatePaginatedData();
          this.loading = false;
        },
        error: (err) => {
          console.error('Erreur lors du chargement:', err);
          this.error = 'Erreur lors du chargement des données';
          this.loading = false;
        }
      });
    }
  }

  // Méthodes de pagination
  updatePaginatedData(): void {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedDetails = this.detailsLivraison.slice(startIndex, endIndex);
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.updatePaginatedData();
  }

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.pageSize);
  }

  get pages(): number[] {
    const maxPagesToShow = 5;
    const pages = [];
    const startPage = Math.max(1, this.currentPage - Math.floor(maxPagesToShow / 2));
    const endPage = Math.min(this.totalPages, startPage + maxPagesToShow - 1);

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }
    return pages;
  }

  // Méthodes d'édition inline
  startEdit(detail: IdetailLivraison, index: number): void {
    const globalIndex = (this.currentPage - 1) * this.pageSize + index;
    this.editingRows.add(globalIndex);
    this.originalValues.set(globalIndex, {
      quantite: detail.quantite,
      emplacement: detail.emplacement,
      observation: detail.observation
    });
    this.tempQuantities.set(globalIndex, detail.quantite);
  }

  cancelEdit(detail: IdetailLivraison, index: number): void {
    const globalIndex = (this.currentPage - 1) * this.pageSize + index;
    this.editingRows.delete(globalIndex);

    // Restaurer les valeurs originales
    const original = this.originalValues.get(globalIndex);
    if (original) {
      detail.quantite = original.quantite;
      detail.emplacement = original.emplacement;
      detail.observation = original.observation;
    }

    this.originalValues.delete(globalIndex);
    this.tempQuantities.delete(globalIndex);
  }

  saveEdit(detail: IdetailLivraison, index: number): void {

    const globalIndex = (this.currentPage - 1) * this.pageSize + index;

    // Validation de la quantité
    const newQuantite = this.tempQuantities.get(globalIndex) || detail.quantite;
    const maxQuantite = detail.ordreFabrication?.qteRest+detail.quantite || 0;

    if (newQuantite > maxQuantite) {
      this.notificationService.showError(
        `La quantité ne peut pas dépasser ${maxQuantite}`,
        "Erreur de validation"
      );
      return;
    }

    if (newQuantite <= 0) {
      this.notificationService.showError(
        "La quantité doit être supérieure à 0",
        "Erreur de validation"
      );
      return;
    }

    // Mise à jour de la quantité
    detail.quantite = newQuantite;

    // Appel au service pour sauvegarder
    this.detailLivraisonService.updateDetail(detail).subscribe({
      next: (response) => {
        this.notificationService.showSuccess("Détail mis à jour avec succès", "Succès");
        this.editingRows.delete(globalIndex);
        this.originalValues.delete(globalIndex);
        this.tempQuantities.delete(globalIndex);
        this.loadDetailLivraison(); // Recharger les données
      },
      error: (error) => {
        console.error("Erreur lors de la mise à jour:", error);
        this.notificationService.showError("Erreur lors de la mise à jour", "Erreur");
        this.cancelEdit(detail, index);
      }
    });
  }

  isEditing(index: number): boolean {
    const globalIndex = (this.currentPage - 1) * this.pageSize + index;
    return this.editingRows.has(globalIndex);
  }

  onQuantiteInputChange(event: any, detail: IdetailLivraison, index: number): void {
    const globalIndex = (this.currentPage - 1) * this.pageSize + index;
    const newQuantite = parseInt(event.target.value);
    this.tempQuantities.set(globalIndex, newQuantite);
  }

  getTempQuantite(detail: IdetailLivraison, index: number): number {
    const globalIndex = (this.currentPage - 1) * this.pageSize + index;
    return this.tempQuantities.get(globalIndex) || detail.quantite;
  }

  getMaxQuantiteForDetail(detail: IdetailLivraison): number {
    return detail.ordreFabrication?.qteRest+detail.quantite || 0;

  }

  onAdd() {
    this.detailLivraisonService
      .ajouterDetail(this.myFormAdd.value, this.livraison.id, { responseType: 'text' })
      .subscribe({
        next: (data: any) => {
        /*  setTimeout(() => {*/
            this.notificationService.showSuccess(data, "Ajout Livraison");
            this.initmyForm();
            this.loadDetailLivraison(); // Recharger pour mettre à jour la pagination
      /*    }, 400);*/
        },
        error: (error) => {
          console.error("Erreur lors de l'ajout :", error);
          this.notificationService.showError("Échec de l'ajout", "Erreur");
        },
      });
  }

  onQuantiteChange($event: Event) {
    // Méthode existante si nécessaire
  }

  deleteDetail(detail: any) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce détail ?')) {
      this.detailLivraisonService.deleteDetail(detail.id).subscribe({
        next: () => {
          this.notificationService.showSuccess("Détail supprimé avec succès", "Succès");
          this.loadDetailLivraison();
        },
        error: (error) => {
          console.error("Erreur lors de la suppression:", error);
          this.notificationService.showError("Erreur lors de la suppression", "Erreur");
        }
      });
    }
  }

  editDetail(detail: any) {
    // Cette méthode peut être supprimée car nous utilisons l'édition inline
  }

  onValueChangeQteMax($event: any) {
    this.qteMax = this.myFormAdd.value.ordreFabrication.qteRest;
  }

  // Méthode pour calculer la hauteur maximale de la table
  getTableMaxHeight(): number {
    // Hauteur basée sur le nombre de lignes par page
    const baseHeight = 400; // Hauteur de base
    const rowHeight = 60;   // Hauteur approximative par ligne
    const headerHeight = 45; // Hauteur de l'en-tête

    if (this.pageSize <= 5) return baseHeight;
    if (this.pageSize <= 10) return baseHeight + (rowHeight * 5);
    if (this.pageSize <= 25) return baseHeight + (rowHeight * 15);
    return baseHeight + (rowHeight * 25); // Maximum
  }

  protected readonly ROLES_ADMIN = ROLES_ADMIN;
  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;
}
