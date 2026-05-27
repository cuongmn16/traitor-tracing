import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Toast } from '../../service/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
  <div class="toast-container">
    <div *ngFor="let t of toasts" class="toast" [ngClass]="'toast--' + t.type">
      <div class="toast__icon">{{ t.type === 'success' ? '✓' : t.type === 'error' ? '✕' : 'ℹ' }}</div>
      <div class="toast__body">
        <div class="toast__message">{{ t.message }}</div>
      </div>
      <button class="toast__close" (click)="dismiss(t.id)">✕</button>
    </div>
  </div>
  `,
  styles: [
    `
    .toast-container {
      position: fixed;
      top: 16px;
      right: 16px;
      display: flex;
      flex-direction: column;
      gap: 10px;
      z-index: 9999;
      pointer-events: none;
    }
    .toast {
      display: flex;
      align-items: center;
      min-width: 280px;
      max-width: 420px;
      background: #fff;
      color: #111;
      border-radius: 8px;
      box-shadow: 0 6px 18px rgba(0,0,0,0.12);
      padding: 12px 12px;
      gap: 12px;
      pointer-events: auto;
      overflow: hidden;
      transform-origin: right top;
      animation: toast-in .18s ease-out;
    }
    .toast__icon {
      font-weight: 700;
      width: 28px;
      height: 28px;
      display:flex;align-items:center;justify-content:center;
      border-radius:6px;
      background: rgba(0,0,0,0.06);
    }
    .toast__message { font-size: 14px; }
    .toast__close { background: transparent; border: none; cursor: pointer; color: #666; font-size: 14px; }

    .toast--success { border-left: 4px solid #16a34a; }
    .toast--error { border-left: 4px solid #dc2626; }
    .toast--info { border-left: 4px solid #0ea5e9; }

    @keyframes toast-in {
      from { transform: translateX(12px) scale(.98); opacity: 0 }
      to { transform: translateX(0) scale(1); opacity: 1 }
    }
    `
  ]
})
export class ToastComponent implements OnDestroy {
  toasts: Toast[] = [];
  private sub: any;

  constructor(private notify: NotificationService) {
    this.sub = this.notify.getToastStream().subscribe((list: Toast[]) => this.toasts = list);
  }

  dismiss(id: string) {
    this.notify.dismiss(id);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}

