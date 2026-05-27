import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { User } from '../../admin/module/user';
import { UserService } from '../../service/UserService/user.service';
import { combineLatest } from 'rxjs';
import { Router } from '@angular/router';
import { ImageResponse } from '../../admin/module/apiResponse';
import { ImagesService } from '../../service/ImagesService/images.service';
import { IntegrationService } from '../../service/IntegrationService/integration.service';
export interface ImageModel {
  id: string;
  fileName: string;
  filePath: string;
  phash: string;
  createdAt: string;
  users: any[];

  // preview image
  previewUrl?: string;
}
@Component({
  selector: 'app-user-dashboard',
  imports: [CommonModule],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})
export class UserDashboardComponent implements OnInit {
  user?: User;
  images: ImageResponse[] = [];
  isLoading: boolean = false;
  selectedImage: any = null;
  constructor(
    private userService: UserService,
    private imageService: ImagesService,
    private integrationService: IntegrationService,
    private router: Router
  ) { }
  ngOnInit(): void {
    this.userService.getUser().subscribe({
      next: (data) => {
        this.user = data.result;
      }
    });
    
    this.loadImages();
  }

  loadImages(): void {
    this.isLoading = true;
    this.imageService.getAll().subscribe({
      next: (data) => { this.images = data; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  getFileFormat(fileName: string): string {
    if (!fileName) return 'IMG';
    return fileName.split('.').pop()?.toUpperCase() || 'IMG';
  }

  download(imageId: string, fileName: string) {

    this.integrationService.downloadImage(imageId).subscribe({
      next: (blob: Blob) => {
  
        const url = window.URL.createObjectURL(blob);
  
        const a = document.createElement('a');
  
        a.href = url;
  
        // tên file khi lưu
        const usernameStr = this.user?.username ? this.user.username + '_' : '';
        a.download = 'secure_' + usernameStr + fileName;
  
        // mở hộp thoại chọn nơi lưu
        document.body.appendChild(a);
        a.click();
  
        document.body.removeChild(a);
  
        window.URL.revokeObjectURL(url);
      },
  
      error: (err) => {
        console.error(err);
        alert('Download thất bại');
      }
    });
  }

  logout() {
    const comfirmLogout = confirm("Bạn có chắc muốn đăng xuất!")
    if (comfirmLogout) {
      alert("Đăng xuất thành côngcông")
      this.userService.logout();
      this.router.navigate(['/login']);
    }

  }

  openPreview(img: any) {
    this.selectedImage = img;
    setTimeout(() => {
      this.drawWatermarkedCanvas();
    }, 50);
  }
  
  closePreview() {
    this.selectedImage = null;
  }

  drawWatermarkedCanvas() {
    const canvas = document.getElementById('previewCanvas') as HTMLCanvasElement;
    if (!canvas || !this.selectedImage) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const img = new Image();
    img.crossOrigin = 'anonymous'; // Tránh lỗi CORS canvas
    img.src = this.selectedImage.imageUrl;

    img.onload = () => {
      // Đặt kích thước canvas bằng ảnh gốc
      canvas.width = img.naturalWidth;
      canvas.height = img.naturalHeight;

      // 1. Vẽ ảnh gốc lên canvas
      ctx.drawImage(img, 0, 0);

      // 2. Vẽ lưới vi phân độc bản của user (nếu có vân tay)
      const fingerprint = this.user?.fingerprint_bits;
      if (fingerprint && fingerprint.match(/^[01]+$/)) {
        this.applyVisualFingerprintOverlay(canvas, ctx, fingerprint);
      }
    };
  }

  applyVisualFingerprintOverlay(canvas: HTMLCanvasElement, ctx: CanvasRenderingContext2D, fingerprint: string) {
    const length = fingerprint.length;
    const blockSize = 4;
    const cols = Math.ceil(Math.sqrt(length));
    const rows = Math.ceil(length / cols);

    const patternW = cols * blockSize;
    const patternH = rows * blockSize;
    
    const W = canvas.width;
    const H = canvas.height;

    // Đặt màu nhạt (khoảng 6% opacity) để mắt người khó thấy nhưng máy dễ phát hiện khi diff
    ctx.fillStyle = 'rgba(255, 255, 255, 0.06)';

    // Hàm vẽ 1 pattern
    const drawPattern = (startX: number, startY: number) => {
      let idx = 0;
      for (let r = 0; r < rows; r++) {
        for (let c = 0; c < cols; c++) {
          if (idx >= length) break;
          if (fingerprint.charAt(idx) === '1') {
            ctx.fillRect(startX + c * blockSize, startY + r * blockSize, blockSize, blockSize);
          }
          idx++;
        }
      }
    };

    // Vẽ ở 4 góc
    drawPattern(0, 0); // Top-Left
    drawPattern(W - patternW, 0); // Top-Right
    drawPattern(0, H - patternH); // Bottom-Left
    drawPattern(W - patternW, H - patternH); // Bottom-Right
  }

  // viewImage(image: ImageModel) {
  //   window.open(image.previewUrl, '_blank');
  // }

  // downloadImage(image: ImageModel) {

  //   // demo download fake
  //   const link = document.createElement('a');

  //   link.href = image.previewUrl;
  //   link.download = image.fileName;

  //   link.click();

  //   alert('Downloading: ' + image.fileName);
  // }
  // ngOnInit(): void {
  //   this.getImages();
  // }

  // getImages() {
  //   this.http.get<ImageModel[]>('http://localhost:8080/api/images')
  //     .subscribe({
  //       next: (res) => {
  //         this.images = res.map(img => ({
  //           ...img,
  //           previewUrl: `http://localhost:8080/api/images/view/${img.id}`
  //         }));
  //       },
  //       error: (err) => {
  //         console.log(err);
  //       }
  //     });
  // }

  // viewImage(image: ImageModel) {
  //   window.open(
  //     `http://localhost:8080/api/images/view/${image.id}`,
  //     '_blank'
  //   );
  // }

  // downloadImage(image: ImageModel) {
  //   window.open(
  //     `http://localhost:8080/api/images/download/${image.id}`,
  //     '_blank'
  //   );
  // }
}
