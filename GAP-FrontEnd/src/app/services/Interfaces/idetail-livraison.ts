import { Iarticle } from "./iarticle";
import { Ilivraison } from "./ilivraison";
import { IordreFabrication } from "./iordre-fabrication";
import {INomenclature} from "./inomenclature";

export interface IdetailLivraison {
  id?: number;
  quantite: number;
  emplacement: string;
  observation: string;
  ordreFabrication?: IordreFabrication;
  livraison?: Ilivraison;

  // Nouvelles propriétés ajoutées
  typeDetail?: string; // 'OF_COMPLET' ou 'NOMENCLATURE'
  nomenclature?: INomenclature; // Référence vers la nomenclature
}
