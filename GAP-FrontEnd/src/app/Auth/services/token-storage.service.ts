import { Injectable } from '@angular/core';

const TOKEN_KEY = 'auth-token';
const USER_KEY = 'auth-user';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  private userRoles!: string[];
  public isLoggIn: boolean = false;
  constructor() { }

  signOut(): void {
    this.isLoggIn = false;
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.clear();
  }

  public saveToken(token: string): void {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, token);
  }
  public getToken(): string | null {
    return window.sessionStorage.getItem(TOKEN_KEY);
  }
  public isLoggedIn() {
    if( this.getToken() != null ){
      this.isLoggIn = true;
      return true;
    }
    return  false;
  }
  public saveUser(user: any): void {
    window.sessionStorage.removeItem(USER_KEY);
    window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
  }
  public getRoles(): string[] {
    return this.getUser().roles || [];
  }
  public getUser(): any {
    const user = window.sessionStorage.getItem(USER_KEY);
    if (user) {
      return JSON.parse(user);
    }
    return { roles: [], ateliers: [], matricule: '' }; // Valeurs par défaut
  }

  hasRole(role: string ): boolean {
    this.userRoles = this.getRoles();
    return this.userRoles.includes(role); // Vérifier si l'utilisateur a le rôle spécifié
  }

}
