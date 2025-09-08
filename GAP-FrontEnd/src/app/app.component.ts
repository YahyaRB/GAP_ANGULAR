import {Component, OnInit} from '@angular/core';
import {TokenStorageService} from "./Auth/services/token-storage.service";
import {GlobalService} from "./Auth/services/global.service";
import {Iuser} from "./services/Interfaces/iuser";
import {Irole} from "./services/Interfaces/irole";
import {Iateliers} from "./services/Interfaces/iateliers";
import {UtilisateurService} from "./services/utilisateur.service";
import {IuserConnected} from "./services/Interfaces/iuser-connected";



@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{

  userConnected:Iuser;

  constructor(public globalVariableService:GlobalService,
              private tokenStorage:TokenStorageService,
              private userService:UtilisateurService) {
  }

  ngOnInit(): void {
    this.userConnected=this.tokenStorage.getUser();



  }




}
