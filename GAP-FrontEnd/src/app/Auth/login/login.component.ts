import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AuthService} from "../services/auth.service";
import {TokenStorageService} from "../services/token-storage.service";
import {Router} from "@angular/router";
import {first, skip} from "rxjs";
import {GlobalService} from "../services/global.service";
import {NotificationService} from "../../services/notification.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit{
  showPassword?: boolean = false;
  myFormUser:FormGroup;
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';
  roles: string[] = [];


  constructor( private formBuilder: FormBuilder,
               private tokenStorage: TokenStorageService,
               private router: Router,
               private authService: AuthService,
               public globalVariableService:GlobalService,
               private notifyService : NotificationService,
  ){}
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onAuthentification() {

    this.authService.login(this.myFormUser.value.username,this.myFormUser.value.password)
      .pipe(first())
      .subscribe(
        data => {
          this.tokenStorage.saveToken(data.accessToken);
          this.tokenStorage.saveUser(data);

          this.isLoginFailed = false;
          this.isLoggedIn = true;
          this.router.navigate(['navBar']).then(()=>
            window.location.reload()
          );
          this.roles = this.tokenStorage.getUser().roles;
          this.router.navigate(['/Acceuil']);
        },
        err => {

          this.notifyService.showError("Login ou mot de passe incorrect !!","Authentification");


          this.errorMessage = err.error.message;
          this.isLoginFailed = true;
        }

      );




  }

  ngOnInit(): void {
    this.globalVariableService.isLogged=false;
    this.initmyForm();
  }
  private initmyForm() {
    this.myFormUser = this.formBuilder.group({
      username:['',Validators.required],
      password: ['',Validators.required],
    });
  }



}
