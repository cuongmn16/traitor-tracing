import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { User } from '../module/user';
import { UserService } from '../../service/UserService/user.service';

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterOutlet, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
  user?: User;

  constructor(private userService: UserService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.userService.getUser().subscribe({
      next: (data) => {
        this.user = data.result;
        console.log(this.user);
      }
    })
  }

  logout() {
    const confirmLogout =  confirm("Bạn có chắc muốn đăng xuất?");
    if (confirmLogout) {
      this.userService.logout();
      this.router.navigate(["/login"])
    }
  }
}
