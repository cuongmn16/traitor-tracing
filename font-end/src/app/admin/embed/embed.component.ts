import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ImagesService } from '../../service/ImagesService/images.service';
import { ImageRequest, ImageResponse } from '../module/apiResponse';

@Component({
  selector: 'app-embed',
  imports: [CommonModule, FormsModule],
  templateUrl: './embed.component.html',
  styleUrl: './embed.component.css'
})
export class EmbedComponent implements OnInit{
  isLoading: boolean = false;
  showUpdateModel: boolean = false;
  selectedImage: any = null;
  uploadProgress: number | null = null;
  isUploading: boolean = false;

  images: ImageResponse[] = []; 

  constructor(private imageService: ImagesService) {}

  ngOnInit(): void {
    this.loadImages();
  }

  loadImages(): void {
    this.isLoading = true;
    this.imageService.getAll().subscribe({
      next: (data) => { this.images = data; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    
    if (input.files && input.files[0]) {
      const file = input.files[0];
      
      this.isUploading = true;
      this.uploadProgress = 0;

      this.imageService.upload(file).subscribe({
        next: (res) => {
          this.uploadProgress = res.progress;

          if (res.data) {
            const backendImage: ImageResponse = res.data;

            this.images.unshift(backendImage);
            
            this.resetUploadStatus();
            input.value = '';
            this.loadImages();
          }
        },
        error: (err) => {
          console.error('Upload thất bại:', err);
          alert('Có lỗi xảy ra trong quá trình upload ảnh!');
          this.resetUploadStatus();
          input.value = '';
        }
      });
    }
  }

  onDelete(id: string) { 
    const confirmDelete = confirm("Bạn có chắc chắn xóa ảnh này không");
    if(confirmDelete){
      this.imageService.delete(id).subscribe({
        next: (deletedId) => {
          console.log('Xóa thành công mục có ID:', deletedId);
          
          this.loadImages();
          
          alert('Xóa thành công!');
        },
        error: (err) => {
          console.error('Lỗi khi xóa:', err);
          alert('Có lỗi xảy ra khi xóa. Vui lòng thử lại!');
        }
      });
    }
  }
  onUpdate(id: string) {
    const found = this.images.find(
      img => img.id === id
    );

    if (found) {
      this.selectedImage = { ...found };
      this.showUpdateModel = true;
    }
  }

  closeModal() {

    this.showUpdateModel = false;
  }
  
  saveUpdate() {
    console.log(this.showUpdateModel);
    this.closeModal();
  
    const request: ImageRequest = {
      fileName: this.selectedImage.fileName,
      filePath: this.selectedImage.filePath,
      phash: this.selectedImage.phash
    };

    this.imageService
      .update(this.selectedImage.id, request)
      .subscribe({
        next: (response: ImageResponse) => {
          console.log('Update thành công', response);

          alert('Cập nhật thành công');

          this.closeModal();
          this.loadImages();
        },

        error: (err) => {
          console.error('Lỗi update', err);
          alert('Cập nhật thất bại');
        }

      });
  }

  private resetUploadStatus() {
    this.isUploading = false;
    this.uploadProgress = null;
  }


  getFileFormat(fileName: string): string {
    if (!fileName) return 'IMG';
    return fileName.split('.').pop()?.toUpperCase() || 'IMG';
  }

}
