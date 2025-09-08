import {Ifonction} from "./ifonction";
import {Iateliers} from "./iateliers";

export interface Iemploye {

   id:number;
   nom:string;
   prenom:string;
   matricule:string;
   fonction:Ifonction;
   ateliers:Iateliers

}
