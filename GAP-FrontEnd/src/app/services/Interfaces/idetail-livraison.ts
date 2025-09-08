import { Iarticle } from "./iarticle";
import { Ilivraison } from "./ilivraison";
import { IordreFabrication } from "./iordre-fabrication";

export interface IdetailLivraison {
  id?: number;
  quantite: number;
  emplacement: string;
  observation: string;
  imprime: number; // 0 = false, 1 = true
  ordreFabrication: IordreFabrication;
  livraison: Ilivraison;
}
