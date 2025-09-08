import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Router} from "@angular/router";
import {TokenStorageService} from "../../Auth/services/token-storage.service";
import {GlobalService} from "../../Auth/services/global.service";
import {RoleService} from "../../services/role.service";
import {UpperCasePipe} from "@angular/common";
import {Iuser} from "../../services/Interfaces/iuser";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit,OnChanges{
  @Input()
  userCon:Iuser;
  constructor(private router:Router,
              private tokenStorage:TokenStorageService,
              public globalVariableService:GlobalService,
              private roleService:RoleService) {
  }
  logout(): void {
    this.tokenStorage.signOut();
    this.router.navigateByUrl('/Login');
  }



  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string):boolean {
    return this.roleService.hasRole(roleToCheck);
  }


  protected readonly UpperCasePipe = UpperCasePipe;

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
  }
}



