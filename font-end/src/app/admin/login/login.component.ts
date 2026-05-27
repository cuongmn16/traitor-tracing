import { Component } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from "@angular/router";
import { CommonModule } from '@angular/common';
import { ApiResponse } from '../module/apiResponse';
import { UserService } from '../../service/UserService/user.service';
import { NotificationService } from '../../service/notification.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loginForm: FormGroup;
  message: string = ' ';

  constructor(
    private userService: UserService,
    private router: Router,
    private fb: FormBuilder,
    private notify: NotificationService
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(4)]]
    })
  }

  login() {
    if (this.loginForm.invalid) {
      return
    }
    const data = this.loginForm.value;

    this.userService.login(data).subscribe({
      next: (res: ApiResponse<any>) => {

        if (res.code === 1000 && res.result.authenticated) {
          this.message = res.message ?? " ";
          const token = res.result.token;
          this.notify.success(this.message);

          this.userService.saveToken(token);
          const role = this.userService.getRoleFromToken(token);
          if (role === "ADMIN") {
            this.router.navigate(['/admin/dashboard']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        } else {
          this.message = "Tài khoản không tồn tại";
          this.notify.error(this.message);
        }

      },
      error: (err: any) => {
        console.error(err);
        // Error đã được normalize bởi ErrorInterceptor
        const errorMessage = err?.message || "Lỗi! Không thể kết nối đến máy chủ";
        this.message = errorMessage;
        this.notify.error(errorMessage);
      }
    });
  }
  // login() {

  //   const data = this.loginForm.value;

  //   if (data.username === "admin" && data.password === "123") {
  //     const fakeRes = {
  //       code: 1000,
  //       message: 'Đăng nhập thành công!',
  //       result: {
  //         authenticated: true,
  //         token: 'abc.eyJyb2xlIjoiQURNSU4ifQ==.xyz' // chứa role ADMIN
  //       }
  //     };

  //     console.log(fakeRes);

  //     if (fakeRes.code === 1000 && fakeRes.result.authenticated) {

  //       this.message = fakeRes.message;

  //       const token = fakeRes.result.token;

  //       alert(this.message);

  //       this.loginService.saveToken(token);

  //       const role = this.getRoleFromToken();

  //       if (role === "ADMIN") {
  //         this.router.navigate(['/admin/dashboard']);
  //       } else {
  //         this.router.navigate(['/user/dashboard']);
  //       }
  //     }
  //   }
  // }


}
