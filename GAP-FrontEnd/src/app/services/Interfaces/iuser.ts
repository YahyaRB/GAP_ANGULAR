import {Irole} from "./irole";
import {Iaffaire} from "./iaffaire";
import {Iateliers} from "./iateliers";

export interface Iuser {
  id : number;
  username: string;
  email : string;
  password:string;
  nom : string;
  prenom: string;
  session: string;
  matricule : string;
  roles: Irole[];
  atelier:Iateliers[];
}
