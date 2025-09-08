import { Component } from '@angular/core';
import {ROLES_ADMIN} from "../../Roles";
import {GlobalService} from "../../Auth/services/global.service";
import {TokenStorageService} from "../../Auth/services/token-storage.service";

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  showSideBar:boolean=true;
  subscription:string[]=ROLES_ADMIN;
  constructor(public globalVariableService:GlobalService,
              private storageService:TokenStorageService) {}
  hasRole(roles: string[]): boolean {
    for (let i=0;i<roles.length;i++){
      if(this.storageService.hasRole(roles[i])){
        return true
      }
    }
    return false;
  }
}
