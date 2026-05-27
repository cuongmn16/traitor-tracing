import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { UserService } from '../../service/UserService/user.service';
import { User } from '../module/user';
import { FormBuilder, FormGroup, FormsModule, ɵInternalFormsSharedModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiResponse } from '../module/apiResponse';

@Component({
  selector: 'app-user-management',
  imports: [CommonModule, ɵInternalFormsSharedModule, ReactiveFormsModule],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css'
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  registerForm: FormGroup;
  isOpen: boolean = false;

  constructor(private userService: UserService,
    private fb: FormBuilder
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4)]],
      password_hash: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      role:['USER']
    })
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.getUsers().subscribe({
      next: (data) => {
        this.users = data.result?.filter(user => user.role == "USER") || [];
      },
      error: (err) => {
        console.error(err);
      }
    });
  }

  createUser() {
    if (this.registerForm.invalid)
      return;

    const user = this.registerForm.value;
    console.log(user);
    this.userService.createUser(user).subscribe({
      next: (res: ApiResponse<User>) => {
        if (res.code === 1000) {
          alert('Đăng ký User thành công');
          this.registerForm.reset();
          this.closeModal();
          this.loadUsers(); // Tự động load lại danh sách để hiển thị User mới kèm vân tay Tardos
        }
      },
      error: (err) => {
        alert("Lỗi " + err);
        console.log(err)
      }
    });
  }

  deleteUser(id: string) {
    const deleteConfirm = confirm("Bạn có chắc xóa User này với ID: " + id);

    if (deleteConfirm) {
      this.userService.delete(id).subscribe({
        next: (res) => {
          this.users = this.users.filter(user => user.id !== id);
          alert("Xóa thành công!");
        },
        error: (err) => {
          console.error(err);
          alert("Xóa thất bại!");
        }
      })
    }
  }

  openModal() {
    this.isOpen = true;
    this.registerForm.reset({
      username: '',
      name: '',
      password_hash: '',
      role:'USER'
    });
  }

  closeModal() {
    this.isOpen = false;
  }


}
