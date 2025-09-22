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

import {ActivatedRoute} from "@angular/router";
import {OfProjectQteRest} from "../../../services/Interfaces/of-project-qte-rest";
import {OfService} from "../../../services/of.service";
import {ROLES_ADMIN, ROLES_ADMIN_AGENTSAISIE} from "../../../Roles";
import { switchMap } from 'rxjs/operators';
import {INomenclature} from "../../../services/Interfaces/inomenclature";
import {DetailLivraisonRequest} from "../../../services/Interfaces/detail-livraison-request";
import {IdetailLivraison} from "../../../services/Interfaces/idetail-livraison";

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
  typeDetail: string = 'OF_COMPLET';
  nomenclatures: INomenclature[] = [];
  selectedOF: OfProjectQteRest | null = null;
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
      quantite: ['', Validators.required],
      emplacement: ['', Validators.required],
      observation: ['', Validators.required],
      ordreFabrication: [[], this.typeDetail === 'OF_COMPLET' ? Validators.required : null],
      nomenclature: [null, this.typeDetail === 'NOMENCLATURE' ? Validators.required : null],
    });

    // Désactiver le contrôle si nécessaire après la création
    if (!this.selectedOF && this.typeDetail === 'NOMENCLATURE') {
      this.myFormAdd.get('nomenclature')?.disable();
    }
  }
  onTypeDetailChange(type: string) {
    this.typeDetail = type;
    this.nomenclatures = [];
    this.selectedOF = null;
    this.initmyForm();
  }
  onOFSelectionForNomenclature(of: OfProjectQteRest) {
    this.selectedOF = of;
    if (of && of.id) {
      // Activer le contrôle nomenclature
      this.myFormAdd.get('nomenclature')?.enable();

      this.detailLivraisonService.getNomenclaturesByOF(of.id).subscribe({
        next: (data) => {
          this.nomenclatures = data;
        },
        error: (err) => {
          console.error('Erreur lors du chargement des nomenclatures:', err);
          this.nomenclatures = [];
        }
      });
    }}

  loadDetailLivraison(): void {
    if (this.livraison.id) {
      this.loading = true;
      this.error = null;

      // Compteur pour suivre les appels terminés
      let completedCalls = 0;
      const totalCalls = 2;

      const checkComplete = () => {
        completedCalls++;
        if (completedCalls === totalCalls) {
          this.loading = false;
        }
      };

      // Premier appel - Récupérer les OF disponibles
      this.detailLivraisonService.getAllOFByLivraison(this.livraison.id).subscribe({
        next: (data) => {
          console.log('getAllOFByLivraison success:', data);
          this.ordreFabrications = data;
          checkComplete();
        },
        error: (err) => {
          console.error('Erreur getAllOFByLivraison:', err);
          this.ordreFabrications = [];
          checkComplete();
        }
      });

      // Deuxième appel - Récupérer les détails de livraison
      this.detailLivraisonService.getListeDetailByLivraison(this.livraison.id).subscribe({
        next: (data) => {
          console.log('getListeDetailByLivraison success:', data);

          // DEBUG - Afficher les données reçues
          data.forEach((detail, index) => {
            console.log(`Detail ${index}:`, {
              id: detail.id,
              typeDetail: detail.typeDetail,
              quantite: detail.quantite,
              hasOF: !!detail.ordreFabrication,
              hasNomenclature: !!detail.nomenclature,
              ofNumOF: detail.ordreFabrication?.numOF,
              nomenclatureType: detail.nomenclature?.type
            });
          });

          this.detailsLivraison = data;
          this.totalItems = data.length;
          this.updatePaginatedData();
          checkComplete();
        },
        error: (err) => {
          console.error('Erreur getListeDetailByLivraison:', err);
          this.detailsLivraison = [];
          this.totalItems = 0;
          this.updatePaginatedData();
          this.error = 'Erreur lors du chargement des détails de livraison';
          checkComplete();
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
  getDetailDesignation(detail: IdetailLivraison): string {
    const type = this.getDetailType(detail);

    if (type === 'OF_COMPLET' && detail.ordreFabrication?.article) {
      return detail.ordreFabrication.article.designation || 'N/A';
    } else if (type === 'NOMENCLATURE' && detail.nomenclature) {
      // Utiliser la propriété designation si elle existe
      if (detail.nomenclature.designation) {
        return detail.nomenclature.designation;
      }
      // Sinon utiliser type comme fallback
      return detail.nomenclature.type || 'N/A';
    }

    return 'N/A';
  }

  getDetailNumOF(detail: IdetailLivraison): string {
    const type = this.getDetailType(detail);

    if (type === 'OF_COMPLET' && detail.ordreFabrication) {
      return detail.ordreFabrication.numOF || 'N/A';
    } else if (type === 'NOMENCLATURE' && detail.nomenclature) {
      // Utiliser d'abord l'objet ordreFabrication si disponible
      if (detail.nomenclature.ordreFabrication) {
        return detail.nomenclature.ordreFabrication.numOF || 'N/A';
      }
      // Sinon utiliser la propriété numOF si elle existe
      if (detail.nomenclature.numOF) {
        return detail.nomenclature.numOF;
      }
      // Fallback: chercher dans la liste des OF par ID
      if (detail.nomenclature.ordreFabricationId && this.ordreFabrications) {
        const of = this.ordreFabrications.find(o => o.id === detail.nomenclature!.ordreFabricationId);
        return of ? of.numOF : `OF-${detail.nomenclature.ordreFabricationId}`;
      }
    }

    return 'N/A';
  }
  getDetailType(detail: IdetailLivraison): string {
    // Si typeDetail est défini, l'utiliser
    if (detail.typeDetail) {
      return detail.typeDetail;
    }

    // Sinon, deviner basé sur les relations présentes
    if (detail.nomenclature) {
      return 'NOMENCLATURE';
    } else if (detail.ordreFabrication) {
      return 'OF_COMPLET';
    }

    return 'UNKNOWN';
  }

  saveEdit(detail: IdetailLivraison, index: number): void {
    const globalIndex = (this.currentPage - 1) * this.pageSize + index;

    // Validation de la quantité
    const newQuantite = this.tempQuantities.get(globalIndex) || detail.quantite;
    const maxQuantite = this.getMaxQuantiteForDetail(detail);

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
  loadOFForNomenclature(nomenclature: INomenclature): void {
    if (nomenclature.ordreFabrication && this.ordreFabrications) {
      const of = this.ordreFabrications.find(o => o.id === nomenclature.ordreFabrication.id);
      if (of) {

        nomenclature.numOF = of.numOF;
      }
    }
  }
  getMaxQuantiteForDetail(detail: IdetailLivraison): number {
    // Pour OF_COMPLET
    if (detail.typeDetail === 'OF_COMPLET' && detail.ordreFabrication) {
      return (detail.ordreFabrication.qteRest || 0) + detail.quantite;
    }

    // Pour NOMENCLATURE
    if (detail.typeDetail === 'NOMENCLATURE' && detail.nomenclature) {
      return (detail.nomenclature.quantiteRest || 0) + detail.quantite;
    }

    // Fallback pour les cas où typeDetail n'est pas défini mais OF existe
    if (!detail.typeDetail && detail.ordreFabrication) {
      return (detail.ordreFabrication.qteRest || 0) + detail.quantite;
    }

    return detail.quantite; // Minimum: la quantité actuelle
  }


  onAdd() {
    const request: DetailLivraisonRequest = {
      type: this.typeDetail,
      livraisonId: this.livraison.id,
      quantite: this.myFormAdd.value.quantite,
      emplacement: this.myFormAdd.value.emplacement,
      observation: this.myFormAdd.value.observation
    };

    if (this.typeDetail === 'OF_COMPLET') {
      request.ordreFabricationId = this.myFormAdd.value.ordreFabrication.id;
    } else {
      request.nomenclatureId = this.myFormAdd.value.nomenclature.id;
    }

    // Appeler la bonne méthode
    this.detailLivraisonService.addDetailWithType(request).subscribe({
      next: (data: any) => {
        this.notificationService.showSuccess(data, "Ajout Livraison");
        this.initmyForm();
        this.loadDetailLivraison();
      },
      error: (error) => {
        console.error("Erreur lors de l'ajout :", error);
        this.notificationService.showError(error.error || "Échec de l'ajout", "Erreur");
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
    if (this.typeDetail === 'OF_COMPLET' && this.myFormAdd.value.ordreFabrication) {
      this.qteMax = this.myFormAdd.value.ordreFabrication.qteRest;
    } else if (this.typeDetail === 'NOMENCLATURE' && this.myFormAdd.value.nomenclature) {
      this.qteMax = this.myFormAdd.value.nomenclature.quantiteRest;
    }
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
  imprimDetail(detail: IdetailLivraison) {

    this.detailLivraisonService.impressionDetail(detail.id).subscribe(
      (response: Blob) => {
        const url = window.URL.createObjectURL(response);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Detail_${detail.id}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();

        // Mise à jour du statut d'impression

      },
      error => {
        console.error('Error downloading the file', error);
      }
    );
  }
  protected readonly ROLES_ADMIN = ROLES_ADMIN;
  protected readonly ROLES_ADMIN_AGENTSAISIE = ROLES_ADMIN_AGENTSAISIE;
}
