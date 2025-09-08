import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {Iuser} from "../../../services/Interfaces/iuser";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {UtilisateurService} from "../../../services/utilisateur.service";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {ROLES_ADMIN} from "../../../Roles";

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public user:Iuser;
  myFormAdd:FormGroup;
  showPassword: any;
  showconfirmPassword: any;

  constructor(private userService:UtilisateurService,
              private formBuilder: FormBuilder,
              private notifyService: NotificationService,
              private roleService: RoleService) {
  }

  onSubmit() {
    this.userService.changePassword(this.user.id,this.myFormAdd.value.password).subscribe(data=>{
          this.notifyService.showSuccess("Mot de passe modifié avec succés !!", "Changement Mot de Passe")
        }
    );

      this.closebutton.nativeElement.click();

  }

  private initMyForm(){
    this.myFormAdd = this.formBuilder.group({
      confirmpassword:['',Validators.required],
      password: ['',Validators.required],

    }, {validator: this.passwordMatchValidator});
  }
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }
  toggleConfirmPasswordVisibility(): void {
    this.showconfirmPassword = !this.showconfirmPassword;
  }

  passwordMatchValidator(formGroup: FormGroup) {



    const passwordControl = formGroup.get('password');
    const confirmPasswordControl = formGroup.get('confirmpassword');

    if (passwordControl.value !== confirmPasswordControl.value) {
      confirmPasswordControl.setErrors({passwordMismatch: true});
    } else {
      confirmPasswordControl.setErrors(null);
    }
  }


  ngOnInit(): void {
    this.initMyForm();
  }

  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  protected readonly ROLES_ADMIN = ROLES_ADMIN;
}


