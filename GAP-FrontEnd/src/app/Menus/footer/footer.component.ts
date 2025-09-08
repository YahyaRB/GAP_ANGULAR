import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {TokenStorageService} from "../../Auth/services/token-storage.service";
import {GlobalService} from "../../Auth/services/global.service";
import {RoleService} from "../../services/role.service";

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent {
  constructor(public globalVariableService:GlobalService) {}
}
