import { Injectable } from '@angular/core';
import {Observable} from "rxjs";

import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Router} from "@angular/router";
import {TokenStorageService} from "./token-storage.service";
import {environment} from "../../../environments/environment";
const AUTH_API = 'api/auth';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient,
              private router:Router,
              private tokenStorage:TokenStorageService
  ) { }

  login(username: string, password: string): Observable<any> {

    return this.http.post(environment.apiUrl + AUTH_API + '/signin', {
      username,
      password
    }, httpOptions);
  }
  register(username: string, password: string,nom:string,prenom: string,session: string,sexe: string,rolesUser:string[]): Observable<any> {
    return this.http.post(environment.apiUrl + AUTH_API + 'signup', {
      username, password, nom, prenom,session,sexe,rolesUser});
  }
  logout(): void {
    this.tokenStorage.signOut();
    this.router.navigateByUrl('/Login');
  }
  isAuthenticated(): boolean {
    const token = localStorage.getItem('token');
    if (token) {
      const expiry = (JSON.parse(atob(token.split('.')[1]))).exp;
      if ((Math.floor((new Date).getTime() / 1000)) >= expiry) {
        this.logout(); // Token expired, log out
        return false;
      }
      return true;
    }
    return false;
  }
}
