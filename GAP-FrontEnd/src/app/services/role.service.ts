import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {TokenStorageService} from "../Auth/services/token-storage.service";
import {environment} from "../../environments/environment";
const AUTH_API = 'api/role';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class RoleService {

  constructor(private http: HttpClient,
              private storageService: TokenStorageService) {
  }
  getAllRoles(): Observable<any> {
    return this.http.get(environment.apiUrl + AUTH_API + '/ListeRoles');
  }

  getRoleByName(name: string):Observable<any>{
    return this.http.get<any>(environment.apiUrl + AUTH_API + '/roleByUserName/' + name, httpOptions);
  }
  hasRoleGroup(roles: string[]): boolean {
    for (let role of roles) {
      if (this.storageService.hasRole(role)) {
        return true;
      }
    }
    return false;
  }
  hasRole(role: string): boolean {

    if (this.storageService.hasRole(role)) {
      return true;

    }
    return false;
  }
}
