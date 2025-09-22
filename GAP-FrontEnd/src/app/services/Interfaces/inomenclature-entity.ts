export interface INomenclatureEntity {
  id?: number;
  type: string;
  designation: string;
  unite: string;
  quantiteTot: number;        // ← Nom de l'entité Java
  quantiteRest: number;
  quantiteLivre: number;
  ordreFabricationId?: number;
}
