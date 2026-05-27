import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../../admin/module/user';
import { ApiResponse } from '../../admin/module/apiResponse';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private Url = `${environment.apiUrl}/users`;
  constructor(private http: HttpClient) { }

  login(data: any): Observable<ApiResponse<any>>{
    return this.http.post<ApiResponse<any>>(`${this.Url}/token`, data);
  }

  register(data: any): Observable<ApiResponse<any>>{
    console.log(data);
    return this.http.post<ApiResponse<any>>(`${this.Url}`, data);
  }

  saveToken(token: string) {
    localStorage.setItem('token', token);
  }

  getToken() {
    return localStorage.getItem('token');
  }

  logout() {
    localStorage.removeItem('token');
  }


  getUsers(): Observable<ApiResponse<User[]>>{
    const headers = this.getHeaders();
    return this.http.get<ApiResponse<User[]>>(this.Url, { headers });
  }

  getUser(): Observable<ApiResponse<User>>{
    const headers = this.getHeaders();
    return this.http.get<ApiResponse<User>>(`${this.Url}/myInfo`, { headers });
  }

  createUser(data: User): Observable<ApiResponse<User>> {
    const headers = this.getHeaders();
    return this.http.post<ApiResponse<User>>(this.Url, data, { headers });
  }

  update(id: string, data: any) {
    const headers = this.getHeaders();
    return this.http.put(`${this.Url}/${id}`, data, { headers });
  }

  delete(id: string) {
    const headers = this.getHeaders();
    return this.http.delete(`${this.Url}/${id}`, { headers });
  }
  private getHeaders(): HttpHeaders{
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

  }
  getRoleFromToken(token: string): string | null {
    if (!token) return null;

    try {
      const payloadBase64 = token.split('.')[1];
      const payloadJson = window.atob(payloadBase64);
      const payload = JSON.parse(payloadJson);

      return payload.role;
    } catch (e) {
      console.log(e);
      return null;
    }
  }

}
