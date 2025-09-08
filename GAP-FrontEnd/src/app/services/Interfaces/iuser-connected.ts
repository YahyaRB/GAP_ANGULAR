import {Irole} from "./irole";
import {Iateliers} from "./iateliers";

export interface IuserConnected {
  id : number;
  username: string;
  nom : string;
  prenom: string;
  session: string;
  roles: Irole[];

}
