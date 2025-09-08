import {Iprojet} from "./iprojet";
import {Iemploye} from "./iemploye";

export interface Ideplacement {
 id : number;
 date : Date;
 nmbJours : number;
 motif : string;
 pieceJointe:string;
 employee : Iemploye[];
 projet : Iprojet;
}
