import { Injectable } from '@angular/core';
import {Iuser} from "../../services/Interfaces/iuser";
import {IuserConnected} from "../../services/Interfaces/iuser-connected";


@Injectable({
  providedIn: 'root'
})
export class GlobalService {


  public isLogged:boolean=false;
  public userConnected:IuserConnected=null;



}
