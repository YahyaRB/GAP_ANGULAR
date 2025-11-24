import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AcceuilComponent } from "./Components/acceuil/acceuil.component";
import { LoginComponent } from "./Auth/login/login.component";
import { AuthGuard } from "./Auth/Guard/auth.guard";

import { ForgotPasswordComponent } from "./Auth/forgot-password/forgot-password.component";
import { ResetPasswordComponent } from "./Auth/reset-password/reset-password.component";
import { ListeUtilisateursComponent } from "./Components/Utilisateurs/liste-utilisateurs/liste-utilisateurs.component";
import { ROLES, ROLES_ADMIN } from "./Roles";
import { ListeChauffeursComponent } from "./Components/Logistique/Chauffeurs/liste-chauffeurs/liste-chauffeurs.component";
import {
  ListeLivraisonLogComponent
} from "./Components/Logistique/Livraisons/liste-livraison-log/liste-livraison-log.component";
import { ListeLivraisonsComponent } from "./Components/Livraison/liste-livraisons/liste-livraisons.component";
import { ListeProjetsComponent } from "./Components/Projets/liste-projets/liste-projets.component";
import { ListePersonnelsComponent } from "./Components/Personnels/liste-personnels/liste-personnels.component";
import { ListeAffectationsComponent } from "./Components/Affectations/liste-affectations/liste-affectations.component";
import { ListeDeplacementsComponent } from "./Components/Deplacements/liste-deplacements/liste-deplacements.component";
import { ListeArticlesComponent } from "./Components/Articles/liste-articles/liste-articles.component";
import { ListeFonctionsComponent } from "./Components/Fonctions/liste-fonctions/liste-fonctions.component";
import { ListeOFComponent } from "./Components/OrdreFabrication/liste-of/liste-of.component";
import { DashboardComponent } from "./Components/dashboard/dashboard.component";


const routes: Routes = [

  { path: 'Login', component: LoginComponent },
  {
    path: 'Acceuil', component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'Dashboard', component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  {
    path: '', component: DashboardComponent,
    canActivate: [AuthGuard]
  },
  { path: 'ForgotPassword', component: ForgotPasswordComponent },
  { path: 'ResetPassword', component: ResetPasswordComponent },
  {
    path: 'Utilisateurs', component: ListeUtilisateursComponent,
    canActivate: [AuthGuard], data: { expectedRoles: ROLES_ADMIN }
  },
  {
    path: 'Chauffeurs', component: ListeChauffeursComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN, ROLES.CONSULTEUR, ROLES.LOGISTIQUE] }
  },
  {
    path: 'AffectationLivraison', component: ListeLivraisonLogComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN, ROLES.CONSULTEUR, ROLES.LOGISTIQUE] }
  },
  {
    path: 'Livraisons', component: ListeLivraisonsComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN, ROLES.CONSULTEUR, ROLES.AGENTSAISIE] }
  },
  {
    path: 'Projets', component: ListeProjetsComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN] }
  },
  {
    path: 'Personnels', component: ListePersonnelsComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN, ROLES.CONSULTEUR, ROLES.AGENTSAISIE] }
  },
  {
    path: 'Affectations', component: ListeAffectationsComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN, ROLES.CONSULTEUR, ROLES.AGENTSAISIE] }
  },
  {
    path: 'Deplacements', component: ListeDeplacementsComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN, ROLES.CONSULTEUR, ROLES.AGENTSAISIE] }
  },
  {
    path: 'Articles', component: ListeArticlesComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN, ROLES.CONSULTEUR, ROLES.AGENTSAISIE] }
  },
  {
    path: 'Ordres_Fabrications', component: ListeOFComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN, ROLES.CONSULTEUR, ROLES.AGENTSAISIE] }
  },
  {
    path: 'Fonctions', component: ListeFonctionsComponent,
    canActivate: [AuthGuard], data: { expectedRoles: [ROLES.ADMIN, ROLES.CONSULTEUR, ROLES.AGENTSAISIE, ROLES.RH] }
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
