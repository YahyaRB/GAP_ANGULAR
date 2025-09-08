import {Component, EventEmitter, Input, Output, SimpleChanges, ViewChild} from '@angular/core';
import {Iuser} from "../../../services/Interfaces/iuser";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Iaffaire} from "../../../services/Interfaces/iaffaire";
import {Irole} from "../../../services/Interfaces/irole";
import {UtilisateurService} from "../../../services/utilisateur.service";
import {ListeUtilisateursComponent} from "../liste-utilisateurs/liste-utilisateurs.component";
import {NotificationService} from "../../../services/notification.service";
import {RoleService} from "../../../services/role.service";
import {Iateliers} from "../../../services/Interfaces/iateliers";

@Component({
  selector: 'app-update-utilisateur',
  templateUrl: './update-utilisateur.component.html',
  styleUrls: ['./update-utilisateur.component.css']
})
export class UpdateUtilisateurComponent {
  @Output() refreshTable = new EventEmitter<void>();
  @ViewChild('closebutton') closebutton;
  @Input()
  public user:Iuser;
  myFormUpdate:FormGroup;
  @Input()
  listeAteliers: Iateliers[] = [];
  @Input()
  listeRoles: Irole[] = [];
  @Input()
  Users:Iuser[]=[];
  listRolesUser:string[]=[];
  listAteliersUser:string[]=[];
  usernameExist:boolean=false;
  emailExist:boolean=false;
  constructor(private userservice: UtilisateurService,
              private formBuilder: FormBuilder,
              private notifyService : NotificationService,
              private roleService: RoleService) {}

  onUpdateUser() {
    this.userservice.existeByUsername(this.myFormUpdate.value.username).subscribe(username=>{
        if(username && this.myFormUpdate.value.username!=this.user.username){
          this.usernameExist=true;
          this.notifyService.showError("Ce username existe déjà !!", "Erreur Username");
        }
        else {
          this.usernameExist=false;
        }
      }
    );
    this.userservice.existeByEmail(this.myFormUpdate.value.email).subscribe(email=>{
        if(email && this.myFormUpdate.value.email!=this.user.email){
          this.emailExist=true;
          this.notifyService.showError("Cet email existe déjà !!", "Erreur Email");
        }else{
          this.emailExist=false;
        }
      }
    );
    if (!this.usernameExist && !this.emailExist) {
      this.userservice.updateUser(this.myFormUpdate.value, this.user.id).subscribe(
        data => {
          this.myFormUpdate.value.listeRoles.forEach(a => {
            this.userservice.addRoles(this.user.id, a).subscribe();
            this.myFormUpdate.value.listeAteliers.forEach(aff => {
              this.userservice.addAteliersToUser(this.user.id, aff).subscribe();
            });
            // alert('Utilisateur modifié avec succés');
            this.notifyService.showSuccess("Utilisateur modifié avec succés !!", "Modification Utilisateur");
            this.initmyUpdateForm();

              //this.userC.ngOnInit();
              this.refreshTable.emit();
              this.closebutton.nativeElement.click();

          });
        },
      );
    }
  }
  ngOnChanges(changes: SimpleChanges): void {
    if(this.user){
      this.affectUsertoForm(this.user.id);
    }

  }
  ngOnInit(): void {
    this.initmyUpdateForm();
  }
  private initmyUpdateForm() {
    this.myFormUpdate = this.formBuilder.group({
      username:['',Validators.required],
      email: ['',Validators.email],
      listeRoles:['',Validators.required],
      listeAteliers:['',Validators.required],
      matricule: [''],
      nom: ['',Validators.required],
      prenom: ['',Validators.required],
      session: ['',Validators.required],
    });

  }
  private affectUsertoForm(id:number){
    this.listAteliersUser=[];
    this.listRolesUser=[];
    this.userservice.getUserById(id).subscribe(data=> {
      this.user=data;
      data.roles.forEach(a=>{
        this.listRolesUser.push(a.name);
      });
      data.atelier.forEach(a=>{
        this.listAteliersUser.push(a.code);
      });
      this.myFormUpdate.setValue({
        username:this.user.username,
        email: this.user.email,
        listeRoles:this.listRolesUser,
        listeAteliers:this.listAteliersUser,
        matricule:this.user.matricule,
        nom: this.user.nom,
        prenom: this.user.prenom,
        session:this.user.session
      });
    });
  }

  onMaterialGroupChange($event: Event) {

  }
  hasRoleGroup(rolesToCheck:string[]):boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

}

