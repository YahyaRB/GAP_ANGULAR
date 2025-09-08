import {Iateliers} from "./iateliers";
import {Iprojet} from "./iprojet";
import {Iarticle} from "./iarticle";
import {Iplan} from "./iplan";

export interface IordreFabrication {
  id:number;
  numOF:string;
  atelier:Iateliers;
  projet:Iprojet;
  date:Date;
  article:Iarticle;
  plan:Iplan;
  quantite:number;
  dateFin:Date;
  tempsPrevu:number;
  description:string;
  statut:string;
  compteur:number;
  avancement:number;
  pieceJointe:string;
  flag:number;
  qteRest:number;
  qteLivre:number

}
