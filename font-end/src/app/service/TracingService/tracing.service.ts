import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TracingService {
  private url = environment.tracingUrl;

  constructor(private http: HttpClient) {}

  health(): Observable<{ status: string }> {
    return this.http.get<{ status: string }>(`${this.url}/health`);
  }

  private authHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      Authorization: `Bearer ${token || ''}`
    });
  }

  /** Gọi trực tiếp Python qua gateway (admin / debug). Luồng chính vẫn dùng IntegrationService → Spring. */
  autoTrace(file: File, length: number): Observable<unknown> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('length', String(length));
    return this.http.post(`${this.url}/auto_trace`, formData, {
      headers: this.authHeaders()
    });
  }
}
