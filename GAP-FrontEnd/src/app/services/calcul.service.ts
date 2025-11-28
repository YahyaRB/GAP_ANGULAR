import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class CalculService {

    private baseUrl = 'http://localhost:8083/GAP-DEV/api/Calcul';

    constructor(private http: HttpClient) { }

    calculerParProjet(idUser: number, atelier: number, month: number, year: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}/CalculerParProjet?idUser=${idUser}&atelier=${atelier}&month=${month}&year=${year}`);
    }

    exportCalculs(atelier: number, month: number, year: number): Observable<Blob> {
        return this.http.get(`${this.baseUrl}/export/calculs?atelier=${atelier}&month=${month}&year=${year}`, { responseType: 'blob' });
    }

    exportDetails(calcul: any): Observable<Blob> {
        return this.http.post(`${this.baseUrl}/export/details`, calcul, { responseType: 'blob' });
    }
}
