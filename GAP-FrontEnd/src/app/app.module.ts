import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ForgotPasswordComponent } from './Auth/forgot-password/forgot-password.component';
import { LoginComponent } from './Auth/login/login.component';
import { ResetPasswordComponent } from './Auth/reset-password/reset-password.component';
import { AcceuilComponent } from './Components/acceuil/acceuil.component';
import { SidebarComponent } from './Menus/sidebar/sidebar.component';
import { UserSidebarComponent } from './Menus/user-sidebar/user-sidebar.component';
import { NavbarComponent } from './Menus/navbar/navbar.component';
import { HttpClientModule } from "@angular/common/http";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { NgxPaginationModule } from "ngx-pagination";
import { NgSelectModule } from "@ng-select/ng-select";
import { ToastrModule } from "ngx-toastr";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { SwitcherComponent } from './Menus/switcher/switcher.component';
import { FooterComponent } from './Menus/footer/footer.component';
import { ListeUtilisateursComponent } from './Components/Utilisateurs/liste-utilisateurs/liste-utilisateurs.component';
import { AddUtilisateurComponent } from './Components/Utilisateurs/add-utilisateur/add-utilisateur.component';
import { UpdateUtilisateurComponent } from './Components/Utilisateurs/update-utilisateur/update-utilisateur.component';
import { DeleteUtilisateurComponent } from './Components/Utilisateurs/delete-utilisateur/delete-utilisateur.component';
import { MatSortModule } from "@angular/material/sort";
import { MatPaginatorIntl, MatPaginatorModule } from "@angular/material/paginator";
import { MatTableModule } from "@angular/material/table";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatIconModule } from "@angular/material/icon";
import { CustomMatPaginatorIntl } from "./paginator-intl";
import { ChangePasswordComponent } from './Components/Utilisateurs/change-password/change-password.component';
import { ListeChauffeursComponent } from './Components/Logistique/Chauffeurs/liste-chauffeurs/liste-chauffeurs.component';
import { AddChauffeurComponent } from './Components/Logistique/Chauffeurs/add-chauffeur/add-chauffeur.component';
import { UpdateChauffeurComponent } from './Components/Logistique/Chauffeurs/update-chauffeur/update-chauffeur.component';
import { DeleteChauffeurComponent } from './Components/Logistique/Chauffeurs/delete-chauffeur/delete-chauffeur.component';
import { ListeLivraisonLogComponent } from './Components/Logistique/Livraisons/liste-livraison-log/liste-livraison-log.component';
import { ListeLivraisonsComponent } from './Components/Livraison/liste-livraisons/liste-livraisons.component';
import { SearchPipe } from './search.pipe';
import { ListeProjetsComponent } from './Components/Projets/liste-projets/liste-projets.component';
import { AddProjetComponent } from './Components/Projets/add-projet/add-projet.component';
import { UpdateProjetComponent } from './Components/Projets/update-projet/update-projet.component';
import { DeleteProjetComponent } from './Components/Projets/delete-projet/delete-projet.component';
import { AddLivraisonComponent } from './Components/Livraison/add-livraison/add-livraison.component';
import { UpdateLivraisonComponent } from './Components/Livraison/update-livraison/update-livraison.component';
import { DeleteLivraisonComponent } from './Components/Livraison/delete-livraison/delete-livraison.component';
import { AffectationChauffeurComponent } from './Components/Logistique/Livraisons/affectation-chauffeur/affectation-chauffeur.component';
import { ListeArticlesComponent } from './Components/Articles/liste-articles/liste-articles.component';
import { AddArticleComponent } from './Components/Articles/add-article/add-article.component';
import { UpdateArticleComponent } from './Components/Articles/update-article/update-article.component';
import { DeleteArticleComponent } from './Components/Articles/delete-article/delete-article.component';
import { ListeAffectationsComponent } from './Components/Affectations/liste-affectations/liste-affectations.component';
import { UpdateAffectationComponent } from './Components/Affectations/update-affectation/update-affectation.component';
import { AddAffectationComponent } from './Components/Affectations/add-affectation/add-affectation.component';
import { DeleteAffectationComponent } from './Components/Affectations/delete-affectation/delete-affectation.component';
import { ListeDeplacementsComponent } from './Components/Deplacements/liste-deplacements/liste-deplacements.component';
import { AddDeplacementComponent } from './Components/Deplacements/add-deplacement/add-deplacement.component';
import { UpdateDeplacementComponent } from './Components/Deplacements/update-deplacement/update-deplacement.component';
import { DeleteDeplacementComponent } from './Components/Deplacements/delete-deplacement/delete-deplacement.component';
import { DeletePersonnelComponent } from './Components/Personnels/delete-personnel/delete-personnel.component';
import { UpdatePersonnelComponent } from './Components/Personnels/update-personnel/update-personnel.component';
import { AddPersonnelComponent } from './Components/Personnels/add-personnel/add-personnel.component';
import { ListePersonnelsComponent } from './Components/Personnels/liste-personnels/liste-personnels.component';
import { ListeFonctionsComponent } from './Components/Fonctions/liste-fonctions/liste-fonctions.component';
import { AddFonctionComponent } from './Components/Fonctions/add-fonction/add-fonction.component';
import { UpdateFonctionComponent } from './Components/Fonctions/update-fonction/update-fonction.component';
import { DeleteFonctionComponent } from './Components/Fonctions/delete-fonction/delete-fonction.component';
import { HashLocationStrategy, LocationStrategy } from "@angular/common";
import { ListeOFComponent } from './Components/OrdreFabrication/liste-of/liste-of.component';
import { AddOFComponent } from './Components/OrdreFabrication/add-of/add-of.component';
import { UpdateOFComponent } from './Components/OrdreFabrication/update-of/update-of.component';
import { DeleteOFComponent } from './Components/OrdreFabrication/delete-of/delete-of.component';
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { ScrollingModule } from '@angular/cdk/scrolling';
import { AddDetailLivraisonComponent } from './Components/DetailLivraison/add-detail-livraison/add-detail-livraison.component';
import { DuplicateAffectationComponent } from './Components/Affectations/duplicate-affectation/duplicate-affectation.component';
import { AddNomenclatureComponent } from './Components/Nomenclature/add-nomenclature/add-nomenclature.component';
import { UpdateNomenclatureComponent } from './Components/Nomenclature/update-nomenclature/update-nomenclature.component';
import { DeleteNomenclatureComponent } from './Components/Nomenclature/delete-nomenclature/delete-nomenclature.component';
import { DashboardComponent } from './Components/dashboard/dashboard.component';
import { ExportProgressModalComponent } from './Components/Affectations/export-progress-modal/export-progress-modal.component';






@NgModule({
  declarations: [
    AppComponent,
    ForgotPasswordComponent,
    LoginComponent,
    ResetPasswordComponent,
    AcceuilComponent,
    SidebarComponent,
    UserSidebarComponent,
    NavbarComponent,
    SwitcherComponent,
    FooterComponent,
    ListeUtilisateursComponent,
    AddUtilisateurComponent,
    UpdateUtilisateurComponent,
    DeleteUtilisateurComponent,
    ChangePasswordComponent,
    ListeChauffeursComponent,
    AddChauffeurComponent,
    UpdateChauffeurComponent,
    DeleteChauffeurComponent,
    ListeLivraisonLogComponent,
    ListeLivraisonsComponent,
    SearchPipe,
    ListeProjetsComponent,
    AddProjetComponent,
    UpdateProjetComponent,
    DeleteProjetComponent,
    AddLivraisonComponent,
    UpdateLivraisonComponent,
    DeleteLivraisonComponent,
    AffectationChauffeurComponent,
    ListeArticlesComponent,
    AddArticleComponent,
    UpdateArticleComponent,
    DeleteArticleComponent,
    ListeAffectationsComponent,
    UpdateAffectationComponent,
    AddAffectationComponent,
    DeleteAffectationComponent,
    ListeDeplacementsComponent,
    AddDeplacementComponent,
    UpdateDeplacementComponent,
    DeleteDeplacementComponent,
    DeletePersonnelComponent,
    UpdatePersonnelComponent,
    AddPersonnelComponent,
    ListePersonnelsComponent,
    ListeFonctionsComponent,
    AddFonctionComponent,
    UpdateFonctionComponent,
    DeleteFonctionComponent,
    ListeOFComponent,
    AddOFComponent,
    UpdateOFComponent,
    DeleteOFComponent,
    AddDetailLivraisonComponent,
    DuplicateAffectationComponent,
    AddNomenclatureComponent,
    UpdateNomenclatureComponent,
    DeleteNomenclatureComponent,
    DashboardComponent,
    ExportProgressModalComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    NgxPaginationModule,
    NgSelectModule,
    ToastrModule.forRoot(),
    BrowserAnimationsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatSnackBarModule,
    ScrollingModule

  ],
  providers: [
    { provide: MatPaginatorIntl, useClass: CustomMatPaginatorIntl },
    { provide: LocationStrategy, useClass: HashLocationStrategy }

  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
