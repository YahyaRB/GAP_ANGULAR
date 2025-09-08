// Méthodes à ajouter dans votre DetailLivraisonService

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {IdetailLivraison} from "./Interfaces/idetail-livraison";
import {environment} from "../../environments/environment";
import {OfProjectQteRest} from "./Interfaces/of-project-qte-rest";


@Injectable({
  providedIn: 'root'
})
export class DetailLivraisonService {

  private AUTH_API = 'api/DetailLivraison'
  private baseUrl=environment.apiUrl+this.AUTH_API;

  constructor(private http: HttpClient) { }

  // Méthode pour mettre à jour un détail de livraison
  updateDetail(detail: IdetailLivraison): Observable<IdetailLivraison> {
    const url = `${this.baseUrl}/Editer/${detail.id}`;
    return this.http.put<IdetailLivraison>(url, detail);
  }

  // Méthode pour supprimer un détail de livraison
  deleteDetail(detailId: number): Observable<void> {
    const url = `${this.baseUrl}/Supprimer/${detailId}`;
    return this.http.delete<void>(url);
  }

  // Méthode pour obtenir un détail spécifique
  getDetailById(detailId: number): Observable<IdetailLivraison> {
    const url = `${this.baseUrl}/details-livraison/${detailId}`;
    return this.http.get<IdetailLivraison>(url);
  }

  // Méthode existante (si pas déjà présente)
  getListeDetailByLivraison(livraisonId: number): Observable<IdetailLivraison[]> {
    const url = `${this.baseUrl}/Livraison/ListeDetailByBL/${livraisonId}`;
    return this.http.get<IdetailLivraison[]>(url);
  }

  // Méthode existante (si pas déjà présente)
  ajouterDetail(detail: any, livraisonId: number, options?: any): Observable<any> {
    const url = `${this.baseUrl}/Ajouter/${livraisonId}`;
    return this.http.post(url, detail, options);
  }

  // Méthode pour valider la quantité par rapport au stock disponible
  validateQuantite(ordreFabricationId: number, quantite: number): Observable<boolean> {
    const url = `${this.baseUrl}/ordre-fabrication/${ordreFabricationId}/validate-quantity`;
    return this.http.post<boolean>(url, { quantite });
  }

  // Méthode pour obtenir les détails paginés (optionnel si vous voulez la pagination côté serveur)
  getDetailsPaginated(livraisonId: number, page: number, size: number): Observable<{
    content: IdetailLivraison[],
    totalElements: number,
    totalPages: number,
    currentPage: number
  }> {
    const url = `${this.baseUrl}/livraisons/${livraisonId}/details/paginated`;
    const params = {
      page: page.toString(),
      size: size.toString()
    };
    return this.http.get<{
      content: IdetailLivraison[],
      totalElements: number,
      totalPages: number,
      currentPage: number
    }>(url, { params });
  }
  getAllOFByLivraison(idLivraison: number): Observable<OfProjectQteRest[]> {
    const url = `${this.baseUrl}/getAllOFByLivraison/${idLivraison}`;
    return this.http.get<OfProjectQteRest[]>(url);
  }
  // Méthode pour l'impression de livraison (corrige l'erreur mentionnée)

}

// Interface pour la réponse paginée (à ajouter dans vos interfaces si pas présente)
export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
  first: boolean;
  last: boolean;
}
