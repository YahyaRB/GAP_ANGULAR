import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {ROLES_ADMIN} from "../../../Roles";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {ListeUtilisateursComponent} from "../liste-utilisateurs/liste-utilisateurs.component";
import {UtilisateurService} from "../../../services/utilisateur.service";
import {RoleService} from "../../../services/role.service";
import {Iuser} from "../../../services/Interfaces/iuser";
import {Irole} from "../../../services/Interfaces/irole";
import {Iaffaire} from "../../../services/Interfaces/iaffaire";
import {NotificationService} from "../../../services/notification.service";
import {Iateliers} from "../../../services/Interfaces/iateliers";

@Component({
  selector: 'app-add-utilisateur',
  templateUrl: './add-utilisateur.component.html',
  styleUrls: ['./add-utilisateur.component.css']
})
export class AddUtilisateurComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  myFormAdd: FormGroup;
  confirmpassword: null;
  @Input()
  listeAteliers: Iateliers[] = [];
  @Input()
  listeRoles: Irole[] = [];
  @Input()
  Users:Iuser[]=[];
  user: Iuser;
  showPassword?: boolean = false;
  showconfirmPassword?: boolean = false;
  usernameExist: boolean = false;
  emailExist: boolean = false;
  constructor(private notifyService: NotificationService,
              private formBuilder: FormBuilder,
              private userservice: UtilisateurService,
              private roleService: RoleService

  ) {
  }
  onMaterialGroupChange(event) {}
  onAdd() {
    this.userservice.existeByUsername(this.myFormAdd.value.username).subscribe(username => {
        this.usernameExist = username;
        if (this.usernameExist) {
          this.notifyService.showError("Ce username existe déjà !!", "Erreur Username");
        }
      }
    );
    this.userservice.existeByEmail(this.myFormAdd.value.email).subscribe(email => {
        this.emailExist = email;
        if (email) {
          this.notifyService.showError("Cet email existe déjà !!", "Erreur Email");
        }
      }
    );
    if (!this.usernameExist && !this.emailExist) {
      this.userservice.register(this.myFormAdd.value).subscribe(
        data => {
          this.user = data;
          this.myFormAdd.value.listeRoles.forEach(a => {
            this.userservice.addRoles(data.id, a).subscribe();

            this.myFormAdd.value.listeAteliers.forEach(aff => {
              this.userservice.addAteliersToUser(data.id, aff).subscribe();
            });
            this.notifyService.showSuccess("Utilisateur ajouté avec succés !!", "Ajout Utilisateur");
            this.initmyForm();
              this.closebutton.nativeElement.click();
              this.refreshTable.emit();

          });
        },
      );
    }
  }

  private initmyForm() {
    this.myFormAdd = this.formBuilder.group({
      username: ['', Validators.required],
      email: ['', Validators.email],
      listeRoles: ['', Validators.required],
      listeAteliers: ['', Validators.required],
      password: ['', Validators.required],
      confirmpassword: ['', Validators.required],
      matricule: [''],
      session: ['Actif',Validators.required],
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
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
    this.initmyForm();
  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  hasRole(roleToCheck: string):boolean {
    return this.roleService.hasRole(roleToCheck);
  }

  protected readonly ROLES_ADMIN = ROLES_ADMIN;

}
