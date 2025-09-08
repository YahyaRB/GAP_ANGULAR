import {Iemploye} from "./iemploye";
import {Iprojet} from "./iprojet";
import {Iateliers} from "./iateliers";
import {Iarticle} from "./iarticle";

export interface AffectationRequestDTO {

  date: Date;
  periode: string;
  nombreHeures: number;
  employees: Iemploye[];
  projets: Iprojet;
  ateliers: Iateliers;
  article: Iarticle;
}
