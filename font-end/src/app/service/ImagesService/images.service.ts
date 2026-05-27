import { HttpClient, HttpEventType, HttpHeaders, HttpRequest, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { filter, map, Observable } from 'rxjs';
import { ApiResponse, ImageRequest, ImageResponse, UploadProgress } from '../../admin/module/apiResponse';
import { environment, mediaUrl } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ImagesService {

  private URL = `${environment.apiUrl}/images`;

  constructor(private http: HttpClient) { }

  upload(file: File): Observable<UploadProgress> {

    const formData = new FormData();
    formData.append('file', file);

    const headers = this.getHeaders();
    const req = new HttpRequest(
      'POST',
      `${this.URL}/upload`,
      formData,
      {
        reportProgress: true,
        headers
      }
    );

    return this.http.request<ApiResponse<ImageResponse>>(req).pipe(

      filter(event =>
        event.type === HttpEventType.UploadProgress ||
        event.type === HttpEventType.Response
      ),

      map(event => {

        if (event.type === HttpEventType.UploadProgress) {

          const progress = event.total
            ? Math.round(100 * event.loaded / event.total)
            : 0;

          return { progress };
        }

        const response = event as HttpResponse<ApiResponse<ImageResponse>>;

        return {
          progress: 100,
          data: response.body?.result as ImageResponse,
        };
      })
    );
  }

  // create(request: ImageRequest): Observable<ImageResponse> {
  //   return this.http.post<ApiResponse<ImageResponse>>(this.URL, request)
  //     .pipe(map(res => res.result!));
  // }

  getAll(): Observable<ImageResponse[]> {
    const headers = this.getHeaders();
    return this.http
      .get<ApiResponse<ImageResponse[]>>(this.URL,{headers})
      .pipe(
        map(res => {
  
          return res.result!.map(item => ({
  
            ...item,
  
            imageUrl: mediaUrl(item.filePath)
  
          }));
  
        })
      );
  }

  getById(id: string): Observable<ImageResponse> {
    return this.http
      .get<ApiResponse<ImageResponse>>(`${this.URL}/${id}`)
      .pipe(map(res => res.result!));
  }

  update(id: string, request: ImageRequest): Observable<ImageResponse> {
    const headers = this.getHeaders();
    return this.http
      .put<ApiResponse<ImageResponse>>(`${this.URL}/${id}`, request, {headers})
      .pipe(map(res => res.result!));
  }

  delete(id: string): Observable<string> {
    const headers = this.getHeaders();
    return this.http.delete<ApiResponse<string>>(`${this.URL}/${id}`,{headers})
      .pipe(
        map(res => res.result!)
      );
  }

  private getHeaders(): HttpHeaders{
    const token = localStorage.getItem('token'); 
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

  }
}
