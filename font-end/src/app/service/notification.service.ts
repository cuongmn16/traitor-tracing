import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info';
export interface Toast { id: string; type: ToastType; message: string; timeout?: number }

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private toasts: Toast[] = [];
  private subject = new BehaviorSubject<Toast[]>([]);

  getToastStream() {
    return this.subject.asObservable();
  }

  private emit() { this.subject.next([...this.toasts]); }

  notify(type: ToastType, message: string, timeout = 4000) {
    const id = Math.random().toString(36).slice(2);
    const t: Toast = { id, type, message, timeout };
    this.toasts.push(t);
    this.emit();

    if (timeout && timeout > 0) {
      setTimeout(() => this.dismiss(id), timeout);
    }
  }

  success(message: string, timeout?: number) { this.notify('success', message, timeout); }
  error(message: string, timeout?: number) { this.notify('error', message, timeout); }
  info(message: string, timeout?: number) { this.notify('info', message, timeout); }

  dismiss(id: string) {
    this.toasts = this.toasts.filter(t => t.id !== id);
    this.emit();
  }

  clear() { this.toasts = []; this.emit(); }
}

