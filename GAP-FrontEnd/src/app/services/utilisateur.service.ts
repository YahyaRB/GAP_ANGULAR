import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {Iuser} from "./Interfaces/iuser";
import {HttpClient, HttpEvent, HttpHeaders} from "@angular/common/http";
import {environment} from "../../environments/environment";
const AUTH_API = 'api/user';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {

  constructor(private http: HttpClient) {}
  getAllUsers(): Observable<Iuser[]> {
    return this.http.get<Iuser[]>(environment.apiUrl + AUTH_API + '/Liste');
  }
  getUserById(id:number): Observable<Iuser>{
    return this.http.get<Iuser>(environment.apiUrl + AUTH_API + '/userById/'+id);
  }
  register(utilisateur:Iuser): Observable<Iuser> {
    return this.http.post<Iuser>(environment.apiUrl + AUTH_API + '/addUser', utilisateur, httpOptions);
  }
  addRoles(id,role) {
    return this.http.post(environment.apiUrl + AUTH_API + '/addRoles/'+id, role, httpOptions);
  }
  addAteliersToUser(id:number,idAtelier:number){
    return this.http.post(environment.apiUrl + AUTH_API + '/addAteliers/'+id, idAtelier, httpOptions);
  }
  getAffairesByUser(id:number){
    return this.http.get(environment.apiUrl + AUTH_API +"/affairesUserbyId/"+id,httpOptions)
  }
  deleteUser(id:number){
    return this.http.delete(environment.apiUrl + AUTH_API + '/Supprimer/'+id);
  }
  getUsersByRoleName(roleName:string):Observable<Iuser[]>{
    return this.http.get<Iuser[]>(environment.apiUrl + AUTH_API +"/userByRole/"+roleName);
  }
  updateSessionUser(id:number){
    return this.http.get(environment.apiUrl + AUTH_API + '/sessionUser/'+id);
  }
  updateUser(user:Iuser,id:number):Observable<Iuser>{
    return this.http.put<Iuser>(environment.apiUrl + AUTH_API +"/updateUser/"+id,user,httpOptions)
  }
  existeByUsername(username:string):Observable<boolean>{
    return this.http.get<boolean>(environment.apiUrl + AUTH_API+"/existeByUsername/"+username);
  }
  existeByEmail(email:string):Observable<boolean>{
    return this.http.get<boolean>(environment.apiUrl + AUTH_API+"/existeByEmail/"+email);
  }
  changePassword(id:number,newPassword:string):Observable<string>{
    return this.http.put<string>(environment.apiUrl+AUTH_API+"/changePassword/"+id,newPassword,httpOptions);
  }
  passwordIsValid(id:number,password:string):Observable<boolean>{
    return this.http.post<boolean>(environment.apiUrl+AUTH_API+"/verificationPassword/"+id,password,httpOptions);
  }
  resetPassword(resetToken : string, password : string):Observable<string> {
    return this.http.get<string>(environment.apiUrl+AUTH_API+"/resetPwd/"+resetToken+"/"+password);
  }

  forgetPassword(email :string):Observable<string> {
    return this.http.get<string>(environment.apiUrl + AUTH_API+"/forgetPassword/"+email);

  }
}
