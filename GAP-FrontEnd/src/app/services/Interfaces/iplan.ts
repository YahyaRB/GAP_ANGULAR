import {Ihistorique} from "./ihistorique";
import {Iuser} from "./iuser";
import {Iateliers} from "./iateliers";
import {Iarticle} from "./iarticle";
import {Iprojet} from "./iprojet";

export interface Iplan {
  id: number;
  statut: string;
  niveau: string;
  emplacement: string;
  datePlan: Date;
  dateFin: Date;
  pieceJointe: string;
  affaire: Iprojet;
  article: Iarticle;
  atelier: Iateliers;
  dessinePar: Iuser;
  controlPar: Iuser;
  listePlans: Iplan[];
  historiques: Ihistorique[];
}
