import { Routes } from '@angular/router';
import { LoginComponent } from './admin/login/login.component';
import { RegisterComponent } from './admin/register/register.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { SidebarComponent } from './admin/sidebar/sidebar.component';
import { UserManagementComponent } from './admin/user-management/user-management.component';
import { OriginalResourcesComponent } from './admin/original-resources/original-resources.component';
import { EmbedComponent } from './admin/embed/embed.component';
import { UserDashboardComponent } from './user/user-dashboard/user-dashboard.component';
import { TracingComponent } from './admin/tracing/tracing.component';

export const routes: Routes = [
    { path: '', redirectTo: 'login', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'dashboard', component: UserDashboardComponent},

    {
        path: 'admin',
        component: SidebarComponent,
        children: [
          { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
          { path: 'dashboard', component: DashboardComponent },
          { path: 'user-management', component: UserManagementComponent},
          { path: 'original-resources', component: OriginalResourcesComponent},
          { path: 'tracing', component: TracingComponent}
        ]
    },


    { path: '**', redirectTo: 'login' }
];
