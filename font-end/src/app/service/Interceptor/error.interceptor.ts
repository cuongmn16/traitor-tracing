import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NotificationService } from '../notification.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private notify: NotificationService) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Trích xuất message từ error response
        let errorMessage = 'Lỗi! Vui lòng thử lại.';
        let errorCode = error.status;

        if (error.error instanceof ErrorEvent) {
          // Client-side error
          errorMessage = error.error.message;
        } else if (error.error && typeof error.error === 'object') {
          // Server-side error - lấy message từ error.error.message
          errorMessage = error.error.message || error.error.msg || error.statusText;
          errorCode = error.error.code || error.status;
        } else if (error.statusText) {
          // Fallback to statusText
          errorMessage = error.statusText;
        }

        console.error('HTTP Error:', {
          code: errorCode,
          message: errorMessage,
          status: error.status,
          fullError: error
        });

        // Show toast
        try { this.notify.error(errorMessage); } catch (e) { /* ignore if notify not available */ }

        // Pass the normalized error to the caller
        const normalizedError = {
          code: errorCode,
          message: errorMessage,
          error: error
        };

        return throwError(() => normalizedError);
      })
    );
  }
}

