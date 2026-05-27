import { Component } from '@angular/core';
import { SidebarComponent } from "./admin/sidebar/sidebar.component";
import { RouterModule, RouterOutlet } from "@angular/router";
import { ToastComponent } from './shared/toast/toast.component';

@Component({
  selector: 'app-root',
  imports: [ RouterModule, RouterOutlet, ToastComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'font-end';
}
