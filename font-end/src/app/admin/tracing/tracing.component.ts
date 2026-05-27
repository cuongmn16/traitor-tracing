import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { IntegrationService } from '../../service/IntegrationService/integration.service';
import { ImagesService } from '../../service/ImagesService/images.service';
import { ImageResponse } from '../../admin/module/apiResponse';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tracing',
  imports: [CommonModule, FormsModule],
  templateUrl: './tracing.component.html',
  styleUrl: './tracing.component.css'
})
export class TracingComponent implements OnInit {
  selectedFile!: File;
  loading = false;
  result: any = null;
  notification = '';
  notificationType = 'success';
  currentDate = new Date();

  constructor(
    private traceService: IntegrationService,
    private imagesService: ImagesService
  ) {}

  ngOnInit() {
  }

  onFileSelected(event: any) {

    const file = event.target.files[0];

    if (file) {

      this.selectedFile = file;

      this.showNotification(
        'Tệp đã được chọn thành công',
        'success'
      );
    }
  }

  traceImage() {
    if (!this.selectedFile) {
      this.showNotification(
        'Vui lòng chọn hình ảnh cần truy vết',
        'error'
      );
      return;
    }

    this.loading = true;
    this.result = null;

    this.traceService.traceImage(this.selectedFile).subscribe({
      next: (res) => {
        this.result = res;
        this.loading = false;

        if (res.status === 'FOUND') {
          this.showNotification(
            'Đã tìm thấy tài khoản nghi phạm vi phạm!',
            'success'
          );
        } else {
          this.showNotification(
            res.error || 'Không tìm thấy người dùng trùng khớp vượt ngưỡng an toàn.',
            'warning'
          );
        }
      },

      error: (err) => {
        this.loading = false;
        console.error(err);
        this.showNotification(
          'Quá trình truy vết thất bại, vui lòng thử lại',
          'error'
        );
      }
    });
  }

  showNotification(
    message: string,
    type: string
  ) {

    this.notification = message;

    this.notificationType = type;

    setTimeout(() => {
      this.notification = '';
    }, 4000);
  }

  printReport() {
    window.print();
  }
}
