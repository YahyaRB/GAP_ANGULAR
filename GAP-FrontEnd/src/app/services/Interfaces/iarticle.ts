import {Iprojet} from "./iprojet";
import {Iateliers} from "./iateliers";

export interface Iarticle {
  id:number;
  numPrix:string;
  designation:string;
  quantiteTot:number;
  quantiteProd:number;
  quantiteEnProd:number;
  quantiteLivre:number;
  quantitePose:number;
  unite:string;
  projet:Iprojet;
  ateliers:Iateliers;
}
