import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { DashboardService, DashboardStats } from '../../service/DashboardService/dashboard.service';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  currentDate = new Date();

  stats = [
    { label: 'Tổng số tệp gốc', value: '0', icon: 'description', color: '#2c7be5' },
    { label: 'Bản sao được phân phối', value: '0', icon: 'content_copy', color: '#00d97e' },
    { label: 'Vi phạm được phát hiện', value: '0', icon: 'warning', color: '#e63757' },
    { label: 'Người dùng được quản lý', value: '0', icon: 'person', color: '#f6c343' }
  ];

  chartData = [
    { day: 'Mon', percent: 60 },
    { day: 'Tue', percent: 40 },
    { day: 'Wed', percent: 55 },
    { day: 'Thu', percent: 80 },
    { day: 'Fri', percent: 90 },
    { day: 'Sat', percent: 70 },
    { day: 'Sun', percent: 45 }
  ];

  activities: any[] = [];
  alerts: any[] = [];

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats() {
    this.dashboardService.getStats().subscribe({
      next: (res) => {
        if (res.code === 1000 && res.result) {
          const data: DashboardStats = res.result;
          
          this.stats = [
            { label: 'Tổng số tệp gốc', value: data.totalOriginalImages.toString(), icon: 'description', color: '#2c7be5' },
            { label: 'Bản sao được phân phối', value: data.totalDistributedCopies.toString(), icon: 'content_copy', color: '#00d97e' },
            { label: 'Vi phạm được phát hiện', value: data.totalViolationsDetected.toString(), icon: 'warning', color: '#e63757' },
            { label: 'Người dùng được quản lý', value: data.totalManagedUsers.toString(), icon: 'person', color: '#f6c343' }
          ];

          // Áp dụng định dạng hiển thị cho hoạt động gần đây
          this.activities = data.recentActivities.map(act => {
            const timeDate = new Date(act.time);
            return {
              time: timeDate.toLocaleString('vi-VN'),
              action: act.action,
              content: act.content,
              user: act.user
            };
          });

          // Áp dụng định dạng hiển thị cho cảnh báo gần đây
          this.alerts = data.recentAlerts.map(al => {
            return {
              fileName: al.fileName,
              leakerId: al.leakerUsername || 'Unknown User'
            };
          });
        }
      },
      error: (err) => {
        console.error('Không thể load số liệu dashboard:', err);
      }
    });
  }
}
