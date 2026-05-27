import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ApiResponse } from '../module/apiResponse';
import { UserService } from '../../service/UserService/user.service';
import { NotificationService } from '../../service/notification.service';

@Component({
  selector: 'app-register',
  imports: [FormsModule, ReactiveFormsModule, CommonModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerForm: FormGroup;
  message: string = '';
  messageType: 'success' | 'error' = 'error';
  isLoading: boolean = false;

  constructor(
    private userService: UserService,
    private router: Router,
    private fb: FormBuilder,
    private notify: NotificationService
  ) {
    this.registerForm = this.fb.group({
      username:['', Validators.required, Validators.maxLength(3)],
      name: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      role: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup) {
    const password = group.get('password');
    const confirmPassword = group.get('confirmPassword');

    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  register() {
    if (this.registerForm.invalid) {
      this.message = 'Vui lòng kiểm tra lại thông tin';
      this.messageType = 'error';
      return;
    }

    this.isLoading = true;
    const data = {
      username: this.registerForm.value.username,
      name: this.registerForm.value.name,
      password_hash: this.registerForm.value.password,
      role: this.registerForm.value.role
    };

    this.userService.register(data).subscribe({
      next: (res: ApiResponse<any>) => {
        this.isLoading = false;

        if (res.code === 1000) {
          this.message = res.message ?? 'Đăng ký thành công! Chuyển hướng đến trang đăng nhập...';
          this.messageType = 'success';
          this.notify.success(this.message);

          // Chuyển hướng đến trang login sau 2 giây
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        } else {
          this.message = res.message ?? 'Đăng ký thất bại';
          this.messageType = 'error';
          this.notify.error(this.message);
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error(err);
        this.message = 'Lỗi! Không thể kết nối đến máy chủ';
        this.messageType = 'error';
        this.notify.error(this.message);
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
}

