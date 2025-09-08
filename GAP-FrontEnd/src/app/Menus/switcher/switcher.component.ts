import { Component } from '@angular/core';
import {GlobalService} from "../../Auth/services/global.service";

@Component({
  selector: 'app-switcher',
  templateUrl: './switcher.component.html',
  styleUrls: ['./switcher.component.css']
})
export class SwitcherComponent {
  constructor(public globalVariableService:GlobalService) {}
}
