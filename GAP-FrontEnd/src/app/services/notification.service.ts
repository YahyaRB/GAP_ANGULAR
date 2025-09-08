import { Injectable } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import {HttpHeaders} from "@angular/common/http";
const AUTH_API = 'api/affaire';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  constructor(private toastr: ToastrService) { }

  showSuccess(message, title){
    this.toastr.success(message, title);
  }

  showError(message, title){
    this.toastr.error(message, title);
  }

  showInfo(message, title){
    this.toastr.info(message, title)
  }

  showWarning(message, title){
    this.toastr.warning(message, title)
  }


}
