import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ImageResponse } from '../module/apiResponse';
import { ImagesService } from '../../service/ImagesService/images.service';
import { FormsModule } from '@angular/forms';
import { mediaUrl } from '../../../environments/environment';

@Component({
  selector: 'app-original-resources',
  imports: [CommonModule, FormsModule],
  templateUrl: './original-resources.component.html',
  styleUrl: './original-resources.component.css'
})
export class OriginalResourcesComponent implements OnInit{
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
 
  images: ImageResponse[] = [];
  selectedImage: ImageResponse | null = null;
  isLoading = false;
  isUploading = false;
  uploadProgress = 0;
  isDragging = false;
  viewMode: 'grid' | 'list' = 'grid';
  searchTerm = '';
  toast: { message: string; type: 'success' | 'error' | 'info' } | null = null;
  showEditModal = false;
  showDeleteConfirm = false;
  editForm = { fileName: '', filePath: '', phash: '' };
  deleteTargetId: string | null = null;
  previewImage: ImageResponse | null = null;
  ripples: { id: number; x: number; y: number }[] = [];
  private rippleCounter = 0;
 
  constructor(private imageService: ImagesService) {}
 
  ngOnInit(): void {
    this.loadImages();
  }
 
  get filteredImages(): ImageResponse[] {
    if (!this.searchTerm) return this.images;
    const term = this.searchTerm.toLowerCase();
    return this.images.filter(img =>
      img.fileName.toLowerCase().includes(term) ||
      img.phash.toLowerCase().includes(term)
    );
  }
 
  loadImages(): void {
    this.isLoading = true;
    this.imageService.getAll().subscribe({
      next: (data) => { this.images = data; this.isLoading = false; },
      error: () => { this.showToast('Không thể tải danh sách ảnh', 'error'); this.isLoading = false; }
    });
  }
 
  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.uploadFile(input.files[0]);
  }
 
  onDragOver(event: DragEvent): void { event.preventDefault(); this.isDragging = true; }
  onDragLeave(): void { this.isDragging = false; }
 
  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging = false;
    const file = event.dataTransfer?.files[0];
    if (file?.type.startsWith('image/')) this.uploadFile(file);
    else this.showToast('Chỉ hỗ trợ file ảnh!', 'error');
  }
 
  uploadFile(file: File): void {
    this.isUploading = true;
    this.uploadProgress = 0;
 
    this.imageService.upload(file).subscribe({
      next: ({ progress, data }) => {
        this.uploadProgress = progress;
        if (data) {
          data.imageUrl = mediaUrl(data.filePath);
          this.images.unshift(data);
          this.isUploading = false;
          this.uploadProgress = 0;
          this.showToast('Upload thành công: ' + data.fileName, 'success');
          if (this.fileInput) this.fileInput.nativeElement.value = '';
        }
      },
      error: () => {
        this.showToast('Upload thất bại!', 'error');
        this.isUploading = false;
        this.uploadProgress = 0;
      }
    });
  }
 
  openEdit(image: ImageResponse, event: MouseEvent): void {
    this.addRipple(event);
    this.selectedImage = image;
    this.editForm = { fileName: image.fileName, filePath: image.filePath, phash: image.phash };
    this.showEditModal = true;
  }
 
  saveEdit(): void {
    if (!this.selectedImage) return;
    this.imageService.update(this.selectedImage.id, this.editForm).subscribe({
      next: (updated) => {
        const idx = this.images.findIndex(i => i.id === this.selectedImage!.id);
        if (idx !== -1) this.images[idx] = updated;
        this.showEditModal = false;
        this.showToast('Cập nhật thành công!', 'success');
      },
      error: () => this.showToast('Cập nhật thất bại!', 'error')
    });
  }
 
  confirmDelete(id: string, event: MouseEvent): void {
    this.addRipple(event);
    this.deleteTargetId = id;
    this.showDeleteConfirm = true;
  }
 
  deleteImage(): void {
    if (!this.deleteTargetId) return;
    this.imageService.delete(this.deleteTargetId).subscribe({
      next: () => {
        this.images = this.images.filter(i => i.id !== this.deleteTargetId);
        this.showDeleteConfirm = false;
        this.deleteTargetId = null;
        this.showToast('Đã xóa ảnh!', 'success');
      },
      error: () => this.showToast('Xóa thất bại!', 'error')
    });
  }
 
  openPreview(image: ImageResponse, event: MouseEvent): void {
    this.addRipple(event);
    this.previewImage = image;
  }
 
  toggleView(event: MouseEvent): void {
    this.addRipple(event);
    this.viewMode = this.viewMode === 'grid' ? 'list' : 'grid';
  }
 
  addRipple(event: MouseEvent): void {
    const el = event.currentTarget as HTMLElement;
    const rect = el.getBoundingClientRect();
    const id = this.rippleCounter++;
    this.ripples.push({ id, x: event.clientX - rect.left, y: event.clientY - rect.top });
    setTimeout(() => { this.ripples = this.ripples.filter(r => r.id !== id); }, 600);
  }
 
  showToast(message: string, type: 'success' | 'error' | 'info'): void {
    this.toast = { message, type };
    setTimeout(() => (this.toast = null), 3500);
  }
 
  formatDate(ts: string): string {
    return new Date(ts).toLocaleString('vi-VN');
  }
 
  trackById(_: number, img: ImageResponse): string {
    return img.id;
  }
}
