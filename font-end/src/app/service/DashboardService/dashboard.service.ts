import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../admin/module/apiResponse';
import { environment } from '../../../environments/environment';

export interface DashboardStats {
  totalOriginalImages: number;
  totalDistributedCopies: number;
  totalViolationsDetected: number;
  totalManagedUsers: number;
  recentActivities: any[];
  recentAlerts: any[];
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private URL = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) { }

  getStats(): Observable<ApiResponse<DashboardStats>> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.get<ApiResponse<DashboardStats>>(`${this.URL}/stats`, { headers });
  }
}
