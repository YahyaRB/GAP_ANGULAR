import { Injectable } from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router} from '@angular/router';
import { Observable } from 'rxjs';
import { TokenStorageService } from '../services/token-storage.service';
import {GlobalService} from "../services/global.service";


@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private tokenstorage: TokenStorageService,
    private router: Router,
    public globalVariableService:GlobalService
  ) {}
  canActivate( route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    if (!this.tokenstorage.isLoggedIn()) {
      this.globalVariableService.isLogged=false;
      this.router.navigate(['/Login']);
      return false;
    } else {
      this.globalVariableService.isLogged=true;
      return this.isAtuhorized(route);
    }
  }
  private isAtuhorized(route: ActivatedRouteSnapshot): boolean {

    const expectedRoles = route.data['expectedRoles'];
    if (expectedRoles && expectedRoles.length > 0) {
      const userRoles: any = this.tokenstorage.getRoles();
      for (let i = 0; i < userRoles.length; i++) {
        for (let j = 0; j < expectedRoles.length; j++) {
          if ( userRoles[i] === expectedRoles[j]) {

            return true;
          }
        }
      }
      return false;
    }
    else{
      return true;
    }

  }

}
