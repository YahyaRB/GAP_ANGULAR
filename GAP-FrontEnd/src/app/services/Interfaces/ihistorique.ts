import {Iuser} from "./iuser";
import {Iplan} from "./iplan";

export interface Ihistorique {
  id:number;
  indice:string;
  description:string;
  type:string;
  date:Date;
  faitPar:Iuser;
  validePar:Iuser;
  numeroPlan:Iplan;

}
