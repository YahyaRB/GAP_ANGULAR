
export interface DetailLivraisonRequest {
  type: string;
  livraisonId: number;
  ordreFabricationId?: number;
  nomenclatureId?: number;
  quantite: number;
  emplacement: string;
  observation: string;
}
