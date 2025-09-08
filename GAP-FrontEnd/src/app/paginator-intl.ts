// paginator-intl.ts
import { MatPaginatorIntl } from '@angular/material/paginator';

export class CustomMatPaginatorIntl extends MatPaginatorIntl {
  override itemsPerPageLabel = 'Articles par page :';
  override nextPageLabel = 'Page suivante';
  override previousPageLabel = 'Page précédente';

  override getRangeLabel = (page: number, pageSize: number, length: number) => {
    if (length === 0) {
      return '0 de 0';
    }
    const startIndex = page * pageSize;
    const endIndex = Math.min(startIndex + pageSize, length);
    return `${startIndex + 1} - ${endIndex} de ${length}`;
  };
}
