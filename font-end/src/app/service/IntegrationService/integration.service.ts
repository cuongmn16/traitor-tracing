import { HttpClient, HttpHandler, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class IntegrationService {
  private URL = `${environment.apiUrl}/integration`;
  constructor(private http: HttpClient) { }

  downloadImage(imageId: string): Observable<Blob>{
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get(
      `${this.URL}/download/${imageId}`,
      {
        responseType: 'blob',
        headers
      }
    );
  }

  traceImage(file: File): Observable<any> {

    const formData = new FormData();

    formData.append('file', file);

    const token = localStorage.getItem('token');

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.post(
      `${this.URL}/trace-image`,
      formData,
      { headers }
    );
  }

}
