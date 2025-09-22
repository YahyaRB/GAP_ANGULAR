export interface INomenclature {
  id: number;
  type: string;
  designation: string;
  unite: string;

  // Propriétés de quantité (selon NomenclatureDTO)
  quantite: number;       // Propriété principale (vient de quantiteTot en backend)
  quantiteRest: number;   // Quantité restante
  quantiteLivre: number;  // Quantité livrée

  // Propriétés de relation
  ordreFabricationId: number;

  // Propriétés optionnelles pour compatibilité
  quantiteTot?: number;   // Alias pour quantite si nécessaire
  ordreFabrication?: any; // Pour compatibilité
  numOF?: string;
  articleDesignation?: string;

  // Dates optionnelles
  dateCreation?: Date;
  dateModification?: Date;
}
