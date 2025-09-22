import {Ifonction} from "../../../services/Interfaces/ifonction";
import {Component, OnInit, ViewChild} from "@angular/core";
import {FonctionService} from "../../../services/fonction.service";
import {RoleService} from "../../../services/role.service";
import * as XLSX from 'xlsx';
import {ROLES_ADMIN_RH} from "../../../Roles";

@Component({
  selector: 'app-liste-fonctions',
  templateUrl: './liste-fonctions.component.html',
  styleUrls: ['./liste-fonctions.component.css']
})
export class ListeFonctionsComponent implements OnInit {
  @ViewChild(ListeFonctionsComponent) ListeUtilisateurs: ListeFonctionsComponent;
  POSTS: Ifonction[] = [];
  page: number = 1;
  count: number = 0;
  tableSize: number = 10;
  pfiltre: any;
  sortDirection: { [key: string]: boolean } = {};
  fonctions: Ifonction[] = [];

  fonctionSelected: Ifonction = null;

  constructor(
    private fonctionService: FonctionService,
    private roleService: RoleService
  ) {}

  ngOnInit(): void {
    this.postList();
  }



  hasRoleGroup(rolesToCheck: string[]): boolean {
    return this.roleService.hasRoleGroup(rolesToCheck);
  }

  onTableSizeChange(): void {
    this.page = 1;
  }

  recupSelected(fonction: Ifonction) {
    this.fonctionSelected = fonction;
  }

  hasRole(role: string): boolean {
    return this.roleService.hasRole(role);
  }


  sortColumn(column: string) {
    if (!(column in this.sortDirection)) {
      this.sortDirection[column] = true;
    }

    const isAscending = this.sortDirection[column];

    this.POSTS.sort((a, b) => {
      const aValue = this.resolvePath(a, column);
      const bValue = this.resolvePath(b, column);

      if (aValue < bValue) return isAscending ? -1 : 1;
      if (aValue > bValue) return isAscending ? 1 : -1;
      return 0;
    });

    this.sortDirection[column] = !isAscending;
  }

  resolvePath(obj: any, path: string) {
    return path.split('.').reduce((acc, key) => acc && acc[key], obj);
  }

  onTableDataChange(event: any) {
    this.page = event;
    this.postList();
  }

  postList(): void {
    this.fonctionService.getAllFonctions().subscribe(data => {
      this.POSTS = data;

    });
  }


  exportExel(): void {
    try {
      // Commencer avec toutes les données
      let filteredData = [...this.POSTS];

      // Appliquer le filtre de recherche textuel si présent
      if (this.pfiltre && this.pfiltre.trim() !== '') {
        const searchTerm = this.pfiltre.toLowerCase().trim();
        filteredData = filteredData.filter(fonction =>
          (fonction.codeFonction || 0)==(searchTerm) ||
          (fonction.designation?.toLowerCase() || '').includes(searchTerm) ||
          (fonction.typeCalcul?.toLowerCase() || '').includes(searchTerm) ||
          (fonction.id?.toString() || '').includes(searchTerm)
        );
      }

      if (filteredData.length === 0) {
        alert('Aucune donnée correspondant aux filtres à exporter');
        return;
      }

      // Préparer les données pour l'export
      const exportData = filteredData.map((fonction, index) => ({
        'N°': index + 1,
        'ID': fonction.id || '',
        'Code Fonction': fonction.codeFonction || '',
        'Désignation': fonction.designation || '',
        'Type de Calcul': fonction.typeCalcul || ''
      }));

      // Créer le workbook et la worksheet
      const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
      const wb: XLSX.WorkBook = XLSX.utils.book_new();

      // Définir la largeur des colonnes
      const colWidths = [
        { wch: 5 },   // N°
        { wch: 8 },   // ID
        { wch: 20 },  // Code Fonction
        { wch: 40 },  // Désignation
        { wch: 20 }   // Type de Calcul
      ];
      ws['!cols'] = colWidths;

      // Styliser les en-têtes
      const headerStyle = {
        font: { bold: true, color: { rgb: "FFFFFF" } },
        fill: { fgColor: { rgb: "4472C4" } },
        alignment: { horizontal: "center", vertical: "center" },
        border: {
          top: { style: "thin", color: { rgb: "000000" } },
          bottom: { style: "thin", color: { rgb: "000000" } },
          left: { style: "thin", color: { rgb: "000000" } },
          right: { style: "thin", color: { rgb: "000000" } }
        }
      };

      // Appliquer le style aux en-têtes (ligne 1)
      const headerCells = ['A1', 'B1', 'C1', 'D1', 'E1'];
      headerCells.forEach(cell => {
        if (ws[cell]) {
          ws[cell].s = headerStyle;
        }
      });

      // Ajouter des styles alternés pour les lignes de données
      const dataStyle = {
        alignment: { vertical: "center" },
        border: {
          top: { style: "thin", color: { rgb: "CCCCCC" } },
          bottom: { style: "thin", color: { rgb: "CCCCCC" } },
          left: { style: "thin", color: { rgb: "CCCCCC" } },
          right: { style: "thin", color: { rgb: "CCCCCC" } }
        }
      };

      // Appliquer le style aux données (à partir de la ligne 2)
      for (let row = 2; row <= exportData.length + 1; row++) {
        headerCells.forEach((_, colIndex) => {
          const cellAddress = XLSX.utils.encode_cell({ r: row - 1, c: colIndex });
          if (ws[cellAddress]) {
            ws[cellAddress].s = {
              ...dataStyle,
              fill: row % 2 === 0 ?
                { fgColor: { rgb: "F8F9FA" } } :
                { fgColor: { rgb: "FFFFFF" } }
            };
          }
        });
      }

      // Ajouter la worksheet au workbook
      XLSX.utils.book_append_sheet(wb, ws, 'Liste Fonctions');

      // Ajouter une feuille de résumé
      const typeCalculStats = this.getTypeCalculStatistics(filteredData);
      const summaryData = [
        { 'Information': 'Nombre total de fonctions', 'Valeur': filteredData.length },
        { 'Information': 'Fonctions avec code', 'Valeur': filteredData.filter(f => f.codeFonction && f.codeFonction !== 0).length },
        { 'Information': 'Types de calcul différents', 'Valeur': new Set(filteredData.map(f => f.typeCalcul).filter(type => type)).size },
        { 'Information': 'Fonctions sans type de calcul', 'Valeur': filteredData.filter(f => !f.typeCalcul || f.typeCalcul.trim() === '').length },
        { 'Information': 'Date d\'export', 'Valeur': new Date().toLocaleString('fr-FR') },
        { 'Information': 'Filtre de recherche', 'Valeur': this.pfiltre?.trim() || 'Aucun filtre' }
      ];

      // Ajouter les statistiques par type de calcul
      typeCalculStats.forEach(stat => {
        summaryData.push({
          'Information': `Fonctions "${stat.type}"`,
          'Valeur': stat.count
        });
      });

      const summaryWs: XLSX.WorkSheet = XLSX.utils.json_to_sheet(summaryData);
      summaryWs['!cols'] = [{ wch: 35 }, { wch: 20 }];

      // Styliser la feuille de résumé
      const summaryHeaderCells = ['A1', 'B1'];
      summaryHeaderCells.forEach(cell => {
        if (summaryWs[cell]) {
          summaryWs[cell].s = headerStyle;
        }
      });

      XLSX.utils.book_append_sheet(wb, summaryWs, 'Résumé');

      // Générer le nom du fichier avec la date
      const currentDate = new Date();
      const dateStr = currentDate.toISOString().split('T')[0];
      const timeStr = currentDate.toTimeString().split(' ')[0].replace(/:/g, '-');
      const fileName = `Fonctions_${dateStr}_${timeStr}.xlsx`;

      // Télécharger le fichier
      XLSX.writeFile(wb, fileName);

      // Message de succès
      console.log(`Export réussi: ${exportData.length} fonctions exportées`);

      // Si vous avez un service de toast/notification
      // this.toastr.success(`${exportData.length} fonctions exportées avec succès`, 'Export Excel');

    } catch (error) {
      console.error('Erreur lors de l\'export Excel:', error);
      alert('Erreur lors de l\'export Excel. Veuillez réessayer.');
    }
  }

// Méthode auxiliaire pour calculer les statistiques par type de calcul
  private getTypeCalculStatistics(data: any[]): { type: string; count: number }[] {
    const typeCalculMap = new Map<string, number>();

    data.forEach(fonction => {
      const type = fonction.typeCalcul || 'Non défini';
      typeCalculMap.set(type, (typeCalculMap.get(type) || 0) + 1);
    });

    return Array.from(typeCalculMap.entries())
      .map(([type, count]) => ({ type, count }))
      .sort((a, b) => b.count - a.count); // Trier par nombre décroissant
  }

  protected readonly ROLES_ADMIN_RH = ROLES_ADMIN_RH;
}
