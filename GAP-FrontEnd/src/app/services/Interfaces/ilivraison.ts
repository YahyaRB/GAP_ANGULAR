import {Ichauffeur} from "./ichauffeur";
import {Iateliers} from "./iateliers";
import {Iprojet} from "./iprojet";
import {IdetailLivraison} from "./idetail-livraison";

export interface Ilivraison {

  id: number;
  dateLivraison: Date;
  chauffeur: Ichauffeur;
  atelier: Iateliers;
  projet: Iprojet;
  detailLivraison: IdetailLivraison[];


}
