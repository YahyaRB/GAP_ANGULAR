import {IordreFabrication} from "./iordre-fabrication";

export interface IdetailOf {
  id:number;
  sousOfCode:string;
  quantite:number;
  quantiteLivre:number;
  description:string;
  ordreFabrication:IordreFabrication;
  compteur:number;
}
