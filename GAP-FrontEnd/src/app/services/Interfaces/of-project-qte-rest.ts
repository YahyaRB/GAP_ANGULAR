import {Iarticle} from "./iarticle";

export interface OfProjectQteRest {
  id: number;
  numOF: string;
  designation: string;
  qteRest: number;
  quantite?: number;
  avancement?: number;
  projet?: {
    id: number;
    nom: string;
    description?: string;
  };
  article?: Iarticle;
  statut?: string;
  dateCreation?: Date;
  dateEcheance?: Date;
}
